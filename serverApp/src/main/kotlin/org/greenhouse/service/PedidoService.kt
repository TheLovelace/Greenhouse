package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.PedidoCreateRequestDTO
import org.greenhouse.dto.request.PedidoItemRequestDTO
import org.greenhouse.dto.request.PedidoUpdateRequestDTO
import org.greenhouse.dto.response.PedidoResponseDTO
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.mapper.PedidoMapper
import org.greenhouse.model.Pedido
import org.greenhouse.model.PedidoItem
import org.greenhouse.repository.ClienteRepository
import org.greenhouse.repository.DescontoRepository
import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.repository.FuncionarioRepository
import org.greenhouse.repository.PedidoItemRepository
import org.greenhouse.repository.PedidoRepository
import org.greenhouse.repository.ProdutoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class PedidoService(
    private val pedidoRepository: PedidoRepository,
    private val pedidoItemRepository: PedidoItemRepository,
    private val clienteRepository: ClienteRepository,
    private val funcionarioRepository: FuncionarioRepository,
    private val produtoRepository: ProdutoRepository,
    private val descontoRepository: DescontoRepository,
    private val formaPagamentoRepository: FormaPagamentoRepository,
    private val estoqueService: EstoqueService,
    private val pedidoMapper: PedidoMapper,
) {
    @Transactional(rollbackFor = [Exception::class])
    fun criar(request: PedidoCreateRequestDTO): PedidoResponseDTO {
        validarDataRetiradaPrevista(request.modalidade, request.dataRetiradaPrevista)
        validarFuncionario(request.funcionarioId)
        validarCliente(request.clienteId)
        validarFormaPagamento(request.formaPagamentoId)
        validarObservacao(request.observacao)
        validarNumeroVolumes(request.numeroVolumes)

        val itens = resolverItens(request.itens)
        val (valorDesconto, valorTotal) = calcularTotais(itens, request.descontoId)

        val salvo =
            pedidoRepository.save(
                pedidoMapper.toModel(request).copy(
                    status = StatusPedido.ABERTO,
                    valorDesconto = valorDesconto,
                    valorTotal = valorTotal,
                ),
            )
        val pedidoId = requireNotNull(salvo.id)
        val itensSalvos = pedidoItemRepository.saveAll(itens.map { it.copy(pedidoId = pedidoId) })

        return pedidoMapper.toResponse(salvo, itensSalvos)
    }

    fun listar(
        clienteId: Long? = null,
        funcionarioId: Long? = null,
        status: String? = null,
        modalidade: String? = null,
    ): List<PedidoResponseDTO> {
        val pedidos =
            pedidoRepository.listar(
                clienteId = clienteId,
                funcionarioId = funcionarioId,
                status = status?.let(::parseStatus),
                modalidade = modalidade?.let(::parseModalidade),
            )
        val itensPorPedido =
            if (pedidos.isEmpty()) {
                emptyMap()
            } else {
                pedidoItemRepository
                    .buscarPorPedidoIds(pedidos.mapNotNull { it.id })
                    .groupBy { it.pedidoId }
            }
        return pedidos.map { pedidoMapper.toResponse(it, itensPorPedido[it.id].orEmpty()) }
    }

    fun buscarPorId(id: Long): PedidoResponseDTO {
        val idValidado = validarId(id)
        val pedido =
            pedidoRepository.buscarPorId(idValidado)
                ?: throw NotFoundException("Pedido não encontrado")
        return pedidoMapper.toResponse(pedido, pedidoItemRepository.buscarPorPedidoId(idValidado))
    }

    @Transactional(rollbackFor = [Exception::class])
    fun atualizar(id: Long, request: PedidoUpdateRequestDTO): PedidoResponseDTO {
        val idValidado = validarId(id)
        val atual =
            pedidoRepository.buscarPorId(idValidado)
                ?: throw NotFoundException("Pedido não encontrado")
        if (!request.possuiCamposParaAtualizar) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }
        if (atual.status != StatusPedido.ABERTO) {
            throw UnprocessableEntityException("Um pedido ${atual.status} não pode ser alterado")
        }

        val atualizado = pedidoMapper.toModel(request, atual)

        if (request.dataRetiradaPrevista != null) {
            validarDataRetiradaPrevista(atualizado.modalidade, atualizado.dataRetiradaPrevista)
        }
        if (request.funcionarioId != null) {
            validarFuncionario(atualizado.funcionarioId)
        }
        if (request.clienteId != null) {
            validarCliente(atualizado.clienteId)
        }
        if (request.formaPagamentoId != null) {
            validarFormaPagamento(atualizado.formaPagamentoId)
        }
        if (request.observacao != null) {
            validarObservacao(atualizado.observacao)
        }
        if (request.numeroVolumes != null) {
            validarNumeroVolumes(atualizado.numeroVolumes)
        }

        val itensAtualizados =
            if (request.itens != null) {
                resolverItens(request.itens)
            } else {
                pedidoItemRepository.buscarPorPedidoId(idValidado)
            }
        val comTotais =
            if (request.itens != null || request.descontoId != null) {
                val (valorDesconto, valorTotal) = calcularTotais(itensAtualizados, atualizado.descontoId)
                atualizado.copy(valorDesconto = valorDesconto, valorTotal = valorTotal)
            } else {
                atualizado
            }

        val salvo = pedidoRepository.save(comTotais)
        if (request.itens == null) {
            return pedidoMapper.toResponse(salvo, itensAtualizados)
        }

        pedidoItemRepository.excluirPorPedidoId(idValidado)
        val novosItens = pedidoItemRepository.saveAll(itensAtualizados.map { it.copy(id = null, pedidoId = idValidado) })
        return pedidoMapper.toResponse(salvo, novosItens)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun atualizarStatus(id: Long, novoStatus: StatusPedido): PedidoResponseDTO {
        val idValidado = validarId(id)
        val pedido =
            pedidoRepository.buscarParaAtualizar(idValidado)
                ?: throw NotFoundException("Pedido não encontrado")

        validarTransicao(pedido, novoStatus)
        val itens = pedidoItemRepository.buscarPorPedidoId(idValidado)

        when (novoStatus) {
            StatusPedido.CONFIRMADO ->
                if (pedido.modalidade == ModalidadePedido.AGENDADA) {
                    itens.forEach { estoqueService.reservar(it.produtoId, it.quantidade) }
                }

            StatusPedido.CONCLUIDO -> {
                if (pedido.modalidade == ModalidadePedido.AGENDADA) {
                    itens.forEach { estoqueService.concluirPedidoAgendado(it.produtoId, it.quantidade) }
                } else {
                    itens.forEach { estoqueService.concluirPedidoImediato(it.produtoId, it.quantidade) }
                }
                pedido.dataRetirada = OffsetDateTime.now()
            }

            StatusPedido.CANCELADO ->
                if (pedido.modalidade == ModalidadePedido.AGENDADA &&
                    pedido.status in setOf(StatusPedido.CONFIRMADO, StatusPedido.PRONTO_RETIRADA)
                ) {
                    itens.forEach { estoqueService.liberarReserva(it.produtoId, it.quantidade) }
                }

            else -> Unit
        }

        pedido.status = novoStatus
        return pedidoMapper.toResponse(pedidoRepository.save(pedido), itens)
    }

    private fun validarTransicao(
        pedido: Pedido,
        novoStatus: StatusPedido,
    ) {
        val permitida =
            when (pedido.status to novoStatus) {
                StatusPedido.ABERTO to StatusPedido.CONFIRMADO,
                StatusPedido.ABERTO to StatusPedido.CANCELADO,
                StatusPedido.CONFIRMADO to StatusPedido.PRONTO_RETIRADA,
                StatusPedido.CONFIRMADO to StatusPedido.CANCELADO,
                StatusPedido.PRONTO_RETIRADA to StatusPedido.CONCLUIDO,
                StatusPedido.PRONTO_RETIRADA to StatusPedido.CANCELADO,
                -> true

                StatusPedido.CONFIRMADO to StatusPedido.CONCLUIDO ->
                    pedido.modalidade == ModalidadePedido.IMEDIATA

                else -> false
            }

        if (!permitida) {
            throw UnprocessableEntityException(
                "A transição de ${pedido.status} para $novoStatus não é permitida",
            )
        }
    }

    private fun resolverItens(itens: List<PedidoItemRequestDTO>): List<PedidoItem> {
        if (itens.isEmpty()) {
            throw UnprocessableEntityException("O pedido deve ter ao menos um item")
        }
        itens.forEach { item ->
            if (item.produtoId <= 0) {
                throw UnprocessableEntityException("O id do produto deve ser maior que zero")
            }
            if (item.quantidade <= 0) {
                throw UnprocessableEntityException("A quantidade do item deve ser maior que zero")
            }
        }
        if (itens.map { it.produtoId }.toSet().size != itens.size) {
            throw UnprocessableEntityException("O pedido não pode ter itens repetidos para o mesmo produto")
        }

        return itens.map { item ->
            val produto =
                produtoRepository.buscarPorId(item.produtoId)
                    ?: throw UnprocessableEntityException("Produto não encontrado")
            if (!produto.ativo) {
                throw UnprocessableEntityException("Produto inativo")
            }
            estoqueService.validarDisponibilidade(item.produtoId, item.quantidade)
            PedidoItem(
                produtoId = item.produtoId,
                quantidade = item.quantidade,
                precoUnitario = produto.preco,
            )
        }
    }

    private fun calcularTotais(
        itens: List<PedidoItem>,
        descontoId: Long?,
    ): Pair<BigDecimal, BigDecimal> {
        val subtotal =
            itens.fold(BigDecimal.ZERO) { acc, item ->
                acc + item.precoUnitario.multiply(BigDecimal(item.quantidade))
            }

        if (descontoId == null) {
            return BigDecimal.ZERO.setScale(2) to subtotal
        }

        val desconto =
            descontoRepository.buscarPorId(descontoId)
                ?: throw UnprocessableEntityException("Desconto não encontrado")
        if (!desconto.ativo) {
            throw UnprocessableEntityException("Desconto inativo")
        }
        val agora = OffsetDateTime.now()
        if ((desconto.dataInicio != null && agora.isBefore(desconto.dataInicio)) ||
            (desconto.dataFim != null && agora.isAfter(desconto.dataFim))
        ) {
            throw UnprocessableEntityException("Desconto fora do período de vigência")
        }

        val valorDesconto =
            when (desconto.tipo) {
                TipoDesconto.PERCENTUAL ->
                    subtotal
                        .multiply(desconto.valor)
                        .divide(BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP)

                TipoDesconto.VALOR_FIXO -> desconto.valor.min(subtotal)
            }

        return valorDesconto to subtotal.subtract(valorDesconto)
    }

    private fun validarDataRetiradaPrevista(
        modalidade: ModalidadePedido,
        dataRetiradaPrevista: OffsetDateTime?,
    ) {
        if (modalidade == ModalidadePedido.AGENDADA && dataRetiradaPrevista == null) {
            throw UnprocessableEntityException("A data de retirada prevista é obrigatória para pedidos agendados")
        }
        if (dataRetiradaPrevista != null && dataRetiradaPrevista.toLocalDate().isBefore(LocalDate.now())) {
            throw UnprocessableEntityException("A data de retirada prevista não pode ser anterior a hoje")
        }
    }

    private fun validarFuncionario(funcionarioId: Long) {
        if (funcionarioId <= 0) {
            throw BadRequestException("O id do funcionário deve ser maior que zero")
        }
        val funcionario =
            funcionarioRepository.buscarPorId(funcionarioId)
                ?: throw UnprocessableEntityException("Funcionário não encontrado")
        if (!funcionario.ativo) {
            throw UnprocessableEntityException("Funcionário inativo")
        }
    }

    private fun validarCliente(clienteId: Long?) {
        clienteId ?: return
        if (clienteId <= 0) {
            throw BadRequestException("O id do cliente deve ser maior que zero")
        }
        val cliente =
            clienteRepository
                .buscarPorId(clienteId)
                ?: throw UnprocessableEntityException("Cliente não encontrado")
        if (!cliente.ativo) {
            throw UnprocessableEntityException("Cliente inativo")
        }
    }

    private fun validarFormaPagamento(formaPagamentoId: Long) {
        if (formaPagamentoId <= 0) {
            throw BadRequestException("O id da forma de pagamento deve ser maior que zero")
        }
        val formaPagamento =
            formaPagamentoRepository.buscarPorId(formaPagamentoId)
                ?: throw UnprocessableEntityException("Forma de pagamento não encontrada")
        if (!formaPagamento.ativo) {
            throw UnprocessableEntityException("Forma de pagamento inativa")
        }
    }

    private fun validarObservacao(observacao: String?) {
        if ((observacao?.length ?: 0) > 1000) {
            throw UnprocessableEntityException("A observação deve ter no máximo 1000 caracteres")
        }
    }

    private fun validarNumeroVolumes(numeroVolumes: Int) {
        if (numeroVolumes < 1) {
            throw UnprocessableEntityException("O número de volumes deve ser no mínimo 1")
        }
    }

    private fun parseStatus(valor: String): StatusPedido =
        try {
            StatusPedido.valueOf(valor)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("O status deve ser ABERTO, CONFIRMADO, PRONTO_RETIRADA, CONCLUIDO ou CANCELADO", e)
        }

    private fun parseModalidade(valor: String): ModalidadePedido =
        try {
            ModalidadePedido.valueOf(valor)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("A modalidade deve ser IMEDIATA ou AGENDADA", e)
        }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id do pedido deve ser maior que zero")
        }
        return id
    }
}

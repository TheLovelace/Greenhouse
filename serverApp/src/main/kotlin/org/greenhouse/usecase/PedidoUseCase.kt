package org.greenhouse.usecase

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Pedido
import org.greenhouse.entity.PedidoItem
import org.greenhouse.entity.Produto
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.enums.TipoMovimentacaoEstoque
import org.greenhouse.repository.ClienteRepository
import org.greenhouse.repository.DescontoRepository
import org.greenhouse.repository.PedidoRepository
import org.greenhouse.repository.ProdutoRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

@Service
class PedidoUseCase(
    private val databaseConfig: DatabaseConfig,
    private val pedidoRepository: PedidoRepository,
    private val clienteRepository: ClienteRepository,
    private val produtoRepository: ProdutoRepository,
    private val descontoRepository: DescontoRepository,
    private val estoqueUseCase: EstoqueUseCase,
) {
    fun criar(pedido: Pedido): Pedido {
        val normalizado =
            pedido.copy(
                id = null,
                status = StatusPedido.ABERTO,
                dataPedido = null,
                dataRetirada = null,
            )

        validarDataRetiradaPrevista(normalizado.modalidade, normalizado.dataRetiradaPrevista)
        validarFuncionario(normalizado.funcionarioId)
        validarCliente(normalizado.clienteId)

        val itens = resolverItens(normalizado.itens)
        val (valorDesconto, valorTotal) = calcularTotais(itens, normalizado.descontoId)

        return pedidoRepository.criar(
            normalizado.copy(
                valorDesconto = valorDesconto,
                valorTotal = valorTotal,
                itens = itens,
            ),
        )
    }

    fun listar(
        clienteId: Long? = null,
        funcionarioId: Long? = null,
        status: String? = null,
        modalidade: String? = null,
    ): List<Pedido> =
        pedidoRepository.listar(
            clienteId = clienteId,
            funcionarioId = funcionarioId,
            status = status?.let { parseStatus(it) },
            modalidade = modalidade?.let { parseModalidade(it) },
        )

    fun buscarPorId(id: Long): Pedido =
        pedidoRepository.buscarPorId(validarId(id))
            ?: throw NotFoundException("Pedido não encontrado")

    fun atualizar(
        id: Long,
        campos: Map<String, Any?>,
    ): Pedido {
        val idValidado = validarId(id)
        val pedidoAtual =
            pedidoRepository.buscarPorId(idValidado)
                ?: throw NotFoundException("Pedido não encontrado")

        val camposPermitidos =
            setOf("id", "clienteId", "funcionarioId", "descontoId", "dataRetiradaPrevista", "observacao", "itens")
        val camposDesconhecidos = campos.keys - camposPermitidos
        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        when (pedidoAtual.status) {
            StatusPedido.ABERTO -> Unit
            StatusPedido.CONFIRMADO ->
                if (campos.containsKey("itens")) {
                    throw UnprocessableEntityException("Os itens só podem ser alterados em pedidos abertos")
                }
            else ->
                throw UnprocessableEntityException("Um pedido ${pedidoAtual.status} não pode ser alterado")
        }

        val itensInformados = campos.containsKey("itens")
        val itensAtualizados =
            if (itensInformados) {
                resolverItens(campos.itens("itens"))
            } else {
                pedidoAtual.itens
            }

        val pedidoAtualizado =
            pedidoAtual.copy(
                clienteId = campos.longoOpcional("clienteId", pedidoAtual.clienteId),
                funcionarioId = campos.longo("funcionarioId", pedidoAtual.funcionarioId),
                descontoId = campos.longoOpcional("descontoId", pedidoAtual.descontoId),
                dataRetiradaPrevista =
                    campos.instanteOpcional("dataRetiradaPrevista", pedidoAtual.dataRetiradaPrevista),
                observacao = campos.textoOpcional("observacao", pedidoAtual.observacao),
                itens = itensAtualizados,
            )

        if (campos.containsKey("dataRetiradaPrevista")) {
            validarDataRetiradaPrevista(pedidoAtualizado.modalidade, pedidoAtualizado.dataRetiradaPrevista)
        }
        if (campos.containsKey("funcionarioId")) {
            validarFuncionario(pedidoAtualizado.funcionarioId)
        }
        if (campos.containsKey("clienteId")) {
            validarCliente(pedidoAtualizado.clienteId)
        }

        val comTotais =
            if (itensInformados || campos.containsKey("descontoId")) {
                val (valorDesconto, valorTotal) = calcularTotais(itensAtualizados, pedidoAtualizado.descontoId)
                pedidoAtualizado.copy(valorDesconto = valorDesconto, valorTotal = valorTotal)
            } else {
                pedidoAtualizado
            }

        return pedidoRepository.atualizar(idValidado, comTotais, substituirItens = itensInformados)
            ?: throw NotFoundException("Pedido não encontrado")
    }

    fun atualizarStatus(
        id: Long,
        novoStatus: StatusPedido,
    ): Pedido =
        databaseConfig.session {
            val pedido =
                pedidoRepository.bloquearPorId(validarId(id))
                    ?: throw NotFoundException("Pedido não encontrado")

            validarTransicao(pedido, novoStatus)

            when (novoStatus) {
                StatusPedido.CONFIRMADO ->
                    if (pedido.modalidade == ModalidadePedido.AGENDADA) {
                        pedido.itens.forEach { estoqueUseCase.reservar(it.produtoId, it.quantidade) }
                    }
                StatusPedido.CONCLUIDO -> {
                    if (pedido.modalidade == ModalidadePedido.AGENDADA) {
                        pedido.itens.forEach { estoqueUseCase.liberarReserva(it.produtoId, it.quantidade) }
                    }
                    pedido.itens.forEach {
                        estoqueUseCase.registrarMovimentacaoInterna(
                            produtoId = it.produtoId,
                            tipo = TipoMovimentacaoEstoque.SAIDA,
                            quantidade = it.quantidade,
                            observacao = "Baixa por conclusão do pedido",
                            pedidoId = pedido.id,
                            funcionarioId = pedido.funcionarioId,
                        )
                    }
                }
                StatusPedido.CANCELADO ->
                    if (pedido.modalidade == ModalidadePedido.AGENDADA &&
                        pedido.status in setOf(StatusPedido.CONFIRMADO, StatusPedido.PRONTO_RETIRADA)
                    ) {
                        pedido.itens.forEach { estoqueUseCase.liberarReserva(it.produtoId, it.quantidade) }
                    }
                else -> Unit
            }

            pedidoRepository.atualizarStatus(
                id = pedido.id!!,
                status = novoStatus,
                dataRetirada = if (novoStatus == StatusPedido.CONCLUIDO) OffsetDateTime.now() else null,
            ) ?: throw NotFoundException("Pedido não encontrado")
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

    private fun resolverItens(itens: List<PedidoItem>): List<PedidoItem> {
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
            val produto = buscarProdutoAtivo(item.produtoId)
            val disponivel = estoqueUseCase.buscarPosicaoPorProdutoId(item.produtoId).quantidadeDisponivel
            if (disponivel < item.quantidade) {
                throw UnprocessableEntityException(
                    "Estoque insuficiente para o produto ${item.produtoId}: disponível $disponivel, solicitado ${item.quantidade}",
                )
            }
            item.copy(id = null, precoUnitario = produto.preco)
        }
    }

    private fun buscarProdutoAtivo(produtoId: Long): Produto {
        val produto =
            produtoRepository.buscarPorId(produtoId)
                ?: throw UnprocessableEntityException("Produto não encontrado")
        if (!produto.ativo) {
            throw UnprocessableEntityException("Produto inativo")
        }
        return produto
    }

    private fun calcularTotais(
        itens: List<PedidoItem>,
        descontoId: Long?,
    ): Pair<BigDecimal, BigDecimal> {
        val subtotal =
            itens.fold(BigDecimal.ZERO) { acc, item ->
                acc + item.precoUnitario!!.multiply(BigDecimal(item.quantidade))
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
        when (pedidoRepository.funcionarioAtivo(funcionarioId)) {
            null -> throw UnprocessableEntityException("Funcionário não encontrado")
            false -> throw UnprocessableEntityException("Funcionário inativo")
            else -> Unit
        }
    }

    private fun validarCliente(clienteId: Long?) {
        clienteId ?: return
        val cliente =
            clienteRepository.buscarPorId(clienteId)
                ?: throw UnprocessableEntityException("Cliente não encontrado")
        if (!cliente.ativo) {
            throw UnprocessableEntityException("Cliente inativo")
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

    private fun Map<String, Any?>.textoOpcional(
        campo: String,
        valorAtual: String?,
    ): String? {
        if (!containsKey(campo)) return valorAtual
        val valor = this[campo] ?: return null
        return valor as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto ou nulo")
    }

    private fun Map<String, Any?>.longo(
        campo: String,
        valorAtual: Long,
    ): Long {
        if (!containsKey(campo)) return valorAtual
        val valor =
            this[campo] as? Number
                ?: throw BadRequestException("O campo '$campo' deve ser numérico")
        return valor.toLong()
    }

    private fun Map<String, Any?>.longoOpcional(
        campo: String,
        valorAtual: Long?,
    ): Long? {
        if (!containsKey(campo)) return valorAtual
        val valor = this[campo] ?: return null
        return (valor as? Number)?.toLong()
            ?: throw BadRequestException("O campo '$campo' deve ser numérico ou nulo")
    }

    private fun Map<String, Any?>.instanteOpcional(
        campo: String,
        valorAtual: OffsetDateTime?,
    ): OffsetDateTime? {
        if (!containsKey(campo)) return valorAtual
        val valor = this[campo] ?: return null
        val texto =
            valor as? String
                ?: throw BadRequestException("O campo '$campo' deve ser uma data ISO-8601 ou nulo")
        return try {
            OffsetDateTime.parse(texto)
        } catch (e: DateTimeParseException) {
            throw BadRequestException("O campo '$campo' deve ser uma data ISO-8601 válida", e)
        }
    }

    private fun Map<String, Any?>.itens(campo: String): List<PedidoItem> {
        val valor =
            this[campo] as? List<*>
                ?: throw BadRequestException("O campo '$campo' deve ser uma lista de itens")
        return valor.map { elemento ->
            val mapa =
                elemento as? Map<*, *>
                    ?: throw BadRequestException("Cada item deve ser um objeto com produtoId e quantidade")
            val chavesConhecidas = setOf("id", "produtoId", "quantidade", "precoUnitario")
            val chavesDesconhecidas = mapa.keys.filter { it !in chavesConhecidas }
            if (chavesDesconhecidas.isNotEmpty()) {
                throw BadRequestException("Campo(s) não reconhecido(s) no item: ${chavesDesconhecidas.joinToString()}")
            }
            val produtoId =
                (mapa["produtoId"] as? Number)?.toLong()
                    ?: throw BadRequestException("O campo 'produtoId' do item deve ser numérico")
            val quantidade =
                (mapa["quantidade"] as? Number)?.toInt()
                    ?: throw BadRequestException("O campo 'quantidade' do item deve ser numérico")
            PedidoItem(produtoId = produtoId, quantidade = quantidade)
        }
    }
}

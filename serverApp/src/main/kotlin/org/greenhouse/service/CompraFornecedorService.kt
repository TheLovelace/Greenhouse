package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraFornecedorUpdateRequestDTO
import org.greenhouse.dto.request.CompraItemRequestDTO
import org.greenhouse.dto.response.CompraFornecedorResponseDTO
import org.greenhouse.enums.StatusCompra
import org.greenhouse.mapper.CompraFornecedorMapper
import org.greenhouse.model.CompraFornecedor
import org.greenhouse.model.CompraItem
import org.greenhouse.repository.CompraFornecedorRepository
import org.greenhouse.repository.CompraItemRepository
import org.greenhouse.repository.FornecedorRepository
import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.repository.FuncionarioRepository
import org.greenhouse.repository.ProdutoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val VALOR_MAXIMO_COMPRA = BigDecimal("9999999999.99")

@Service
class CompraFornecedorService(
    private val compraFornecedorRepository: CompraFornecedorRepository,
    private val compraItemRepository: CompraItemRepository,
    private val funcionarioRepository: FuncionarioRepository,
    private val fornecedorRepository: FornecedorRepository,
    private val formaPagamentoRepository: FormaPagamentoRepository,
    private val produtoRepository: ProdutoRepository,
    private val estoqueService: EstoqueService,
    private val compraFornecedorMapper: CompraFornecedorMapper,
) {
    @Transactional(rollbackFor = [Exception::class])
    fun criar(request: CompraFornecedorCreateRequestDTO): CompraFornecedorResponseDTO {
        validarFuncionario(request.funcionarioId)
        validarFornecedor(request.fornecedorId)
        validarFormaPagamento(request.formaPagamentoId)
        validarObservacao(request.observacao)
        validarValorMonetario(request.valorFrete, "O valor do frete")

        val itens = resolverItens(request.itens)
        val valorTotal = calcularValorTotal(itens, request.valorFrete, request.freteIncluso)

        val salvo =
            compraFornecedorRepository.save(
                compraFornecedorMapper.toModel(request).copy(
                    status = StatusCompra.PEDIDA,
                    valorTotal = valorTotal,
                ),
            )
        val compraId = requireNotNull(salvo.id)
        val itensSalvos = compraItemRepository.saveAll(itens.map { it.copy(compraId = compraId) })

        return compraFornecedorMapper.toResponse(salvo, itensSalvos)
    }

    fun listar(observacao: String? = null): List<CompraFornecedorResponseDTO> {
        val compras =
            if (observacao == null) {
                compraFornecedorRepository.listar()
            } else {
                compraFornecedorRepository.buscarPorObservacao(observacao)
            }
        val itensPorCompra =
            if (compras.isEmpty()) {
                emptyMap()
            } else {
                compraItemRepository
                    .buscarPorCompraIds(compras.mapNotNull { it.id })
                    .groupBy { it.compraId }
            }
        return compras.map { compraFornecedorMapper.toResponse(it, itensPorCompra[it.id].orEmpty()) }
    }

    fun buscarPorId(id: Long): CompraFornecedorResponseDTO {
        val idValidado = validarId(id)
        val compra =
            compraFornecedorRepository.buscarPorId(idValidado)
                ?: throw NotFoundException("Compra não encontrada")
        return compraFornecedorMapper.toResponse(compra, compraItemRepository.buscarPorCompraId(idValidado))
    }

    @Transactional(rollbackFor = [Exception::class])
    fun atualizar(
        id: Long,
        request: CompraFornecedorUpdateRequestDTO,
    ): CompraFornecedorResponseDTO {
        val idValidado = validarId(id)
        val atual =
            compraFornecedorRepository.buscarParaAtualizar(idValidado)
                ?: throw NotFoundException("Compra não encontrada")
        if (!request.possuiCamposParaAtualizar) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }
        if (atual.status != StatusCompra.PEDIDA) {
            throw UnprocessableEntityException("Uma compra ${atual.status} não pode ser alterada")
        }

        val atualizado = compraFornecedorMapper.toModel(request, atual)

        if (request.funcionarioId != null) {
            validarFuncionario(atualizado.funcionarioId)
        }
        if (request.fornecedorId != null) {
            validarFornecedor(atualizado.fornecedorId)
        }
        if (request.formaPagamentoId != null) {
            validarFormaPagamento(atualizado.formaPagamentoId)
        }
        if (request.observacao != null) {
            validarObservacao(atualizado.observacao)
        }
        if (request.valorFrete != null) {
            validarValorMonetario(atualizado.valorFrete, "O valor do frete")
        }

        val itensAtualizados =
            if (request.itens != null) {
                resolverItens(request.itens)
            } else {
                compraItemRepository.buscarPorCompraId(idValidado)
            }
        val comTotal =
            if (request.itens != null || request.valorFrete != null || request.freteIncluso != null) {
                atualizado.copy(
                    valorTotal = calcularValorTotal(itensAtualizados, atualizado.valorFrete, atualizado.freteIncluso),
                )
            } else {
                atualizado
            }

        val salvo = compraFornecedorRepository.save(comTotal)
        if (request.itens == null) {
            return compraFornecedorMapper.toResponse(salvo, itensAtualizados)
        }

        compraItemRepository.excluirPorCompraId(idValidado)
        val novosItens =
            compraItemRepository.saveAll(itensAtualizados.map { it.copy(id = null, compraId = idValidado) })
        return compraFornecedorMapper.toResponse(salvo, novosItens)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun atualizarStatus(
        id: Long,
        novoStatus: StatusCompra,
    ): CompraFornecedorResponseDTO {
        val idValidado = validarId(id)
        val compra =
            compraFornecedorRepository.buscarParaAtualizar(idValidado)
                ?: throw NotFoundException("Compra não encontrada")

        validarTransicao(compra, novoStatus)
        val itens = compraItemRepository.buscarPorCompraId(idValidado)

        if (novoStatus == StatusCompra.RECEBIDA) {
            itens.forEach { estoqueService.registrarEntradaCompra(it.produtoId, it.quantidade) }
        }

        compra.status = novoStatus
        return compraFornecedorMapper.toResponse(compraFornecedorRepository.save(compra), itens)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun excluir(id: Long) {
        val idValidado = validarId(id)
        val compra =
            compraFornecedorRepository.buscarPorId(idValidado)
                ?: throw NotFoundException("Compra não encontrada")
        if (compra.status != StatusCompra.PEDIDA) {
            throw UnprocessableEntityException("Uma compra ${compra.status} não pode ser excluída")
        }
        compraItemRepository.excluirPorCompraId(idValidado)
        compraFornecedorRepository.excluir(idValidado)
    }

    private fun validarTransicao(
        compra: CompraFornecedor,
        novoStatus: StatusCompra,
    ) {
        val permitida =
            when (compra.status to novoStatus) {
                StatusCompra.PEDIDA to StatusCompra.RECEBIDA,
                StatusCompra.PEDIDA to StatusCompra.CANCELADA,
                -> true

                else -> false
            }

        if (!permitida) {
            throw UnprocessableEntityException(
                "A transição de ${compra.status} para $novoStatus não é permitida",
            )
        }
    }

    private fun resolverItens(itens: List<CompraItemRequestDTO>): List<CompraItem> {
        if (itens.isEmpty()) {
            throw UnprocessableEntityException("A compra deve ter ao menos um item")
        }
        itens.forEach { item ->
            if (item.produtoId <= 0) {
                throw UnprocessableEntityException("O id do produto deve ser maior que zero")
            }
            if (item.quantidade <= 0) {
                throw UnprocessableEntityException("A quantidade do item deve ser maior que zero")
            }
            validarValorMonetario(item.valorUnitario, "O valor unitário")
        }
        if (itens.map { it.produtoId }.toSet().size != itens.size) {
            throw UnprocessableEntityException("A compra não pode ter itens repetidos para o mesmo produto")
        }

        return itens.map { item ->
            val produto =
                produtoRepository.buscarPorId(item.produtoId)
                    ?: throw UnprocessableEntityException("Produto não encontrado")
            if (!produto.ativo) {
                throw UnprocessableEntityException("Produto inativo")
            }
            CompraItem(
                produtoId = item.produtoId,
                quantidade = item.quantidade,
                valorUnitario = item.valorUnitario,
            )
        }
    }

    // O frete só compõe o valor total quando não está incluso nos itens
    // (freteIncluso = false), evitando dupla contagem do frete.
    private fun calcularValorTotal(
        itens: List<CompraItem>,
        valorFrete: BigDecimal?,
        freteIncluso: Boolean,
    ): BigDecimal {
        val subtotal =
            itens.fold(BigDecimal.ZERO) { acc, item ->
                acc + item.valorUnitario.multiply(BigDecimal(item.quantidade))
            }
        return if (freteIncluso) subtotal else subtotal.add(valorFrete ?: BigDecimal.ZERO)
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

    private fun validarFornecedor(fornecedorId: Long) {
        if (fornecedorId <= 0) {
            throw BadRequestException("O id do fornecedor deve ser maior que zero")
        }
        val fornecedor =
            fornecedorRepository.buscarPorId(fornecedorId)
                ?: throw UnprocessableEntityException("Fornecedor não encontrado")
        if (!fornecedor.ativo) {
            throw UnprocessableEntityException("Fornecedor inativo")
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

    private fun validarValorMonetario(
        valor: BigDecimal?,
        campo: String,
    ) {
        valor ?: return
        if (valor < BigDecimal.ZERO) {
            throw UnprocessableEntityException("$campo não pode ser negativo")
        }
        if (valor > VALOR_MAXIMO_COMPRA || valor.stripTrailingZeros().scale() > 2) {
            throw UnprocessableEntityException("$campo deve respeitar o formato NUMERIC(12,2)")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id da compra deve ser maior que zero")
        }
        return id
    }
}

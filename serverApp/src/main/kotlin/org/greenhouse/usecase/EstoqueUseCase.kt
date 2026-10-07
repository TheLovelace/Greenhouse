package org.greenhouse.usecase

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.entity.EstoquePosicao
import org.greenhouse.entity.MovimentacaoEstoque
import org.greenhouse.enums.TipoMovimentacaoEstoque
import org.greenhouse.repository.EstoqueRepository
import org.springframework.stereotype.Service

@Service
class EstoqueUseCase(
    private val estoqueRepository: EstoqueRepository,
) {
    fun listarMovimentacoes(): List<MovimentacaoEstoque> = estoqueRepository.listarMovimentacoes()

    fun buscarPosicaoPorProdutoId(produtoId: Long): EstoquePosicao {
        if (produtoId <= 0) {
            throw BadRequestException("O id do produto deve ser maior que zero")
        }

        return estoqueRepository.buscarPosicaoPorProdutoId(produtoId)
            ?: throw NotFoundException("Produto não encontrado")
    }

    fun registrarMovimentacao(
        produtoId: Long,
        tipo: TipoMovimentacaoEstoque,
        quantidade: Int,
        observacao: String?,
    ): MovimentacaoEstoque {
        if (tipo !in TIPOS_MOVIMENTACAO_MANUAL) {
            throw BadRequestException("A rota manual aceita somente AJUSTE_ENTRADA, AJUSTE_SAIDA ou PERDA")
        }

        return registrarMovimentacaoInterna(
            produtoId = produtoId,
            tipo = tipo,
            quantidade = quantidade,
            observacao = observacao,
        )
    }

    internal fun registrarMovimentacaoInterna(
        produtoId: Long,
        tipo: TipoMovimentacaoEstoque,
        quantidade: Int,
        observacao: String?,
        pedidoId: Long? = null,
        compraId: Long? = null,
        funcionarioId: Long? = null,
    ): MovimentacaoEstoque {
        if (produtoId <= 0) {
            throw BadRequestException("O id do produto deve ser maior que zero")
        }
        if (quantidade <= 0) {
            throw BadRequestException("A quantidade da movimentação deve ser maior que zero")
        }
        if (observacao != null && observacao.length > 10_000) {
            throw BadRequestException("A observação deve ter no máximo 10000 caracteres")
        }

        validarOrigem(tipo, pedidoId, compraId)

        return estoqueRepository.registrarMovimentacao(
            MovimentacaoEstoque(
                produtoId = produtoId,
                pedidoId = pedidoId,
                compraId = compraId,
                funcionarioId = funcionarioId,
                tipo = tipo,
                quantidade = quantidade,
                observacao = observacao,
            ),
        ) ?: throw NotFoundException("Produto não encontrado")
    }

    private fun validarOrigem(
        tipo: TipoMovimentacaoEstoque,
        pedidoId: Long?,
        compraId: Long?,
    ) {
        when (tipo) {
            TipoMovimentacaoEstoque.ENTRADA -> {
                if (compraId == null || pedidoId != null) {
                    throw BadRequestException("ENTRADA deve estar vinculada somente a uma compra recebida")
                }
            }
            TipoMovimentacaoEstoque.SAIDA -> {
                if (pedidoId == null || compraId != null) {
                    throw BadRequestException("SAIDA deve estar vinculada somente a um pedido concluído")
                }
            }
            TipoMovimentacaoEstoque.AJUSTE_ENTRADA,
            TipoMovimentacaoEstoque.AJUSTE_SAIDA,
            TipoMovimentacaoEstoque.PERDA,
            -> {
                if (pedidoId != null || compraId != null) {
                    throw BadRequestException("Ajustes e perdas manuais não podem ser vinculados a compra ou pedido")
                }
            }
        }
        if (pedidoId != null && pedidoId <= 0 || compraId != null && compraId <= 0) {
            throw BadRequestException("O id de compra ou pedido deve ser maior que zero")
        }
    }

    private companion object {
        val TIPOS_MOVIMENTACAO_MANUAL =
            setOf(
                TipoMovimentacaoEstoque.AJUSTE_ENTRADA,
                TipoMovimentacaoEstoque.AJUSTE_SAIDA,
                TipoMovimentacaoEstoque.PERDA,
            )
    }
}

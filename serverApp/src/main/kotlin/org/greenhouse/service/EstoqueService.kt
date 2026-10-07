package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.EstoqueDTO
import org.greenhouse.dto.MovimentacaoEstoqueDTO as MovimentacaoEstoque
import org.greenhouse.model.Estoque as EstoqueModel
import org.greenhouse.enums.TipoMovimentacaoEstoque
import org.greenhouse.mapper.EstoqueMapper
import org.greenhouse.mapper.MovimentacaoEstoqueMapper
import org.greenhouse.repository.EstoqueRepository
import org.greenhouse.repository.MovimentacaoEstoqueRepository
import org.greenhouse.repository.ProdutoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EstoqueService(
    private val estoqueRepository: EstoqueRepository,
    private val produtoRepository: ProdutoRepository,
    private val movimentacaoEstoqueRepository: MovimentacaoEstoqueRepository,
    private val estoqueMapper: EstoqueMapper,
    private val movimentacaoEstoqueMapper: MovimentacaoEstoqueMapper,
) {
    fun listarMovimentacoes(): List<MovimentacaoEstoque> =
        movimentacaoEstoqueRepository.listarMovimentacoes().map(movimentacaoEstoqueMapper::toDto)

    fun buscarPosicaoPorProdutoId(produtoId: Long): EstoqueDTO {
        if (produtoId <= 0) {
            throw BadRequestException("O id do produto deve ser maior que zero")
        }

        if (produtoRepository.buscarPorId(produtoId) == null) {
            throw NotFoundException("Produto não encontrado")
        }

        return estoqueRepository.buscarPorProdutoId(produtoId)?.let(estoqueMapper::toDto)
            ?: EstoqueDTO(
                produtoId = produtoId,
                quantidadeFisica = 0,
                quantidadeReservada = 0,
                quantidadeDisponivel = 0,
                quantidadeMinima = 0,
            )
    }

    @Transactional
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

    @Transactional
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

        val produto = produtoRepository.buscarParaAtualizar(produtoId)
            ?: throw NotFoundException("Produto não encontrado")
        val estoque = estoqueRepository.buscarParaAtualizar(produtoId)
        val quantidadeFisica = estoque?.quantidadeFisica ?: 0
        val quantidadeReservada = estoque?.quantidadeReservada ?: 0
        val novaQuantidadeFisica =
            when (tipo) {
                TipoMovimentacaoEstoque.ENTRADA,
                TipoMovimentacaoEstoque.AJUSTE_ENTRADA,
                -> quantidadeFisica.toLong() + quantidade

                TipoMovimentacaoEstoque.SAIDA,
                TipoMovimentacaoEstoque.AJUSTE_SAIDA,
                TipoMovimentacaoEstoque.PERDA,
                -> quantidadeFisica.toLong() - quantidade
            }

        if (novaQuantidadeFisica < quantidadeReservada) {
            throw UnprocessableEntityException(
                "A movimentação não pode reduzir o estoque físico abaixo da quantidade reservada",
            )
        }
        if (novaQuantidadeFisica > Int.MAX_VALUE) {
            throw UnprocessableEntityException(
                "A quantidade física excede o limite suportado pelo estoque",
            )
        }

        estoqueRepository.save(
            estoque?.apply { this.quantidadeFisica = novaQuantidadeFisica.toInt() }
                ?: EstoqueModel(
                    produtoId = requireNotNull(produto.id),
                    quantidadeFisica = novaQuantidadeFisica.toInt(),
                ),
        )

        val novaMovimentacao =
            movimentacaoEstoqueRepository.save(
                MovimentacaoEstoque(
                    produtoId = produtoId,
                    pedidoId = pedidoId,
                    compraId = compraId,
                    funcionarioId = funcionarioId,
                    tipo = tipo,
                    quantidade = quantidade,
                    observacao = observacao,
                ).let(movimentacaoEstoqueMapper::toModel),
            )
        return movimentacaoEstoqueMapper.toDto(novaMovimentacao)
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

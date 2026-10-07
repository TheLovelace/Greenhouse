package org.greenhouse.integration.estoque

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.enums.TipoMovimentacaoEstoque
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TestEstoqueMovimentacao {
    @Test
    fun `deve registrar ajuste de entrada e atualizar estoque`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 8,
                    quantidadeReservada = 2,
                    quantidadeMinima = 3,
                )

            val movimentacao =
                fixture.useCase.registrarMovimentacao(
                    produtoId = produtoId,
                    tipo = TipoMovimentacaoEstoque.AJUSTE_ENTRADA,
                    quantidade = 2,
                    observacao = "Contagem encontrou duas unidades adicionais",
                )

            assertNotNull(movimentacao.id)
            assertNotNull(movimentacao.dataHora)
            assertEquals(produtoId, movimentacao.produtoId)
            assertEquals(TipoMovimentacaoEstoque.AJUSTE_ENTRADA, movimentacao.tipo)
            assertEquals(2, movimentacao.quantidade)
            assertEquals(
                10,
                fixture.useCase.buscarPosicaoPorProdutoId(produtoId).quantidadeFisica,
            )
            assertEquals(
                2,
                fixture.useCase.buscarPosicaoPorProdutoId(produtoId).quantidadeReservada,
            )
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve criar posicao ao registrar ajuste de entrada sem estoque previo`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId = fixture.novoProdutoSemEstoque()

            fixture.useCase.registrarMovimentacao(
                produtoId = produtoId,
                tipo = TipoMovimentacaoEstoque.AJUSTE_ENTRADA,
                quantidade = 5,
                observacao = null,
            )

            val posicao = fixture.useCase.buscarPosicaoPorProdutoId(produtoId)
            assertEquals(5, posicao.quantidadeFisica)
            assertEquals(0, posicao.quantidadeReservada)
            assertEquals(5, posicao.quantidadeDisponivel)
            assertEquals(0, posicao.quantidadeMinima)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve registrar ajuste de saida sem consumir estoque reservado`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 8,
                    quantidadeReservada = 2,
                    quantidadeMinima = 3,
                )

            fixture.useCase.registrarMovimentacao(
                produtoId = produtoId,
                tipo = TipoMovimentacaoEstoque.AJUSTE_SAIDA,
                quantidade = 3,
                observacao = "Contagem encontrou três unidades a menos",
            )

            val posicao = fixture.useCase.buscarPosicaoPorProdutoId(produtoId)
            assertEquals(5, posicao.quantidadeFisica)
            assertEquals(2, posicao.quantidadeReservada)
            assertEquals(3, posicao.quantidadeDisponivel)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar saida ou perda que ultrapasse o estoque disponivel`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 5,
                    quantidadeReservada = 2,
                    quantidadeMinima = 1,
                )

            listOf(TipoMovimentacaoEstoque.AJUSTE_SAIDA, TipoMovimentacaoEstoque.PERDA).forEach { tipo ->
                assertFailsWith<UnprocessableEntityException> {
                    fixture.useCase.registrarMovimentacao(
                        produtoId = produtoId,
                        tipo = tipo,
                        quantidade = 4,
                        observacao = "A quantidade excede o disponível",
                    )
                }
            }

            val posicao = fixture.useCase.buscarPosicaoPorProdutoId(produtoId)
            assertEquals(5, posicao.quantidadeFisica)
            assertEquals(2, posicao.quantidadeReservada)
            assertEquals(3, posicao.quantidadeDisponivel)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar tipos reservados a compras e pedidos`() {
        val fixture = EstoqueFixture()

        assertFailsWith<BadRequestException> {
            fixture.useCase.registrarMovimentacao(
                produtoId = 1,
                tipo = TipoMovimentacaoEstoque.ENTRADA,
                quantidade = 1,
                observacao = null,
            )
        }
        assertFailsWith<BadRequestException> {
            fixture.useCase.registrarMovimentacao(
                produtoId = 1,
                tipo = TipoMovimentacaoEstoque.SAIDA,
                quantidade = 1,
                observacao = null,
            )
        }
    }

    @Test
    fun `deve rejeitar quantidade invalida`() {
        val fixture = EstoqueFixture()

        assertFailsWith<BadRequestException> {
            fixture.useCase.registrarMovimentacao(
                produtoId = 1,
                tipo = TipoMovimentacaoEstoque.PERDA,
                quantidade = 0,
                observacao = null,
            )
        }
    }

    @Test
    fun `deve rejeitar produto inexistente`() {
        val fixture = EstoqueFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.registrarMovimentacao(
                produtoId = Long.MAX_VALUE,
                tipo = TipoMovimentacaoEstoque.AJUSTE_ENTRADA,
                quantidade = 1,
                observacao = null,
            )
        }
    }
}

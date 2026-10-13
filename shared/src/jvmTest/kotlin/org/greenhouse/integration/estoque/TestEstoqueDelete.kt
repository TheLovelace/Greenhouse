package org.greenhouse.integration.estoque

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestEstoqueDelete {
    @Test
    fun `deve excluir posicao sem quantidade reservada mesmo com saldo fisico`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 8,
                    quantidadeReservada = 0,
                    quantidadeMinima = 3,
                    localizacao = "Estante A-01",
                    bloqueado = true,
                )

            fixture.service.excluirPosicao(produtoId)

            assertEquals(0, fixture.service.buscarPosicaoPorProdutoId(produtoId).quantidadeFisica)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve impedir excluir posicao com quantidade reservada`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 8,
                    quantidadeReservada = 2,
                    quantidadeMinima = 1,
                )

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.excluirPosicao(produtoId)
            }
            assertEquals(8, fixture.service.buscarPosicaoPorProdutoId(produtoId).quantidadeFisica)
            assertEquals(2, fixture.service.buscarPosicaoPorProdutoId(produtoId).quantidadeReservada)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar not found quando produto nao possui posicao`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId = fixture.novoProdutoSemEstoque()

            assertFailsWith<NotFoundException> {
                fixture.service.excluirPosicao(produtoId)
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = EstoqueFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.excluirPosicao(0)
        }
    }
}

package org.greenhouse.integration.estoque

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.dto.EstoqueDTO as Estoque
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestEstoqueGet {
    @Test
    fun `deve consultar posicao com quantidade disponivel calculada`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 12,
                    quantidadeReservada = 2,
                    quantidadeMinima = 3,
                    localizacao = "Estante A-01",
                    bloqueado = true,
                )

            assertEquals(
                Estoque(
                    produtoId = produtoId,
                    quantidadeFisica = 12,
                    quantidadeReservada = 2,
                    quantidadeDisponivel = 10,
                    quantidadeMinima = 3,
                    localizacao = "Estante A-01",
                    bloqueado = true,
                ),
                fixture.service.buscarPosicaoPorProdutoId(produtoId),
            )
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar posicao zerada para produto sem registro de estoque`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId = fixture.novoProdutoSemEstoque()

            assertEquals(
                Estoque(
                    produtoId = produtoId,
                    quantidadeFisica = 0,
                    quantidadeReservada = 0,
                    quantidadeDisponivel = 0,
                    quantidadeMinima = 0,
                    localizacao = null,
                    bloqueado = false,
                ),
                fixture.service.buscarPosicaoPorProdutoId(produtoId),
            )
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar not found para produto inexistente`() {
        val fixture = EstoqueFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.buscarPosicaoPorProdutoId(Long.MAX_VALUE)
        }
    }

    @Test
    fun `deve rejeitar id de produto invalido`() {
        val fixture = EstoqueFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.buscarPosicaoPorProdutoId(0)
        }
    }
}

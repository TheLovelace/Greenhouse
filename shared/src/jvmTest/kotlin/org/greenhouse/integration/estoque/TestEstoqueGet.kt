package org.greenhouse.integration.estoque

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.dto.EstoqueDTO as Estoque
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestEstoqueGet {
    @Test
    fun `deve listar posicoes cadastradas`() {
        val fixture = EstoqueFixture()

        try {
            val primeiroProdutoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 12,
                    quantidadeReservada = 2,
                    quantidadeMinima = 3,
                    localizacao = "Estante A-01",
                    bloqueado = true,
                )
            val segundoProdutoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 8,
                    quantidadeReservada = 0,
                    quantidadeMinima = 1,
                    localizacao = "Estante B-02",
                )

            val posicoes = fixture.service.listarPosicoes()

            assertTrue(
                Estoque(
                    produtoId = primeiroProdutoId,
                    quantidadeFisica = 12,
                    quantidadeReservada = 2,
                    quantidadeDisponivel = 10,
                    quantidadeMinima = 3,
                    localizacao = "Estante A-01",
                    bloqueado = true,
                ) in posicoes,
            )
            assertTrue(
                Estoque(
                    produtoId = segundoProdutoId,
                    quantidadeFisica = 8,
                    quantidadeReservada = 0,
                    quantidadeDisponivel = 8,
                    quantidadeMinima = 1,
                    localizacao = "Estante B-02",
                    bloqueado = false,
                ) in posicoes,
            )
        } finally {
            fixture.limpar()
        }
    }

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

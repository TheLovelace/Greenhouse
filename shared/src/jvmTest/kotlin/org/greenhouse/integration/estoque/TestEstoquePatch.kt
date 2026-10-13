package org.greenhouse.integration.estoque

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TestEstoquePatch {
    @Test
    fun `deve atualizar os campos editaveis da posicao`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 12,
                    quantidadeReservada = 2,
                    quantidadeMinima = 3,
                    localizacao = "Estante antiga",
                )

            val atualizada =
                fixture.service.atualizarPosicao(
                    produtoId,
                    mapOf(
                        "quantidadeFisica" to 15,
                        "quantidadeMinima" to 4,
                        "localizacao" to "Estante nova",
                        "bloqueado" to true,
                    ),
                )

            assertEquals(15, atualizada.quantidadeFisica)
            assertEquals(2, atualizada.quantidadeReservada)
            assertEquals(13, atualizada.quantidadeDisponivel)
            assertEquals(4, atualizada.quantidadeMinima)
            assertEquals("Estante nova", atualizada.localizacao)
            assertEquals(true, atualizada.bloqueado)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve permitir limpar localizacao e criar posicao para produto existente`() {
        val fixture = EstoqueFixture()

        try {
            val produtoComPosicao =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 8,
                    quantidadeReservada = 0,
                    quantidadeMinima = 1,
                    localizacao = "Estante",
                )
            assertNull(
                fixture.service
                    .atualizarPosicao(produtoComPosicao, mapOf("localizacao" to null))
                    .localizacao,
            )

            val produtoSemPosicao = fixture.novoProdutoSemEstoque()
            val novaPosicao =
                fixture.service.atualizarPosicao(
                    produtoSemPosicao,
                    mapOf("bloqueado" to true),
                )

            assertEquals(0, novaPosicao.quantidadeFisica)
            assertEquals(0, novaPosicao.quantidadeReservada)
            assertEquals(true, novaPosicao.bloqueado)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacoes invalidas`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 8,
                    quantidadeReservada = 3,
                    quantidadeMinima = 1,
                )

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarPosicao(produtoId, mapOf("quantidadeFisica" to 2))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarPosicao(produtoId, mapOf("quantidadeMinima" to -1))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarPosicao(produtoId, mapOf("localizacao" to "a".repeat(101)))
            }
            assertFailsWith<BadRequestException> {
                fixture.service.atualizarPosicao(produtoId, mapOf("quantidadeFisica" to 2.5))
            }
            assertFailsWith<BadRequestException> {
                fixture.service.atualizarPosicao(produtoId, mapOf("campo" to 1))
            }
            assertFailsWith<BadRequestException> {
                fixture.service.atualizarPosicao(produtoId, emptyMap())
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar not found ao atualizar estoque de produto inexistente`() {
        val fixture = EstoqueFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizarPosicao(Long.MAX_VALUE, mapOf("bloqueado" to true))
        }
    }
}

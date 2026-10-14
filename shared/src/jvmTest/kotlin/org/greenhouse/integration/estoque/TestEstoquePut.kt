package org.greenhouse.integration.estoque

import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.EstoqueUpdateDTO
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestEstoquePut {
    @Test
    fun `deve substituir os campos editaveis e preservar a quantidade reservada`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 12,
                    quantidadeReservada = 2,
                    quantidadeMinima = 3,
                    localizacao = "Estante antiga",
                    bloqueado = false,
                )

            val atualizada =
                fixture.service.substituirPosicao(
                    produtoId,
                    EstoqueUpdateDTO(
                        quantidadeFisica = 15,
                        quantidadeMinima = 4,
                        localizacao = "Estante nova",
                        bloqueado = true,
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
    fun `deve permitir limpar a localizacao`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 5,
                    quantidadeReservada = 0,
                    quantidadeMinima = 1,
                    localizacao = "Estante",
                )

            val atualizada =
                fixture.service.substituirPosicao(
                    produtoId,
                    EstoqueUpdateDTO(
                        quantidadeFisica = 5,
                        quantidadeMinima = 1,
                        localizacao = null,
                        bloqueado = false,
                    ),
                )

            assertEquals(null, atualizada.localizacao)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar quantidade fisica menor que reserva`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 8,
                    quantidadeReservada = 3,
                    quantidadeMinima = 1,
                )

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.substituirPosicao(
                    produtoId,
                    EstoqueUpdateDTO(
                        quantidadeFisica = 2,
                        quantidadeMinima = 1,
                        localizacao = null,
                        bloqueado = false,
                    ),
                )
            }
            assertEquals(8, fixture.service.buscarPosicaoPorProdutoId(produtoId).quantidadeFisica)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar not found sem posicao existente`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId = fixture.novoProdutoSemEstoque()

            assertFailsWith<NotFoundException> {
                fixture.service.substituirPosicao(
                    produtoId,
                    EstoqueUpdateDTO(
                        quantidadeFisica = 5,
                        quantidadeMinima = 1,
                        localizacao = null,
                        bloqueado = false,
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }
}

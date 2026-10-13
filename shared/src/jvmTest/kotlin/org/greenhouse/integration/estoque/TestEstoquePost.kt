package org.greenhouse.integration.estoque

import org.greenhouse.adapter.ConflictException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.EstoqueCreateDTO
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestEstoquePost {
    @Test
    fun `deve criar posicao com os campos informados`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId = fixture.novoProdutoSemEstoque()

            val criado =
                fixture.service.criarPosicao(
                    EstoqueCreateDTO(
                        produtoId = produtoId,
                        quantidadeFisica = 12,
                        quantidadeMinima = 3,
                        localizacao = "Estante A-01",
                        bloqueado = true,
                    ),
                )

            assertEquals(produtoId, criado.produtoId)
            assertEquals(12, criado.quantidadeFisica)
            assertEquals(0, criado.quantidadeReservada)
            assertEquals(12, criado.quantidadeDisponivel)
            assertEquals(3, criado.quantidadeMinima)
            assertEquals("Estante A-01", criado.localizacao)
            assertEquals(true, criado.bloqueado)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve criar posicao com valores padrao`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId = fixture.novoProdutoSemEstoque()

            val criado = fixture.service.criarPosicao(EstoqueCreateDTO(produtoId))

            assertEquals(0, criado.quantidadeFisica)
            assertEquals(0, criado.quantidadeReservada)
            assertEquals(0, criado.quantidadeMinima)
            assertEquals(null, criado.localizacao)
            assertEquals(false, criado.bloqueado)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar criacao duplicada`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 4,
                    quantidadeReservada = 0,
                    quantidadeMinima = 0,
                )

            assertFailsWith<ConflictException> {
                fixture.service.criarPosicao(EstoqueCreateDTO(produtoId))
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar quantidades e localizacao invalidas`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId = fixture.novoProdutoSemEstoque()

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.criarPosicao(EstoqueCreateDTO(produtoId, quantidadeFisica = -1))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.criarPosicao(EstoqueCreateDTO(produtoId, quantidadeMinima = -1))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.criarPosicao(EstoqueCreateDTO(produtoId, localizacao = "x".repeat(101)))
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar not found para produto inexistente`() {
        val fixture = EstoqueFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.criarPosicao(EstoqueCreateDTO(Long.MAX_VALUE))
        }
    }
}

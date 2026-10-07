package org.greenhouse.integration.desconto

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.integration.fixture.DescontoFixture
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TestDescontoPatch {
    @Test
    fun `deve atualizar parcialmente e refletir alteracoes na consulta`() {
        val fixture = DescontoFixture()

        try {
            val id = fixture.novoDesconto()
            val atualizado =
                fixture.useCase.atualizar(
                    id,
                    mapOf(
                        "titulo" to "Título atualizado",
                    ),
                )

            assertEquals(id, atualizado.id)
            assertEquals("Título atualizado", atualizado.titulo)
            assertEquals(true, atualizado.ativo)
            assertEquals(TipoDesconto.PERCENTUAL, atualizado.tipo)
            assertEquals(BigDecimal("10.00"), atualizado.valor)

            val consultado = fixture.useCase.buscarPorId(id)
            assertEquals("Título atualizado", consultado.titulo)
            assertEquals(true, consultado.ativo)
            assertEquals(id, consultado.id)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve permitir limpar descricao e datas opcionais`() {
        val fixture = DescontoFixture()

        try {
            val id =
                fixture.novoDesconto(
                    dataInicio = OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                    dataFim = OffsetDateTime.parse("2026-12-31T23:59:59Z"),
                )
            val atualizado =
                fixture.useCase.atualizar(
                    id,
                    mapOf(
                        "descricao" to null,
                        "dataInicio" to null,
                        "dataFim" to null,
                    ),
                )

            assertNull(atualizado.descricao)
            assertNull(atualizado.dataInicio)
            assertNull(atualizado.dataFim)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacao de desconto inexistente`() {
        val fixture = DescontoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.atualizar(Long.MAX_VALUE, mapOf("titulo" to "Atualizado"))
        }
    }

    @Test
    fun `deve rejeitar id invalido e corpo sem campos editaveis`() {
        val fixture = DescontoFixture()

        assertFailsWith<BadRequestException> {
            fixture.useCase.atualizar(0, mapOf("titulo" to "Atualizado"))
        }
        assertFailsWith<BadRequestException> {
            fixture.useCase.atualizar(1, mapOf("id" to 10))
        }
    }

    @Test
    fun `deve aceitar atualizar status e rejeitar campos e tipos incorretos`() {
        val fixture = DescontoFixture()
        val id = fixture.novoDesconto()

        try {
            val inativado = fixture.useCase.atualizar(id, mapOf("ativo" to false))
            assertEquals(false, inativado.ativo)

            val reativado = fixture.useCase.atualizar(id, mapOf("ativo" to true))
            assertEquals(true, reativado.ativo)

            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(id, mapOf("campo" to "valor"))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(id, mapOf("titulo" to 123))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(id, mapOf("ativo" to "false"))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(id, mapOf("dataInicio" to "data inválida"))
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve reaplicar validacoes de tipo valor titulo e periodo`() {
        val fixture = DescontoFixture()
        val id = fixture.novoDesconto()

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(id, mapOf("titulo" to " "))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(id, mapOf("tipo" to "INVALIDO"))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(id, mapOf("valor" to BigDecimal("100.01")))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(
                    id,
                    mapOf(
                        "dataInicio" to "2026-02-01T00:00:00Z",
                        "dataFim" to "2026-01-31T23:59:59Z",
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }
}

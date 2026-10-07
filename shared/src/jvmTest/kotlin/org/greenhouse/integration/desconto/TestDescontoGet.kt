package org.greenhouse.integration.desconto

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.integration.fixture.DescontoFixture
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TestDescontoGet {
    @Test
    fun `deve listar descontos ativos e inativos`() {
        val fixture = DescontoFixture()
        val dataInicio = OffsetDateTime.parse("2026-01-01T00:00:00-03:00")
        val dataFim = OffsetDateTime.parse("2026-12-31T23:59:59-03:00")
        var idsCriados = emptyList<Long>()

        try {
            idsCriados =
                listOf(
                    fixture.novoDesconto(
                        tipo = TipoDesconto.PERCENTUAL,
                        valor = BigDecimal("10.00"),
                        ativo = true,
                        dataInicio = dataInicio,
                        dataFim = dataFim,
                    ),
                    fixture.novoDesconto(
                        tipo = TipoDesconto.VALOR_FIXO,
                        valor = BigDecimal("15.00"),
                        ativo = false,
                    ),
                )

            val descontos = fixture.useCase.listar().filter { it.id in idsCriados }

            assertEquals(2, descontos.size)
            assertEquals(setOf(true, false), descontos.map { it.ativo }.toSet())
            val percentual = descontos.single { it.id == idsCriados[0] }
            assertEquals(TipoDesconto.PERCENTUAL, percentual.tipo)
            assertEquals(BigDecimal("10.00"), percentual.valor)
            assertEquals(dataInicio.toInstant(), assertNotNull(percentual.dataInicio).toInstant())
            assertEquals(dataFim.toInstant(), assertNotNull(percentual.dataFim).toInstant())
            assertEquals(TipoDesconto.VALOR_FIXO, descontos.single { it.id == idsCriados[1] }.tipo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve buscar desconto pelo id`() {
        val fixture = DescontoFixture()

        try {
            val id = fixture.novoDesconto(tipo = TipoDesconto.VALOR_FIXO, valor = BigDecimal("15.00"))

            val desconto = fixture.useCase.buscarPorId(id)

            assertEquals(id, desconto.id)
            assertEquals(TipoDesconto.VALOR_FIXO, desconto.tipo)
            assertEquals(BigDecimal("15.00"), desconto.valor)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = DescontoFixture()

        assertFailsWith<BadRequestException> {
            fixture.useCase.buscarPorId(0)
        }
    }

    @Test
    fun `deve retornar not found para desconto inexistente`() {
        val fixture = DescontoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.buscarPorId(Long.MAX_VALUE)
        }
    }
}

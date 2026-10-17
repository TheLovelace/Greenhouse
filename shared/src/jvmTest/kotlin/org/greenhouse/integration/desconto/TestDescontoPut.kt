package org.greenhouse.integration.desconto

import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.DescontoRequestDTO
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.integration.fixture.DescontoFixture
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TestDescontoPut {
    @Test
    fun `deve substituir todos os campos do desconto`() {
        val fixture = DescontoFixture()

        try {
            val id = fixture.novoDesconto()
            val substituido =
                fixture.service.substituir(
                    id,
                    DescontoRequestDTO(
                        titulo = "Oferta atualizada",
                        descricao = null,
                        tipo = TipoDesconto.VALOR_FIXO,
                        valor = BigDecimal("25.00"),
                        dataInicio = OffsetDateTime.parse("2026-11-01T00:00:00Z"),
                        dataFim = null,
                        ativo = false,
                        quantidadeMinimaItens = 4,
                    ),
                )

            assertEquals(id, substituido.id)
            assertEquals("Oferta atualizada", substituido.titulo)
            assertNull(substituido.descricao)
            assertEquals(TipoDesconto.VALOR_FIXO, substituido.tipo)
            assertEquals(BigDecimal("25.00"), substituido.valor)
            assertEquals(OffsetDateTime.parse("2026-11-01T00:00:00Z"), substituido.dataInicio)
            assertNull(substituido.dataFim)
            assertEquals(false, substituido.ativo)
            assertEquals(4, substituido.quantidadeMinimaItens)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar substituicao para desconto inexistente`() {
        val fixture = DescontoFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.substituir(
                Long.MAX_VALUE,
                DescontoRequestDTO(
                    titulo = "Oferta",
                    tipo = TipoDesconto.PERCENTUAL,
                    valor = BigDecimal("10.00"),
                ),
            )
        }
    }

    @Test
    fun `deve validar os dados da substituicao`() {
        val fixture = DescontoFixture()

        try {
            val id = fixture.novoDesconto()

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.substituir(
                    id,
                    DescontoRequestDTO(
                        titulo = "Oferta",
                        tipo = TipoDesconto.PERCENTUAL,
                        valor = BigDecimal("100.01"),
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }
}

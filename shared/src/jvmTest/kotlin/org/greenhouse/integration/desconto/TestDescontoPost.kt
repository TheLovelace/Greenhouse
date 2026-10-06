package org.greenhouse.integration.desconto

import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Desconto
import org.greenhouse.entity.TipoDesconto
import org.greenhouse.integration.fixture.DescontoFixture
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestDescontoPost {
    @Test
    fun `deve criar e persistir desconto percentual com vigencia`() {
        val fixture = DescontoFixture()
        val dataInicio = OffsetDateTime.parse("2026-01-01T00:00:00-03:00")
        val dataFim = OffsetDateTime.parse("2026-12-31T23:59:59-03:00")

        try {
            val criado =
                fixture.criarDesconto(
                    Desconto(
                        id = -1,
                        titulo = "Promoção de verão",
                        descricao = "Desconto percentual de teste",
                        tipo = TipoDesconto.PERCENTUAL,
                        valor = BigDecimal("10.00"),
                        dataInicio = dataInicio,
                        dataFim = dataFim,
                    ),
                )

            val id = assertNotNull(criado.id)
            assertEquals("Promoção de verão", criado.titulo)
            assertEquals(TipoDesconto.PERCENTUAL, criado.tipo)
            assertEquals(BigDecimal("10.00"), criado.valor)
            assertEquals(dataInicio.toInstant(), assertNotNull(criado.dataInicio).toInstant())
            assertEquals(dataFim.toInstant(), assertNotNull(criado.dataFim).toInstant())
            assertEquals(true, criado.ativo)

            val persistido = fixture.useCase.buscarPorId(id)
            assertEquals(criado.id, persistido.id)
            assertEquals(criado.titulo, persistido.titulo)
            assertEquals(criado.descricao, persistido.descricao)
            assertEquals(criado.tipo, persistido.tipo)
            assertEquals(criado.valor, persistido.valor)
            assertEquals(dataInicio.toInstant(), assertNotNull(persistido.dataInicio).toInstant())
            assertEquals(dataFim.toInstant(), assertNotNull(persistido.dataFim).toInstant())
            assertEquals(criado.ativo, persistido.ativo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve criar desconto de valor fixo inativo`() {
        val fixture = DescontoFixture()

        try {
            val criado =
                fixture.criarDesconto(
                    Desconto(
                        titulo = "Desconto fixo",
                        descricao = null,
                        tipo = TipoDesconto.VALOR_FIXO,
                        valor = BigDecimal("15.00"),
                        dataInicio = null,
                        dataFim = null,
                        ativo = false,
                    ),
                )

            assertEquals(TipoDesconto.VALOR_FIXO, criado.tipo)
            assertEquals(BigDecimal("15.00"), criado.valor)
            assertNull(criado.descricao)
            assertEquals(false, criado.ativo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar titulo vazio ou maior que o limite`() {
        val fixture = DescontoFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(descontoValido(titulo = " "))
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(descontoValido(titulo = "a".repeat(151)))
        }
    }

    @Test
    fun `deve rejeitar valores fora das regras do tipo`() {
        val fixture = DescontoFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(descontoValido(valor = BigDecimal.ZERO))
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(descontoValido(valor = BigDecimal("100.01")))
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(
                descontoValido(tipo = TipoDesconto.VALOR_FIXO, valor = BigDecimal("0.01").negate()),
            )
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(descontoValido(valor = BigDecimal("10.001")))
        }
    }

    @Test
    fun `deve rejeitar periodo com data final anterior a inicial`() {
        val fixture = DescontoFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(
                descontoValido(
                    dataInicio = OffsetDateTime.parse("2026-02-01T00:00:00Z"),
                    dataFim = OffsetDateTime.parse("2026-01-31T23:59:59Z"),
                ),
            )
        }
    }

    private fun descontoValido(
        titulo: String = "Desconto válido",
        tipo: TipoDesconto = TipoDesconto.PERCENTUAL,
        valor: BigDecimal = BigDecimal("10.00"),
        dataInicio: OffsetDateTime? = null,
        dataFim: OffsetDateTime? = null,
    ) = Desconto(
        titulo = titulo,
        descricao = null,
        tipo = tipo,
        valor = valor,
        dataInicio = dataInicio,
        dataFim = dataFim,
    )
}

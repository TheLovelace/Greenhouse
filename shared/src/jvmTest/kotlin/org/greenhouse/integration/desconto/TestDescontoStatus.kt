package org.greenhouse.integration.desconto

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.integration.fixture.DescontoFixture
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestDescontoStatus {
    @Test
    fun `deve inativar e reativar desconto preservando os dados`() {
        val fixture = DescontoFixture()

        try {
            val id =
                fixture.novoDesconto(
                    tipo = TipoDesconto.VALOR_FIXO,
                    valor = BigDecimal("15.00"),
                    ativo = true,
                )
            val original = fixture.service.buscarPorId(id)

            val inativado = fixture.service.atualizar(id, mapOf("ativo" to false))
            assertEquals(false, inativado.ativo)
            assertEquals(id, inativado.id)
            assertEquals(original.copy(ativo = false), inativado)

            val reativado = fixture.service.atualizar(id, mapOf("ativo" to true))
            assertEquals(true, reativado.ativo)
            assertEquals(id, reativado.id)
            assertEquals(original, reativado)

            assertEquals(original, fixture.service.buscarPorId(id))
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar not found para desconto inexistente`() {
        val fixture = DescontoFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(Long.MAX_VALUE, mapOf("ativo" to false))
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = DescontoFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.atualizar(0, mapOf("ativo" to false))
        }
    }
}

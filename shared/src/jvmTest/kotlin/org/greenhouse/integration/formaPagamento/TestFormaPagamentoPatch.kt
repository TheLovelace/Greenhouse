package org.greenhouse.integration.formaPagamento

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.FormaPagamentoFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestFormaPagamentoPatch {
    @Test
    fun `deve atualizar apenas os campos enviados`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()
            val id = requireNotNull(criada.id)
            val atualizada = fixture.useCase.atualizar(id, mapOf("nome" to "PIX"))

            assertEquals(criada.id, atualizada.id)
            assertEquals("PIX", atualizada.nome)
            assertEquals(criada.ativo, atualizada.ativo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve permitir atualizar status e rejeitar campos invalidos`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()
            val id = requireNotNull(criada.id)
            val inativada = fixture.useCase.atualizar(id, mapOf("ativo" to false))

            assertEquals(false, inativada.ativo)
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(id, mapOf("campo" to "valor"))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(id, mapOf("nome" to 123))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(id, mapOf("ativo" to "false"))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(id, mapOf("nome" to "  "))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(id, mapOf("id" to 10))
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacao de forma de pagamento inexistente`() {
        val fixture = FormaPagamentoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.atualizar(Long.MAX_VALUE, mapOf("nome" to "PIX"))
        }
    }
}

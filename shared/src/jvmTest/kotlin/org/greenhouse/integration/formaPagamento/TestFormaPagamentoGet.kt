package org.greenhouse.integration.formaPagamento

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.FormaPagamentoFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestFormaPagamentoGet {
    @Test
    fun `deve buscar forma de pagamento pelo id`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()

            assertEquals(criada, fixture.service.buscarPorId(criada.id!!))
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve listar formas ativas e inativas`() {
        val fixture = FormaPagamentoFixture()

        try {
            val ativa = fixture.novaFormaPagamento()
            val inativa = fixture.novaFormaPagamento(ativo = false)
            val listadas = fixture.service.listar()

            assertEquals(true, listadas.any { it.id == ativa.id && it.ativo })
            assertEquals(true, listadas.any { it.id == inativa.id && !it.ativo })
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar id invalido e forma inexistente`() {
        val fixture = FormaPagamentoFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.buscarPorId(0)
        }
        assertFailsWith<NotFoundException> {
            fixture.service.buscarPorId(Long.MAX_VALUE)
        }
    }
}

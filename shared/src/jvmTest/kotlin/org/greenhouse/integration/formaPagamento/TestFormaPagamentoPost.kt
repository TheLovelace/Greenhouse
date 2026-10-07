package org.greenhouse.integration.formaPagamento

import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.FormaPagamento
import org.greenhouse.integration.fixture.FormaPagamentoFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class TestFormaPagamentoPost {
    @Test
    fun `deve criar forma de pagamento ativa por padrao`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()

            assertEquals(true, criada.ativo)
            assertEquals(true, criada.id != null)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve ignorar id informado na criacao`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento(id = -1)

            assertNotEquals(-1L, criada.id)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar nome vazio ou maior que o limite da tabela`() {
        val fixture = FormaPagamentoFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(FormaPagamento(nome = " "))
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(FormaPagamento(nome = "a".repeat(51)))
        }
    }
}

package org.greenhouse.integration.formaPagamento

import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.integration.fixture.FormaPagamentoFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TestFormaPagamentoPost {
    @Test
    fun `deve criar forma de pagamento ativa por padrao`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada =
                fixture.novaFormaPagamento(
                    descricao = "Pagamento à vista",
                    limiteParcelas = 3,
                    prazoEstornoDias = 14,
                )

            assertEquals(true, criada.ativo)
            assertEquals(true, criada.id != null)
            assertEquals("Pagamento à vista", criada.descricao)
            assertEquals(3, criada.limiteParcelas)
            assertEquals(14, criada.prazoEstornoDias)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve gerar id persistido na criacao`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()

            assertNotNull(criada.id)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar nome vazio ou maior que o limite da tabela`() {
        val fixture = FormaPagamentoFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(FormaPagamentoRequestDTO(nome = " "))
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(FormaPagamentoRequestDTO(nome = "a".repeat(51)))
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(FormaPagamentoRequestDTO(nome = "PIX", limiteParcelas = 0))
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(FormaPagamentoRequestDTO(nome = "PIX", prazoEstornoDias = -1))
        }
        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(FormaPagamentoRequestDTO(nome = "PIX", descricao = "a".repeat(251)))
        }
    }
}

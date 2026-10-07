package org.greenhouse.integration.formaPagamento

import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.FormaPagamentoFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestFormaPagamentoDelete {
    @Test
    fun `deve inativar sem remover registro ou alterar nome`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()
            val id = requireNotNull(criada.id)
            fixture.useCase.excluir(id)

            assertEquals(criada.copy(ativo = false), fixture.useCase.buscarPorId(id))
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar not found ao inativar forma inexistente`() {
        val fixture = FormaPagamentoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.excluir(Long.MAX_VALUE)
        }
    }
}

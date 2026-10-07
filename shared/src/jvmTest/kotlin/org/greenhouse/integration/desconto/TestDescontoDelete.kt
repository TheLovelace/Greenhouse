package org.greenhouse.integration.desconto

import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.DescontoFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestDescontoDelete {
    @Test
    fun `deve inativar desconto sem remover registro ou alterar dados`() {
        val fixture = DescontoFixture()

        try {
            val id = fixture.novoDesconto()
            val original = fixture.useCase.buscarPorId(id)

            fixture.useCase.excluir(id)

            assertEquals(original.copy(ativo = false), fixture.useCase.buscarPorId(id))
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve retornar not found ao inativar desconto inexistente`() {
        val fixture = DescontoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.excluir(Long.MAX_VALUE)
        }
    }
}

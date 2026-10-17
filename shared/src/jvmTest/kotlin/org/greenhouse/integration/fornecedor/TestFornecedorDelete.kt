package org.greenhouse.integration.fornecedor

import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.FornecedorFixture

class TestFornecedorDelete {

    @Test
    fun `deve excluir fornecedor existente`() {
        val fixture = FornecedorFixture()
        val criado = fixture.novoFornecedor()
        val id = criado.id!!

        fixture.service.excluir(id)

        assertFailsWith<NotFoundException> {
            fixture.service.buscarPorId(id)
        }
    }

    @Test
    fun `deve lancar not found ao excluir fornecedor inexistente`() {
        val fixture = FornecedorFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.excluir(Long.MAX_VALUE)
        }
    }
}
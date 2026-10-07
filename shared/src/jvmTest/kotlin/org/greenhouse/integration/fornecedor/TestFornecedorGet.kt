package org.greenhouse.integration.fornecedor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.FornecedorFixture

class TestFornecedorGet {

    @Test
    fun `deve buscar fornecedor pelo id`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoFornecedor()
            id = criado.id

            assertEquals(
                criado,
                fixture.useCase.buscarPorId(criado.id!!),
            )
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve listar fornecedores`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoFornecedor()
            id = criado.id

            assertEquals(
                true,
                fixture.useCase.listar().any { it.id == criado.id },
            )
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = FornecedorFixture()

        assertFailsWith<BadRequestException> {
            fixture.useCase.buscarPorId(0)
        }
    }

    @Test
    fun `deve lancar not found para fornecedor inexistente`() {
        val fixture = FornecedorFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.buscarPorId(Long.MAX_VALUE)
        }
    }
}
package org.greenhouse.integration.fornecedor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.FornecedorFixture
import java.util.UUID

class TestFornecedorGet {

    @Test
    fun `deve buscar fornecedor pelo id`() {
        val fixture = FornecedorFixture()

        try {
            val criado = fixture.novoFornecedor()

            assertEquals(criado, fixture.service.buscarPorId(criado.id))
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve listar fornecedores`() {
        val fixture = FornecedorFixture()

        try {
            val criado = fixture.novoFornecedor()

            assertTrue(fixture.service.listar().any { it.id == criado.id })
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve buscar fornecedores por parte do nome`() {
        val fixture = FornecedorFixture()
        val marcador = "Marcador${UUID.randomUUID().toString().take(8)}"

        try {
            val encontrado = fixture.novoFornecedor(nome = "$marcador Flora")
            fixture.novoFornecedor(nome = "Outro nome qualquer")

            val resultados = fixture.service.listar(marcador.lowercase())

            assertEquals(listOf(encontrado.id), resultados.map { it.id })
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = FornecedorFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.buscarPorId(0)
        }
    }

    @Test
    fun `deve lancar not found para fornecedor inexistente`() {
        val fixture = FornecedorFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.buscarPorId(Long.MAX_VALUE)
        }
    }
}

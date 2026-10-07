package org.greenhouse.integration.cliente

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.ClienteFixture

class TestClienteGet {

    @Test
    fun `deve buscar cliente pelo id`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            assertEquals(criado, fixture.useCase.buscarPorId(criado.id!!))
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve listar clientes ativos e inativos`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id
            fixture.useCase.atualizar(criado.id!!, mapOf("ativo" to false))

            assertEquals(true, fixture.useCase.listar().any { it.id == criado.id })
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve filtrar clientes por status ativo`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id
            fixture.useCase.atualizar(criado.id!!, mapOf("ativo" to false))

            assertEquals(false, fixture.useCase.listar(ativo = true).any { it.id == criado.id })
            assertEquals(true, fixture.useCase.listar(ativo = false).any { it.id == criado.id })
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve localizar cliente por trecho do nome`() {
        val fixture = ClienteFixture()
        var id: Long? = null
        val token = fixture.tokenUnico()

        try {
            val criado = fixture.novoCliente(nome = "Maria $token Silva")
            id = criado.id

            assertEquals(true, fixture.useCase.listar(nome = token.lowercase()).any { it.id == criado.id })
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve localizar cliente por trecho do telefone`() {
        val fixture = ClienteFixture()
        var id: Long? = null
        val telefone = fixture.digitosUnicos()

        try {
            val criado = fixture.novoCliente(telefone = telefone)
            id = criado.id

            assertEquals(true, fixture.useCase.listar(telefone = telefone.take(8)).any { it.id == criado.id })
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve retornar lista vazia quando nenhum cliente for encontrado`() {
        val fixture = ClienteFixture()

        val encontrados = fixture.useCase.listar(nome = "Inexistente ${fixture.tokenUnico()}")

        assertEquals(0, encontrados.size)
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = ClienteFixture()

        assertFailsWith<BadRequestException> {
            fixture.useCase.buscarPorId(0)
        }
    }

    @Test
    fun `deve lancar not found para cliente inexistente`() {
        val fixture = ClienteFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.buscarPorId(Long.MAX_VALUE)
        }
    }
}

package org.greenhouse.integration.cliente

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Cliente
import org.greenhouse.integration.fixture.ClienteFixture

class TestClientePost {

    @Test
    fun `deve criar cliente ativo por padrao`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            assertEquals(true, criado.ativo)
            assertEquals(true, criado.id != null)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve ignorar id e ativo informados na criacao`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.useCase.criar(
                Cliente(id = -1, nome = "Cliente com id", telefone = "11999990000", ativo = false),
            )
            id = criado.id

            assertNotEquals(-1L, criado.id)
            assertEquals(true, criado.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve criar cliente com whatsapp`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente(whatsapp = true)
            id = criado.id

            assertEquals(true, criado.whatsapp)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar nome em branco`() {
        val fixture = ClienteFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(Cliente(nome = "   ", telefone = "11999990000"))
        }
    }

    @Test
    fun `deve rejeitar nome maior que o limite da tabela`() {
        val fixture = ClienteFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(Cliente(nome = "a".repeat(151), telefone = "11999990000"))
        }
    }

    @Test
    fun `deve rejeitar telefone em branco`() {
        val fixture = ClienteFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(Cliente(nome = "Cliente sem telefone", telefone = " "))
        }
    }

    @Test
    fun `deve rejeitar telefone maior que o limite da tabela`() {
        val fixture = ClienteFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(Cliente(nome = "Cliente telefone longo", telefone = "1".repeat(21)))
        }
    }

    @Test
    fun `deve rejeitar cpf com formato invalido`() {
        val fixture = ClienteFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.novoCliente(cpf = "123")
        }
    }

    @Test
    fun `deve rejeitar cpf duplicado`() {
        val fixture = ClienteFixture()
        var id: Long? = null
        val cpf = fixture.digitosUnicos()

        try {
            id = fixture.novoCliente(cpf = cpf).id

            assertFailsWith<BadRequestException> {
                fixture.novoCliente(cpf = cpf)
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar email invalido`() {
        val fixture = ClienteFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.novoCliente(email = "email-invalido")
        }
    }
}

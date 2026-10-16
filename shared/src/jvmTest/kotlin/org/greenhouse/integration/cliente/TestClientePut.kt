package org.greenhouse.integration.cliente

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.ClienteUpdateRequestDTO
import org.greenhouse.integration.fixture.ClienteFixture

class TestClientePut {

    @Test
    fun `deve atualizar apenas campos enviados`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            val atualizado =
                fixture.service.atualizar(criado.id, ClienteUpdateRequestDTO(nome = "Nome Atualizado"))

            assertEquals(criado.id, atualizado.id)
            assertEquals("Nome Atualizado", atualizado.nome)
            assertEquals(criado.telefone, atualizado.telefone)
            assertEquals(criado.ativo, atualizado.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve aceitar objeto completo na atualizacao`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            val atualizado =
                fixture.service.atualizar(
                    criado.id,
                    ClienteUpdateRequestDTO(
                        id = criado.id,
                        nome = "Nome Completo",
                        telefone = "11888887777",
                        whatsapp = true,
                        ativo = false,
                    ),
                )

            assertEquals(criado.id, atualizado.id)
            assertEquals("Nome Completo", atualizado.nome)
            assertEquals("11888887777", atualizado.telefone)
            assertEquals(true, atualizado.whatsapp)
            assertEquals(false, atualizado.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve inativar e reativar cliente`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            val inativado = fixture.service.atualizar(criado.id, ClienteUpdateRequestDTO(ativo = false))
            assertEquals(false, inativado.ativo)

            val reativado = fixture.service.atualizar(criado.id, ClienteUpdateRequestDTO(ativo = true))
            assertEquals(true, reativado.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve limpar cpf e email com string vazia`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado =
                fixture.novoCliente(
                    cpf = fixture.digitosUnicos(),
                    email = "cliente@greenhouse.com",
                )
            id = criado.id

            val atualizado =
                fixture.service.atualizar(
                    criado.id,
                    ClienteUpdateRequestDTO(cpf = "", email = ""),
                )

            assertNull(atualizado.cpf)
            assertNull(atualizado.email)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve manter cpf proprio sem erro de duplicidade`() {
        val fixture = ClienteFixture()
        var id: Long? = null
        val cpf = fixture.digitosUnicos()

        try {
            val criado = fixture.novoCliente(cpf = cpf)
            id = criado.id

            val atualizado = fixture.service.atualizar(criado.id, ClienteUpdateRequestDTO(cpf = cpf))

            assertEquals(cpf, atualizado.cpf)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar cpf ja usado por outro cliente`() {
        val fixture = ClienteFixture()
        var idA: Long? = null
        var idB: Long? = null
        val cpf = fixture.digitosUnicos()

        try {
            val clienteA = fixture.novoCliente(cpf = cpf)
            idA = clienteA.id
            val clienteB = fixture.novoCliente()
            idB = clienteB.id

            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(clienteB.id, ClienteUpdateRequestDTO(cpf = cpf))
            }
        } finally {
            fixture.limpar(idA)
            fixture.limpar(idB)
        }
    }

    @Test
    fun `deve rejeitar nome em branco na atualizacao`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(criado.id, ClienteUpdateRequestDTO(nome = "   "))
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar atualizacao sem campos`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(criado.id, ClienteUpdateRequestDTO())
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = ClienteFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.atualizar(0, ClienteUpdateRequestDTO(nome = "Nome"))
        }
    }

    @Test
    fun `deve lancar not found ao atualizar cliente inexistente`() {
        val fixture = ClienteFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(Long.MAX_VALUE, ClienteUpdateRequestDTO(nome = "Nome"))
        }
    }
}

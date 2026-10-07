package org.greenhouse.integration.cliente

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.ClienteFixture

class TestClientePatch {

    @Test
    fun `deve atualizar apenas campos enviados`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            val atualizado = fixture.service.atualizar(criado.id!!, mapOf("nome" to "Nome Atualizado"))

            assertEquals(criado.id, atualizado.id)
            assertEquals("Nome Atualizado", atualizado.nome)
            assertEquals(criado.telefone, atualizado.telefone)
            assertEquals(criado.ativo, atualizado.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve aceitar entidade completa na atualizacao`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id
            val idCliente = criado.id!!

            val atualizado = fixture.service.atualizar(
                idCliente,
                mapOf(
                    "id" to idCliente,
                    "nome" to "Nome Completo",
                    "telefone" to "11888887777",
                    "whatsapp" to true,
                    "ativo" to false,
                ),
            )

            assertEquals(idCliente, atualizado.id)
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

            val inativado = fixture.service.atualizar(criado.id!!, mapOf("ativo" to false))
            assertEquals(false, inativado.ativo)

            val reativado = fixture.service.atualizar(criado.id!!, mapOf("ativo" to true))
            assertEquals(true, reativado.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve limpar cpf e email com null`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente(
                cpf = fixture.digitosUnicos(),
                email = "cliente@greenhouse.com",
            )
            id = criado.id

            val atualizado = fixture.service.atualizar(
                criado.id!!,
                mapOf("cpf" to null, "email" to null),
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

            val atualizado = fixture.service.atualizar(criado.id!!, mapOf("cpf" to cpf))

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
            idA = fixture.novoCliente(cpf = cpf).id
            idB = fixture.novoCliente().id

            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(idB!!, mapOf("cpf" to cpf))
            }
        } finally {
            fixture.limpar(idA)
            fixture.limpar(idB)
        }
    }

    @Test
    fun `deve rejeitar campo desconhecido`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(criado.id!!, mapOf("endereco" to "Rua X"))
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
                fixture.service.atualizar(criado.id!!, emptyMap())
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar campos com tipo invalido`() {
        val fixture = ClienteFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoCliente()
            id = criado.id

            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(criado.id!!, mapOf("ativo" to "false"))
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = ClienteFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.atualizar(0, mapOf("nome" to "Nome"))
        }
    }

    @Test
    fun `deve lancar not found ao atualizar cliente inexistente`() {
        val fixture = ClienteFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(Long.MAX_VALUE, mapOf("nome" to "Nome"))
        }
    }
}

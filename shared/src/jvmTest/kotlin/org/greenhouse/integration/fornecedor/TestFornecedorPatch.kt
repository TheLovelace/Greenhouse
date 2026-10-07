package org.greenhouse.integration.fornecedor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.FornecedorFixture

class TestFornecedorPatch {

    @Test
    fun `deve atualizar apenas campos enviados`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoFornecedor()
            id = criado.id

            val atualizado =
                fixture.service.atualizar(
                    criado.id!!,
                    mapOf("nome" to "Novo fornecedor"),
                )

            assertEquals(criado.id, atualizado.id)
            assertEquals("Novo fornecedor", atualizado.nome)

            assertEquals(criado.cnpj, atualizado.cnpj)
            assertEquals(criado.telefone, atualizado.telefone)
            assertEquals(criado.email, atualizado.email)
            assertEquals(criado.contato, atualizado.contato)
            assertEquals(criado.ativo, atualizado.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve aceitar entidade completa na atualizacao`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoFornecedor()
            id = criado.id
            val idFornecedor = criado.id!!

            val atualizado =
                fixture.service.atualizar(
                    idFornecedor,
                    mapOf(
                        "id" to idFornecedor,
                        "nome" to "Fornecedor atualizado",
                        "cnpj" to "12345678901234",
                        "telefone" to "11988887777",
                        "email" to "novo@greenhouse.com",
                        "contato" to "Maria",
                        "ativo" to false,
                    ),
                )

            assertEquals(idFornecedor, atualizado.id)
            assertEquals("Fornecedor atualizado", atualizado.nome)
            assertEquals("12345678901234", atualizado.cnpj)
            assertEquals("11988887777", atualizado.telefone)
            assertEquals("novo@greenhouse.com", atualizado.email)
            assertEquals("Maria", atualizado.contato)
            assertEquals(false, atualizado.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve permitir remover campo opcional com null`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado =
                fixture.novoFornecedor(
                    telefone = "11999999999",
                )

            id = criado.id

            val atualizado =
                fixture.service.atualizar(
                    criado.id!!,
                    mapOf("telefone" to null),
                )

            assertNull(atualizado.telefone)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar campos com tipo invalido`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoFornecedor()
            id = criado.id

            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(
                    criado.id!!,
                    mapOf("ativo" to "false"),
                )
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar campo desconhecido`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoFornecedor()
            id = criado.id

            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(
                    criado.id!!,
                    mapOf("endereco" to "Rua teste"),
                )
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar cnpj invalido na atualizacao`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoFornecedor()
            id = criado.id

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(
                    criado.id!!,
                    mapOf("cnpj" to "123"),
                )
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve lancar not found ao atualizar fornecedor inexistente`() {
        val fixture = FornecedorFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(
                Long.MAX_VALUE,
                mapOf("nome" to "Novo fornecedor"),
            )
        }
    }
}
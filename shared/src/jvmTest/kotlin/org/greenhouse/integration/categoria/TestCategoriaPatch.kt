package org.greenhouse.integration.categoria

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.CategoriaFixture

class TestCategoriaPatch {

    @Test
    fun `deve atualizar apenas campos enviados`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria()
            id = criada.id

            val atualizada = fixture.service.atualizar(criada.id!!, mapOf("nome" to "Folhagens"))

            assertEquals(criada.id, atualizada.id)
            assertEquals("Folhagens", atualizada.nome)
            assertEquals(criada.descricao, atualizada.descricao)
            assertEquals(criada.ativo, atualizada.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve aceitar entidade completa na atualizacao`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria()
            id = criada.id
            val idCategoria = criada.id!!

            val atualizada = fixture.service.atualizar(
                idCategoria,
                mapOf(
                    "id" to idCategoria,
                    "nome" to "Folhagens",
                    "descricao" to "Plantas ornamentais",
                    "ativo" to false,
                ),
            )

            assertEquals(idCategoria, atualizada.id)
            assertEquals("Folhagens", atualizada.nome)
            assertEquals("Plantas ornamentais", atualizada.descricao)
            assertEquals(false, atualizada.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar campos com tipo invalido`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria()
            id = criada.id

            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(criada.id!!, mapOf("ativo" to "false"))
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve lancar not found ao atualizar categoria inexistente`() {
        val fixture = CategoriaFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(Long.MAX_VALUE, mapOf("nome" to "Folhagens"))
        }
    }
}
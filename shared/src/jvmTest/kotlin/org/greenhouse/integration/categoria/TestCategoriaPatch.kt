package org.greenhouse.integration.categoria

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.entity.Categoria
import org.greenhouse.integration.fixture.CategoriaFixture

class TestCategoriaPatch {

    @Test
    fun `deve atualizar categoria existente`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria()
            id = criada.id

            val atualizada = fixture.useCase.atualizar(
                criada.id!!,
                Categoria(nome = "Folhagens", descricao = "Plantas ornamentais", ativo = false),
            )

            assertEquals(criada.id, atualizada.id)
            assertEquals("Folhagens", atualizada.nome)
            assertEquals(false, atualizada.ativo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve lancar not found ao atualizar categoria inexistente`() {
        val fixture = CategoriaFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.atualizar(
                Long.MAX_VALUE,
                Categoria(nome = "Folhagens", descricao = "Plantas ornamentais"),
            )
        }
    }
}
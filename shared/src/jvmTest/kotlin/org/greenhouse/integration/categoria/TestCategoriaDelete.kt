package org.greenhouse.integration.categoria

import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.CategoriaFixture

class TestCategoriaDelete {

    @Test
    fun `deve excluir categoria existente`() {
        val fixture = CategoriaFixture()
        val criada = fixture.novaCategoria()
        val id = criada.id!!

        fixture.service.excluir(id)

        assertFailsWith<NotFoundException> {
            fixture.service.buscarPorId(id)
        }
    }

    @Test
    fun `deve lancar not found ao excluir categoria inexistente`() {
        val fixture = CategoriaFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.excluir(Long.MAX_VALUE)
        }
    }
}
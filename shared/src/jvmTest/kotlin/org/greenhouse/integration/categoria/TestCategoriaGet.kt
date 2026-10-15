package org.greenhouse.integration.categoria

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.CategoriaFixture
import java.util.UUID

class TestCategoriaGet {

    @Test
    fun `deve buscar categoria pelo id`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria()
            id = criada.id

            assertEquals(criada, fixture.service.buscarPorId(criada.id!!))
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve listar categorias`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria()
            id = criada.id

            assertEquals(true, fixture.service.listar().any { it.id == criada.id })
        } finally {
            fixture.limpar(id)
        }

        @Test
        fun `deve buscar categorias por correspondencia parcial e sem diferenciar maiusculas`() {
            val fixture = CategoriaFixture()
            val termo = "AbC${UUID.randomUUID()}"
            var id: Long? = null

            try {
                val criada = fixture.novaCategoria(nome = "Categoria $termo")
                id = criada.id

                assertEquals(listOf(criada), fixture.service.listar("aBc"))
                assertEquals(listOf(criada), fixture.service.listar(termo.substring(0, 3)))
            } finally {
                fixture.limpar(id)
            }
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = CategoriaFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.buscarPorId(0)
        }
    }

    @Test
    fun `deve lancar not found para categoria inexistente`() {
        val fixture = CategoriaFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.buscarPorId(Long.MAX_VALUE)
        }
    }
}
package org.greenhouse.integration.categoria

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.CategoriaDTO as Categoria
import org.greenhouse.integration.fixture.CategoriaFixture

class TestCategoriaPost {

    @Test
    fun `deve criar categoria ativa por padrao`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria()
            id = criada.id

            assertEquals(true, criada.ativo)
            assertEquals(true, criada.id != null)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve ignorar id informado na criacao`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.service.criar(
                Categoria(id = -1, nome = "Categoria com id", descricao = "Descrição de teste"),
            )
            id = criada.id

            assertNotEquals(-1L, criada.id)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar nome maior que o limite da tabela`() {
        val fixture = CategoriaFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(Categoria(nome = "a".repeat(101), descricao = "Descrição"))
        }
    }
}
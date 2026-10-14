package org.greenhouse.integration.categoria

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.CategoriaCreateRequestDTO
import org.greenhouse.integration.fixture.CategoriaFixture
import java.time.LocalDate
import java.util.UUID

class TestCategoriaPost {

    @Test
    fun `deve criar categoria ativa por padrao`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria()
            id = criada.id

            assertTrue(criada.nome.startsWith("Categoria "))
            assertEquals("Descrição de teste", criada.descricao)
            assertEquals(true, criada.ativo)
            assertTrue(criada.id > 0)
            assertEquals(null, criada.observacao)
            assertEquals(criada, fixture.service.buscarPorId(criada.id!!))
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve criar categoria com observacao e data informadas`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val data = LocalDate.of(2026, 1, 2)
            val criada = fixture.novaCategoria(
                nome = "Categoria ${UUID.randomUUID()}",
                observacao = "Observação",
                dataCadastro = data,
            )
            id = criada.id

            assertEquals("Observação", criada.observacao)
            assertEquals(data, criada.dataCadastro)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar nome maior que o limite da tabela`() {
        val fixture = CategoriaFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(
                CategoriaCreateRequestDTO(nome = "a".repeat(101), descricao = "Descrição"),
            )
        }
    }

    @Test
    fun `deve rejeitar nome de categoria duplicado`() {
        val fixture = CategoriaFixture()
        val nome = "Categoria ${UUID.randomUUID()}"
        var id: Long? = null

        try {
            id = fixture.novaCategoria(nome = nome).id
            assertFailsWith<UnprocessableEntityException> {
                fixture.novaCategoria(nome = nome)
            }
        } finally {
            fixture.limpar(id)
        }
    }
}
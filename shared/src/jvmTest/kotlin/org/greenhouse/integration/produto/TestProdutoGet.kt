package org.greenhouse.integration.produto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.ProdutoFixture

class TestProdutoGet {

    @Test
    fun `deve buscar produto pelo id`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto()
            id = criado.id

            assertEquals(criado, fixture.useCase.buscarPorId(criado.id!!))
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve listar produtos`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto()
            id = criado.id

            assertTrue(fixture.useCase.listar().any { it.id == criado.id })
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar id invalido`() {
        val fixture = ProdutoFixture()

        assertFailsWith<BadRequestException> {
            fixture.useCase.buscarPorId(0)
        }
    }

    @Test
    fun `deve lancar not found para produto inexistente`() {
        val fixture = ProdutoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.buscarPorId(Long.MAX_VALUE)
        }
    }
}

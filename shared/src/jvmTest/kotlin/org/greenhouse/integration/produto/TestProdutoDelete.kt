package org.greenhouse.integration.produto

import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.ProdutoFixture

class TestProdutoDelete {

    @Test
    fun `deve excluir produto sem dependencias`() {
        val fixture = ProdutoFixture()
        val criado = fixture.novoProduto()
        val id = criado.id!!

        fixture.useCase.excluir(id)

        assertFailsWith<NotFoundException> {
            fixture.useCase.buscarPorId(id)
        }
    }

    @Test
    fun `deve impedir excluir produto com estoque`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto()
            id = criado.id
            fixture.adicionarEstoque(criado.id!!)

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.excluir(criado.id!!)
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve lancar not found ao excluir produto inexistente`() {
        val fixture = ProdutoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.excluir(Long.MAX_VALUE)
        }
    }
}

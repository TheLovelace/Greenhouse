package org.greenhouse.integration.produto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.ProdutoFixture
import java.math.BigDecimal

class TestProdutoPatch {

    @Test
    fun `deve atualizar apenas campos enviados`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto()
            id = criado.id

            val atualizado = fixture.useCase.atualizar(criado.id!!, mapOf("nome" to "Rosa branca"))

            assertEquals(criado.id, atualizado.id)
            assertEquals("Rosa branca", atualizado.nome)
            assertEquals(criado.codigo, atualizado.codigo)
            assertEquals(criado.preco, atualizado.preco)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve aceitar limpar campos opcionais`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto()
            id = criado.id

            val atualizado =
                fixture.useCase.atualizar(
                    criado.id!!,
                    mapOf("codigo" to null, "descricao" to null, "preco" to BigDecimal("20.00")),
                )

            assertEquals(null, atualizado.codigo)
            assertEquals(null, atualizado.descricao)
            assertEquals(BigDecimal("20.00"), atualizado.preco)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar campo com tipo invalido e preco negativo`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto()
            id = criado.id

            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(criado.id!!, mapOf("ativo" to "false"))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(criado.id!!, mapOf("preco" to BigDecimal("-1.00")))
            }
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve lancar not found ao atualizar produto inexistente`() {
        val fixture = ProdutoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.atualizar(Long.MAX_VALUE, mapOf("nome" to "Rosa branca"))
        }
    }
}

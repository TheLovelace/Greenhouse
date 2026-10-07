package org.greenhouse.integration.produto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Produto
import org.greenhouse.integration.fixture.ProdutoFixture
import java.math.BigDecimal
import java.util.UUID

class TestProdutoPost {

    @Test
    fun `deve criar produto ativo por padrao`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto()
            id = criado.id

            assertEquals(true, criado.ativo)
            assertEquals(true, criado.id != null)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve ignorar id informado na criacao`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado =
                fixture.useCase.criar(
                    Produto(
                        id = -1,
                        codigo = "SKU-${UUID.randomUUID()}",
                        nome = "Produto com ID informado",
                        preco = BigDecimal("15.00"),
                    ),
                )
            id = criado.id

            assertNotEquals(-1L, criado.id)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar preco negativo`() {
        val fixture = ProdutoFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(
                Produto(nome = "Produto inválido", preco = BigDecimal("-0.01")),
            )
        }
    }

    @Test
    fun `deve rejeitar codigo duplicado`() {
        val fixture = ProdutoFixture()
        val codigo = "SKU-${UUID.randomUUID()}"
        var id: Long? = null

        try {
            id = fixture.novoProduto(codigo = codigo).id

            assertFailsWith<UnprocessableEntityException> {
                fixture.novoProduto(codigo = codigo)
            }
        } finally {
            fixture.limpar(id)
        }
    }
}

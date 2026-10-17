package org.greenhouse.integration.produto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.ProdutoCreateRequestDTO
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

            assertEquals("Descrição de teste", criado.descricao)
            assertEquals(BigDecimal("12.50"), criado.preco)
            assertEquals(true, criado.ativo)
            assertTrue(criado.id > 0)
            assertEquals(null, criado.prazoPreparoMinutos)
            assertEquals(criado, fixture.service.buscarPorId(criado.id!!))
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve criar produto com prazo de preparo informado`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto(prazoPreparoMinutos = 25)
            id = criado.id

            assertEquals(25, criado.prazoPreparoMinutos)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar preco negativo`() {
        val fixture = ProdutoFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(
                ProdutoCreateRequestDTO(nome = "Produto inválido", preco = BigDecimal("-0.01")),
            )
        }
    }

    @Test
    fun `deve rejeitar preco acima da precisao da coluna`() {
        val fixture = ProdutoFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.service.criar(
                ProdutoCreateRequestDTO(
                    nome = "Produto inválido",
                    preco = BigDecimal("10000000000.00"),
                ),
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

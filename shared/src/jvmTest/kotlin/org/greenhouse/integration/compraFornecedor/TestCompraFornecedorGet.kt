package org.greenhouse.integration.compraFornecedor

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraItemRequestDTO
import org.greenhouse.integration.fixture.CompraFornecedorFixture
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestCompraFornecedorGet {
    @Test
    fun `deve buscar compra pelo id com seus itens`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoA = fixture.novoProdutoComEstoque(quantidadeFisica = 0)
        val produtoB = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val criada =
                fixture.criarCompra(
                    CompraFornecedorCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        fornecedorId = fornecedorId,
                        formaPagamentoId = formaPagamentoId,
                        itens =
                            listOf(
                                CompraItemRequestDTO(produtoId = produtoA, quantidade = 1, valorUnitario = BigDecimal("10.00")),
                                CompraItemRequestDTO(produtoId = produtoB, quantidade = 2, valorUnitario = BigDecimal("5.00")),
                            ),
                    ),
                )

            val encontrada = fixture.service.buscarPorId(criada.id)

            assertEquals(criada.id, encontrada.id)
            assertEquals(2, encontrada.itens.size)
            assertEquals(BigDecimal("20.00"), encontrada.valorTotal)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve listar compras`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val criada =
                fixture.criarCompra(
                    CompraFornecedorCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        fornecedorId = fornecedorId,
                        formaPagamentoId = formaPagamentoId,
                        itens =
                            listOf(
                                CompraItemRequestDTO(produtoId = produtoId, quantidade = 1, valorUnitario = BigDecimal("10.00")),
                            ),
                    ),
                )

            assertTrue(fixture.service.listar().any { it.id == criada.id })
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve buscar compras por parte da observacao`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)
        val marcador = "Marcador${UUID.randomUUID().toString().take(8)}"

        try {
            val encontrada =
                fixture.criarCompra(
                    CompraFornecedorCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        fornecedorId = fornecedorId,
                        formaPagamentoId = formaPagamentoId,
                        observacao = "Reposição $marcador de vasos",
                        itens =
                            listOf(
                                CompraItemRequestDTO(produtoId = produtoId, quantidade = 1, valorUnitario = BigDecimal("10.00")),
                            ),
                    ),
                )
            fixture.criarCompra(
                CompraFornecedorCreateRequestDTO(
                    funcionarioId = funcionarioId,
                    fornecedorId = fornecedorId,
                    formaPagamentoId = formaPagamentoId,
                    observacao = "Observação sem o marcador",
                    itens =
                        listOf(
                            CompraItemRequestDTO(produtoId = produtoId, quantidade = 1, valorUnitario = BigDecimal("10.00")),
                        ),
                ),
            )

            val resultados = fixture.service.listar(marcador.lowercase())

            assertEquals(listOf(encontrada.id), resultados.map { it.id })
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar id invalido e lancar not found para compra inexistente`() {
        val fixture = CompraFornecedorFixture()

        assertFailsWith<BadRequestException> {
            fixture.service.buscarPorId(0)
        }
        assertFailsWith<NotFoundException> {
            fixture.service.buscarPorId(Long.MAX_VALUE)
        }
    }
}

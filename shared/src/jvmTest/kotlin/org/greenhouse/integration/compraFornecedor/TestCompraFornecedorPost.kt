package org.greenhouse.integration.compraFornecedor

import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraItemRequestDTO
import org.greenhouse.enums.StatusCompra
import org.greenhouse.integration.fixture.CompraFornecedorFixture
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestCompraFornecedorPost {
    @Test
    fun `deve criar compra pedida com itens e valor total calculado`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val compra =
                fixture.criarCompra(
                    CompraFornecedorCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        fornecedorId = fornecedorId,
                        formaPagamentoId = formaPagamentoId,
                        valorFrete = BigDecimal("5.00"),
                        itens =
                            listOf(
                                CompraItemRequestDTO(
                                    produtoId = produtoId,
                                    quantidade = 3,
                                    valorUnitario = BigDecimal("10.00"),
                                ),
                            ),
                    ),
                )

            assertNotNull(compra.id)
            assertEquals(StatusCompra.PEDIDA, compra.status)
            assertEquals(BigDecimal("35.00"), compra.valorTotal)
            assertEquals(1, compra.itens.size)
            assertEquals(produtoId, compra.itens.first().produtoId)

            val persistida = fixture.service.buscarPorId(compra.id)
            assertEquals(compra.copy(dataCompra = persistida.dataCompra), persistida)
            assertTrue(
                kotlin.math.abs(
                    compra.dataCompra.toInstant().toEpochMilli() -
                        persistida.dataCompra.toInstant().toEpochMilli(),
                ) < 1000,
            )
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve nao somar frete ao total quando frete incluso`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val compra =
                fixture.criarCompra(
                    CompraFornecedorCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        fornecedorId = fornecedorId,
                        formaPagamentoId = formaPagamentoId,
                        valorFrete = BigDecimal("5.00"),
                        freteIncluso = true,
                        itens =
                            listOf(
                                CompraItemRequestDTO(
                                    produtoId = produtoId,
                                    quantidade = 2,
                                    valorUnitario = BigDecimal("10.00"),
                                ),
                            ),
                    ),
                )

            assertEquals(BigDecimal("20.00"), compra.valorTotal)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar compra sem itens`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    CompraFornecedorCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        fornecedorId = fornecedorId,
                        formaPagamentoId = formaPagamentoId,
                        itens = emptyList(),
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar quantidade e valor invalidos`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    compraValida(fixture, funcionarioId, fornecedorId, formaPagamentoId, produtoId)
                        .copy(
                            itens =
                                listOf(
                                    CompraItemRequestDTO(
                                        produtoId = produtoId,
                                        quantidade = 0,
                                        valorUnitario = BigDecimal("10.00"),
                                    ),
                                ),
                        ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    compraValida(fixture, funcionarioId, fornecedorId, formaPagamentoId, produtoId)
                        .copy(
                            itens =
                                listOf(
                                    CompraItemRequestDTO(
                                        produtoId = produtoId,
                                        quantidade = 1,
                                        valorUnitario = BigDecimal("-1.00"),
                                    ),
                                ),
                        ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    compraValida(fixture, funcionarioId, fornecedorId, formaPagamentoId, produtoId)
                        .copy(valorFrete = BigDecimal("-0.01")),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar produto duplicado na mesma compra`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    compraValida(fixture, funcionarioId, fornecedorId, formaPagamentoId, produtoId)
                        .copy(
                            itens =
                                listOf(
                                    CompraItemRequestDTO(
                                        produtoId = produtoId,
                                        quantidade = 1,
                                        valorUnitario = BigDecimal("10.00"),
                                    ),
                                    CompraItemRequestDTO(
                                        produtoId = produtoId,
                                        quantidade = 2,
                                        valorUnitario = BigDecimal("12.00"),
                                    ),
                                ),
                        ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar relacionamentos inexistentes`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    compraValida(fixture, Long.MAX_VALUE, fornecedorId, formaPagamentoId, produtoId),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    compraValida(fixture, funcionarioId, Long.MAX_VALUE, formaPagamentoId, produtoId),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    compraValida(fixture, funcionarioId, fornecedorId, Long.MAX_VALUE, produtoId),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarCompra(
                    compraValida(fixture, funcionarioId, fornecedorId, formaPagamentoId, Long.MAX_VALUE),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    private fun compraValida(
        fixture: CompraFornecedorFixture,
        funcionarioId: Long,
        fornecedorId: Long,
        formaPagamentoId: Long,
        produtoId: Long,
    ) = CompraFornecedorCreateRequestDTO(
        funcionarioId = funcionarioId,
        fornecedorId = fornecedorId,
        formaPagamentoId = formaPagamentoId,
        itens =
            listOf(
                CompraItemRequestDTO(
                    produtoId = produtoId,
                    quantidade = 1,
                    valorUnitario = BigDecimal("10.00"),
                ),
            ),
    )
}

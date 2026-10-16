package org.greenhouse.integration.compraFornecedor

import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.CompraFornecedorUpdateRequestDTO
import org.greenhouse.dto.request.CompraItemRequestDTO
import org.greenhouse.enums.StatusCompra
import org.greenhouse.integration.fixture.CompraFornecedorFixture
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestCompraFornecedorPut {
    @Test
    fun `deve atualizar campos e recalcular total ao trocar itens`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val compra = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))

            val atualizada =
                fixture.service.atualizar(
                    compra.id,
                    CompraFornecedorUpdateRequestDTO(
                        observacao = "Compra renegociada",
                        dataEntregaPrevista = LocalDate.now().plusDays(10),
                        valorFrete = BigDecimal("10.00"),
                        itens =
                            listOf(
                                CompraItemRequestDTO(
                                    produtoId = produtoId,
                                    quantidade = 4,
                                    valorUnitario = BigDecimal("7.50"),
                                ),
                            ),
                    ),
                )

            assertEquals("Compra renegociada", atualizada.observacao)
            assertEquals(LocalDate.now().plusDays(10), atualizada.dataEntregaPrevista)
            assertEquals(BigDecimal("40.00"), atualizada.valorTotal)
            assertEquals(1, atualizada.itens.size)
            assertEquals(4, atualizada.itens.first().quantidade)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve recalcular total ao alterar frete sem trocar itens`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val compra =
                fixture.criarCompra(
                    novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId)
                        .copy(valorFrete = BigDecimal("5.00")),
                )
            assertEquals(BigDecimal("15.00"), compra.valorTotal)

            val comFreteIncluso =
                fixture.service.atualizar(
                    compra.id,
                    CompraFornecedorUpdateRequestDTO(freteIncluso = true),
                )
            assertEquals(BigDecimal("10.00"), comFreteIncluso.valorTotal)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacao de compra recebida ou cancelada`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val recebida = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))
            fixture.service.atualizarStatus(recebida.id, StatusCompra.RECEBIDA)
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(
                    recebida.id,
                    CompraFornecedorUpdateRequestDTO(observacao = "Não deve alterar"),
                )
            }

            val cancelada = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))
            fixture.service.atualizarStatus(cancelada.id, StatusCompra.CANCELADA)
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(
                    cancelada.id,
                    CompraFornecedorUpdateRequestDTO(observacao = "Não deve alterar"),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve lancar not found ao atualizar compra inexistente`() {
        val fixture = CompraFornecedorFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(
                Long.MAX_VALUE,
                CompraFornecedorUpdateRequestDTO(observacao = "Teste"),
            )
        }
    }

    private fun novaCompra(
        funcionarioId: Long,
        fornecedorId: Long,
        formaPagamentoId: Long,
        produtoId: Long,
    ) = org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO(
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

package org.greenhouse.integration.compraFornecedor

import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraItemRequestDTO
import org.greenhouse.enums.StatusCompra
import org.greenhouse.integration.fixture.CompraFornecedorFixture
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TestCompraFornecedorDelete {
    @Test
    fun `deve excluir compra pedida e seus itens`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val compra = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))

            fixture.service.excluir(compra.id)

            assertFailsWith<NotFoundException> {
                fixture.service.buscarPorId(compra.id)
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar exclusao de compra recebida`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            val compra = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))
            fixture.service.atualizarStatus(compra.id, StatusCompra.RECEBIDA)

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.excluir(compra.id)
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve lancar not found ao excluir compra inexistente`() {
        val fixture = CompraFornecedorFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.excluir(Long.MAX_VALUE)
        }
    }

    private fun novaCompra(
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

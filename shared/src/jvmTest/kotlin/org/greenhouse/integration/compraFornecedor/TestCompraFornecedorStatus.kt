package org.greenhouse.integration.compraFornecedor

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraItemRequestDTO
import org.greenhouse.enums.StatusCompra
import org.greenhouse.integration.fixture.CompraFornecedorFixture
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestCompraFornecedorStatus {
    @Test
    fun `deve receber compra e incrementar a quantidade fisica do estoque`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoA = fixture.novoProdutoComEstoque(quantidadeFisica = 10, quantidadeReservada = 2)
        val produtoB = fixture.novoProdutoSemEstoque()

        try {
            val compra =
                fixture.criarCompra(
                    CompraFornecedorCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        fornecedorId = fornecedorId,
                        formaPagamentoId = formaPagamentoId,
                        itens =
                            listOf(
                                CompraItemRequestDTO(produtoId = produtoA, quantidade = 5, valorUnitario = BigDecimal("10.00")),
                                CompraItemRequestDTO(produtoId = produtoB, quantidade = 3, valorUnitario = BigDecimal("7.00")),
                            ),
                    ),
                )

            val recebida = fixture.service.atualizarStatus(compra.id, StatusCompra.RECEBIDA)

            assertEquals(StatusCompra.RECEBIDA, recebida.status)

            val posicaoA = fixture.posicaoEstoque(produtoA)
            assertEquals(15, posicaoA.quantidadeFisica)
            assertEquals(2, posicaoA.quantidadeReservada)

            val posicaoB = fixture.posicaoEstoque(produtoB)
            assertEquals(3, posicaoB.quantidadeFisica)
            assertEquals(0, posicaoB.quantidadeReservada)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar recebimento duplicado da mesma compra`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val compra = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))
            fixture.service.atualizarStatus(compra.id, StatusCompra.RECEBIDA)

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarStatus(compra.id, StatusCompra.RECEBIDA)
            }
            assertEquals(11, fixture.posicaoEstoque(produtoId).quantidadeFisica)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve cancelar compra pedida sem tocar no estoque`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10, quantidadeReservada = 1)

        try {
            val compra = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))

            val cancelada = fixture.service.atualizarStatus(compra.id, StatusCompra.CANCELADA)

            assertEquals(StatusCompra.CANCELADA, cancelada.status)
            val posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(10, posicao.quantidadeFisica)
            assertEquals(1, posicao.quantidadeReservada)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar recebimento de compra cancelada`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val compra = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))
            fixture.service.atualizarStatus(compra.id, StatusCompra.CANCELADA)

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarStatus(compra.id, StatusCompra.RECEBIDA)
            }
            assertEquals(10, fixture.posicaoEstoque(produtoId).quantidadeFisica)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar transicoes invalidas e estados terminais`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val compra = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarStatus(compra.id, StatusCompra.PEDIDA)
            }

            fixture.service.atualizarStatus(compra.id, StatusCompra.RECEBIDA)
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarStatus(compra.id, StatusCompra.CANCELADA)
            }

            val cancelada = fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))
            fixture.service.atualizarStatus(cancelada.id, StatusCompra.CANCELADA)
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarStatus(cancelada.id, StatusCompra.PEDIDA)
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve fazer rollback do recebimento quando um item falha`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoOk = fixture.novoProdutoComEstoque(quantidadeFisica = 10)
        val produtoLotado = fixture.novoProdutoComEstoque(quantidadeFisica = 5)

        try {
            val compra =
                fixture.criarCompra(
                    CompraFornecedorCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        fornecedorId = fornecedorId,
                        formaPagamentoId = formaPagamentoId,
                        itens =
                            listOf(
                                CompraItemRequestDTO(produtoId = produtoOk, quantidade = 4, valorUnitario = BigDecimal("10.00")),
                                CompraItemRequestDTO(produtoId = produtoLotado, quantidade = 1, valorUnitario = BigDecimal("10.00")),
                            ),
                    ),
                )

            // Simula estoque no limite do INTEGER para forçar falha no segundo item.
            fixture.elevarEstoqueAoLimite(produtoLotado)

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizarStatus(compra.id, StatusCompra.RECEBIDA)
            }

            // O incremento do primeiro item e a mudança de status devem ser revertidos.
            assertEquals(StatusCompra.PEDIDA, fixture.service.buscarPorId(compra.id).status)
            assertEquals(10, fixture.posicaoEstoque(produtoOk).quantidadeFisica)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar exclusao de fornecedor vinculado a compra`() {
        val fixture = CompraFornecedorFixture()
        val funcionarioId = fixture.novoFuncionario()
        val fornecedorId = fixture.novoFornecedor()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 0)

        try {
            fixture.criarCompra(novaCompra(funcionarioId, fornecedorId, formaPagamentoId, produtoId))

            assertFailsWith<UnprocessableEntityException> {
                fixture.fornecedorFixture.service.excluir(fornecedorId)
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar status em compra inexistente e id invalido`() {
        val fixture = CompraFornecedorFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizarStatus(Long.MAX_VALUE, StatusCompra.RECEBIDA)
        }
        assertFailsWith<BadRequestException> {
            fixture.service.atualizarStatus(0, StatusCompra.RECEBIDA)
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

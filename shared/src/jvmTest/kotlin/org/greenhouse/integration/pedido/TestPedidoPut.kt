package org.greenhouse.integration.pedido

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.PedidoCreateRequestDTO
import org.greenhouse.dto.request.PedidoItemRequestDTO
import org.greenhouse.dto.request.PedidoUpdateRequestDTO
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.integration.fixture.PedidoFixture
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestPedidoPut {
    @Test
    fun `deve atualizar observacao e desconto de pedido aberto`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("20.00"))
        val descontoId = fixture.novoDesconto(valor = BigDecimal("10"))

        try {
            val pedido =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 2)),
                    ),
                )

            val atualizado =
                fixture.useCase.atualizar(
                    pedido.id,
                    PedidoUpdateRequestDTO(
                        observacao = "Retirar no balcão",
                        descontoId = descontoId,
                    ),
                )

            assertEquals("Retirar no balcão", atualizado.observacao)
            assertEquals(descontoId, atualizado.descontoId)
            assertEquals(BigDecimal("4.00"), atualizado.valorDesconto)
            assertEquals(BigDecimal("36.00"), atualizado.valorTotal)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve substituir itens de pedido aberto recalculando o total`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("10.00"))
        val produtoNovo = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("7.00"))

        try {
            val pedido =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 2)),
                    ),
                )

            val atualizado =
                fixture.useCase.atualizar(
                    pedido.id,
                    PedidoUpdateRequestDTO(
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoNovo, quantidade = 3)),
                    ),
                )

            assertEquals(1, atualizado.itens.size)
            assertEquals(produtoNovo, atualizado.itens.single().produtoId)
            assertEquals(BigDecimal("7.00"), atualizado.itens.single().precoUnitario)
            assertEquals(BigDecimal("21.00"), atualizado.valorTotal)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar alteracao em pedido confirmado ou concluido`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val confirmado =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            fixture.useCase.atualizarStatus(confirmado.id, StatusPedido.CONFIRMADO)

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(
                    confirmado.id,
                    PedidoUpdateRequestDTO(observacao = "tarde demais"),
                )
            }

            fixture.useCase.atualizarStatus(confirmado.id, StatusPedido.CONCLUIDO)
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(
                    confirmado.id,
                    PedidoUpdateRequestDTO(observacao = "bem tarde demais"),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacao sem campos`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val pedido =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 1)),
                    ),
                )

            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(pedido.id, PedidoUpdateRequestDTO())
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve validar retirada prevista no passado e atualizar observacao`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)
        val descontoId = fixture.novoDesconto()
        val clienteId = fixture.novoCliente()

        try {
            val agendado =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        clienteId = clienteId,
                        funcionarioId = funcionarioId,
                        descontoId = descontoId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(2),
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 1)),
                    ),
                )

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(
                    agendado.id,
                    PedidoUpdateRequestDTO(dataRetiradaPrevista = OffsetDateTime.now().minusDays(2)),
                )
            }

            val atualizado = fixture.useCase.atualizar(agendado.id, PedidoUpdateRequestDTO(observacao = "ok"))
            assertEquals("ok", atualizado.observacao)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacao de pedido inexistente`() {
        val fixture = PedidoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.atualizar(Long.MAX_VALUE, PedidoUpdateRequestDTO(observacao = "x"))
        }
        assertFailsWith<BadRequestException> {
            fixture.useCase.atualizar(0, PedidoUpdateRequestDTO(observacao = "x"))
        }
    }
}

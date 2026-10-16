package org.greenhouse.integration.pedido

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.PedidoCreateRequestDTO
import org.greenhouse.dto.request.PedidoItemRequestDTO
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.integration.fixture.PedidoFixture
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TestPedidoStatus {
    @Test
    fun `pedido imediato confirma sem reservar e conclui com baixa`() {
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
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 3)),
                    ),
                )

            val confirmado = fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONFIRMADO)
            assertEquals(StatusPedido.CONFIRMADO, confirmado.status)
            assertEquals(0, fixture.posicaoEstoque(produtoId).quantidadeReservada)

            val concluido = fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONCLUIDO)
            assertEquals(StatusPedido.CONCLUIDO, concluido.status)
            assertNotNull(concluido.dataRetirada)

            val posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(7, posicao.quantidadeFisica)
            assertEquals(0, posicao.quantidadeReservada)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `pedido agendado reserva ao confirmar mantem em pronto e baixa ao concluir`() {
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
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(2),
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 3)),
                    ),
                )

            fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONFIRMADO)
            var posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(10, posicao.quantidadeFisica)
            assertEquals(3, posicao.quantidadeReservada)

            fixture.useCase.atualizarStatus(pedido.id, StatusPedido.PRONTO_RETIRADA)
            posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(10, posicao.quantidadeFisica)
            assertEquals(3, posicao.quantidadeReservada)

            fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONCLUIDO)
            posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(7, posicao.quantidadeFisica)
            assertEquals(0, posicao.quantidadeReservada)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `pedido agendado nao pode concluir direto do confirmado`() {
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
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(2),
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 2)),
                    ),
                )
            fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONFIRMADO)

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONCLUIDO)
            }
            assertEquals(StatusPedido.CONFIRMADO, fixture.useCase.buscarPorId(pedido.id).status)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve cancelar pedido aberto sem tocar no estoque`() {
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
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(1),
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 2)),
                    ),
                )

            val cancelado = fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CANCELADO)
            assertEquals(StatusPedido.CANCELADO, cancelado.status)

            val posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(10, posicao.quantidadeFisica)
            assertEquals(0, posicao.quantidadeReservada)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve cancelar confirmado e pronto para retirada liberando a reserva`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoA = fixture.novoProdutoComEstoque(quantidadeFisica = 10)
        val produtoB = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val canceladoDeConfirmado =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(1),
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoA, quantidade = 4)),
                    ),
                )
            fixture.useCase.atualizarStatus(canceladoDeConfirmado.id, StatusPedido.CONFIRMADO)
            fixture.useCase.atualizarStatus(canceladoDeConfirmado.id, StatusPedido.CANCELADO)
            assertEquals(0, fixture.posicaoEstoque(produtoA).quantidadeReservada)
            assertEquals(10, fixture.posicaoEstoque(produtoA).quantidadeFisica)

            val canceladoDePronto =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(1),
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoB, quantidade = 4)),
                    ),
                )
            fixture.useCase.atualizarStatus(canceladoDePronto.id, StatusPedido.CONFIRMADO)
            fixture.useCase.atualizarStatus(canceladoDePronto.id, StatusPedido.PRONTO_RETIRADA)
            fixture.useCase.atualizarStatus(canceladoDePronto.id, StatusPedido.CANCELADO)

            val posicao = fixture.posicaoEstoque(produtoB)
            assertEquals(0, posicao.quantidadeReservada)
            assertEquals(10, posicao.quantidadeFisica)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar transicoes invalidas e estados terminais`() {
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

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id, StatusPedido.PRONTO_RETIRADA)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONCLUIDO)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id, StatusPedido.ABERTO)
            }

            fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONFIRMADO)
            fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONCLUIDO)
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CANCELADO)
            }

            val cancelado =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            fixture.useCase.atualizarStatus(cancelado.id, StatusPedido.CANCELADO)
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(cancelado.id, StatusPedido.CONFIRMADO)
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar reserva acima do disponivel ao confirmar agendado`() {
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
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(1),
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 3)),
                    ),
                )

            // Simula outro pedido consumindo a disponibilidade entre a criação e a confirmação.
            fixture.reservarEstoque(produtoId, 8)

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id, StatusPedido.CONFIRMADO)
            }
            assertEquals(StatusPedido.ABERTO, fixture.useCase.buscarPorId(pedido.id).status)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar status em pedido inexistente e id invalido`() {
        val fixture = PedidoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.atualizarStatus(Long.MAX_VALUE, StatusPedido.CONFIRMADO)
        }
        assertFailsWith<BadRequestException> {
            fixture.useCase.atualizarStatus(0, StatusPedido.CONFIRMADO)
        }
    }
}

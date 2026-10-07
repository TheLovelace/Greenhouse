package org.greenhouse.integration.pedido

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Pedido
import org.greenhouse.entity.PedidoItem
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.enums.TipoMovimentacaoEstoque
import org.greenhouse.integration.fixture.PedidoFixture
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPedidoStatus {
    @Test
    fun `pedido imediato confirma sem reservar e conclui com baixa e saida`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 3)),
                    ),
                )

            val confirmado = fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONFIRMADO)
            assertEquals(StatusPedido.CONFIRMADO, confirmado.status)
            assertEquals(0, fixture.posicaoEstoque(produtoId).quantidadeReservada)

            val concluido = fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONCLUIDO)
            assertEquals(StatusPedido.CONCLUIDO, concluido.status)
            assertNotNull(concluido.dataRetirada)

            val posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(7, posicao.quantidadeFisica)
            assertEquals(0, posicao.quantidadeReservada)

            val movimentacoes = fixture.movimentacoesDoPedido(pedido.id!!)
            assertEquals(1, movimentacoes.size)
            val saida = movimentacoes.single()
            assertEquals(TipoMovimentacaoEstoque.SAIDA, saida.tipo)
            assertEquals(3, saida.quantidade)
            assertEquals(produtoId, saida.produtoId)
            assertEquals(funcionarioId, saida.funcionarioId)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `pedido agendado reserva ao confirmar mantem em pronto e baixa ao concluir`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(2),
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 3)),
                    ),
                )

            fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONFIRMADO)
            var posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(10, posicao.quantidadeFisica)
            assertEquals(3, posicao.quantidadeReservada)

            fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.PRONTO_RETIRADA)
            posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(10, posicao.quantidadeFisica)
            assertEquals(3, posicao.quantidadeReservada)
            assertTrue(fixture.movimentacoesDoPedido(pedido.id!!).isEmpty())

            fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONCLUIDO)
            posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(7, posicao.quantidadeFisica)
            assertEquals(0, posicao.quantidadeReservada)

            val movimentacoes = fixture.movimentacoesDoPedido(pedido.id!!)
            assertEquals(1, movimentacoes.size)
            assertEquals(TipoMovimentacaoEstoque.SAIDA, movimentacoes.single().tipo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `pedido agendado nao pode concluir direto do confirmado`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(2),
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 2)),
                    ),
                )
            fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONFIRMADO)

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONCLUIDO)
            }
            assertEquals(StatusPedido.CONFIRMADO, fixture.useCase.buscarPorId(pedido.id!!).status)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve cancelar pedido aberto sem tocar no estoque`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(1),
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 2)),
                    ),
                )

            val cancelado = fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CANCELADO)
            assertEquals(StatusPedido.CANCELADO, cancelado.status)

            val posicao = fixture.posicaoEstoque(produtoId)
            assertEquals(10, posicao.quantidadeFisica)
            assertEquals(0, posicao.quantidadeReservada)
            assertTrue(fixture.movimentacoesDoPedido(pedido.id!!).isEmpty())
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve cancelar confirmado e pronto para retirada liberando a reserva`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoA = fixture.novoProdutoComEstoque(quantidadeFisica = 10)
        val produtoB = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val canceladoDeConfirmado =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(1),
                        itens = listOf(PedidoItem(produtoId = produtoA, quantidade = 4)),
                    ),
                )
            fixture.useCase.atualizarStatus(canceladoDeConfirmado.id!!, StatusPedido.CONFIRMADO)
            fixture.useCase.atualizarStatus(canceladoDeConfirmado.id!!, StatusPedido.CANCELADO)
            assertEquals(0, fixture.posicaoEstoque(produtoA).quantidadeReservada)
            assertEquals(10, fixture.posicaoEstoque(produtoA).quantidadeFisica)

            val canceladoDePronto =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(1),
                        itens = listOf(PedidoItem(produtoId = produtoB, quantidade = 4)),
                    ),
                )
            fixture.useCase.atualizarStatus(canceladoDePronto.id!!, StatusPedido.CONFIRMADO)
            fixture.useCase.atualizarStatus(canceladoDePronto.id!!, StatusPedido.PRONTO_RETIRADA)
            fixture.useCase.atualizarStatus(canceladoDePronto.id!!, StatusPedido.CANCELADO)

            val posicao = fixture.posicaoEstoque(produtoB)
            assertEquals(0, posicao.quantidadeReservada)
            assertEquals(10, posicao.quantidadeFisica)
            assertTrue(fixture.movimentacoesDoPedido(canceladoDePronto.id!!).isEmpty())
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar transicoes invalidas e estados terminais`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.PRONTO_RETIRADA)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONCLUIDO)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.ABERTO)
            }

            fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONFIRMADO)
            fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONCLUIDO)
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CANCELADO)
            }

            val cancelado =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            fixture.useCase.atualizarStatus(cancelado.id!!, StatusPedido.CANCELADO)
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(cancelado.id!!, StatusPedido.CONFIRMADO)
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar reserva acima do disponivel ao confirmar agendado`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(1),
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 3)),
                    ),
                )

            // Simula outro pedido consumindo a disponibilidade entre a criação e a confirmação.
            fixture.reservarEstoque(produtoId, 8)

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizarStatus(pedido.id!!, StatusPedido.CONFIRMADO)
            }
            assertEquals(StatusPedido.ABERTO, fixture.useCase.buscarPorId(pedido.id!!).status)
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

package org.greenhouse.integration.estoque

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestEstoquePedidoOps {
    @Test
    fun `deve validar disponibilidade e reservar saldo de pedido agendado`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 10,
                    quantidadeReservada = 1,
                    quantidadeMinima = 2,
                )

            fixture.service.validarDisponibilidade(produtoId, 4)
            val posicao = fixture.service.reservar(produtoId, 4)

            assertEquals(10, posicao.quantidadeFisica)
            assertEquals(5, posicao.quantidadeReservada)
            assertEquals(5, posicao.quantidadeDisponivel)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve liberar reserva e concluir pedido agendado reduzindo fisico e reservado`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 10,
                    quantidadeReservada = 4,
                    quantidadeMinima = 2,
                )

            val aposLiberar = fixture.service.liberarReserva(produtoId, 1)
            assertEquals(3, aposLiberar.quantidadeReservada)
            assertEquals(7, aposLiberar.quantidadeDisponivel)

            val aposConclusao = fixture.service.concluirPedidoAgendado(produtoId, 2)
            assertEquals(8, aposConclusao.quantidadeFisica)
            assertEquals(1, aposConclusao.quantidadeReservada)
            assertEquals(7, aposConclusao.quantidadeDisponivel)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve validar disponibilidade novamente ao concluir pedido imediato`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 10,
                    quantidadeReservada = 2,
                    quantidadeMinima = 0,
                )

            fixture.service.validarDisponibilidade(produtoId, 8)
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.concluirPedidoImediato(produtoId, 9)
            }

            val posicao = fixture.service.concluirPedidoImediato(produtoId, 8)
            assertEquals(2, posicao.quantidadeFisica)
            assertEquals(2, posicao.quantidadeReservada)
            assertEquals(0, posicao.quantidadeDisponivel)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve impedir disponibilidade e reserva para estoque bloqueado`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 10,
                    quantidadeReservada = 0,
                    quantidadeMinima = 0,
                    bloqueado = true,
                )

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.validarDisponibilidade(produtoId, 1)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.reservar(produtoId, 1)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.concluirPedidoImediato(produtoId, 1)
            }
            assertEquals(10, fixture.service.buscarRegistroEstoquePorProdutoId(produtoId).quantidadeFisica)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve permitir concluir reserva existente mesmo se o estoque foi bloqueado depois`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 5,
                    quantidadeReservada = 2,
                    quantidadeMinima = 0,
                    bloqueado = true,
                )

            val posicao = fixture.service.concluirPedidoAgendado(produtoId, 2)

            assertEquals(3, posicao.quantidadeFisica)
            assertEquals(0, posicao.quantidadeReservada)
            assertEquals(true, posicao.bloqueado)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar quantidade invalida ou saldo insuficiente`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 3,
                    quantidadeReservada = 1,
                    quantidadeMinima = 0,
                )

            assertFailsWith<BadRequestException> {
                fixture.service.reservar(produtoId, 0)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.reservar(produtoId, 3)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.liberarReserva(produtoId, 2)
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.concluirPedidoAgendado(produtoId, 2)
            }
        } finally {
            fixture.limpar()
        }
    }
}

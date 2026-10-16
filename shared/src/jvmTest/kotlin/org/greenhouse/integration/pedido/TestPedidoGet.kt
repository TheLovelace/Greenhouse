package org.greenhouse.integration.pedido

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.dto.request.PedidoCreateRequestDTO
import org.greenhouse.dto.request.PedidoItemRequestDTO
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.integration.fixture.PedidoFixture
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestPedidoGet {
    @Test
    fun `deve listar pedidos com seus itens`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val criado =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 2)),
                    ),
                )

            val lista = fixture.useCase.listar()
            val encontrado = lista.single { it.id == criado.id }

            assertEquals(1, encontrado.itens.size)
            assertEquals(produtoId, encontrado.itens.single().produtoId)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve filtrar por cliente funcionario status e modalidade`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val outroFuncionarioId = fixture.novoFuncionario()
        val clienteId = fixture.novoCliente()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 20)

        try {
            val comCliente =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        clienteId = clienteId,
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(2),
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            val doOutroFuncionario =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = outroFuncionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 1)),
                    ),
                )

            assertEquals(
                listOf(comCliente.id),
                fixture.useCase.listar(clienteId = clienteId).map { it.id },
            )
            assertEquals(
                listOf(doOutroFuncionario.id),
                fixture.useCase.listar(funcionarioId = outroFuncionarioId).map { it.id },
            )
            assertEquals(
                listOf(comCliente.id),
                fixture.useCase
                    .listar(funcionarioId = funcionarioId, status = "ABERTO", modalidade = "AGENDADA")
                    .map { it.id },
            )
            assertTrue(
                fixture.useCase
                    .listar(status = "ABERTO")
                    .map { it.id }
                    .containsAll(listOf(comCliente.id, doOutroFuncionario.id)),
            )
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar filtro de status ou modalidade invalido`() {
        val fixture = PedidoFixture()

        assertFailsWith<BadRequestException> {
            fixture.useCase.listar(status = "INVALIDO")
        }
        assertFailsWith<BadRequestException> {
            fixture.useCase.listar(modalidade = "INVALIDA")
        }
    }

    @Test
    fun `deve buscar pedido por id com itens`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val formaPagamentoId = fixture.novaFormaPagamento()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val criado =
                fixture.criarPedido(
                    PedidoCreateRequestDTO(
                        funcionarioId = funcionarioId,
                        formaPagamentoId = formaPagamentoId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        observacao = "Pedido de teste",
                        itens = listOf(PedidoItemRequestDTO(produtoId = produtoId, quantidade = 3)),
                    ),
                )

            val consultado = fixture.useCase.buscarPorId(criado.id)
            assertEquals(criado.id, consultado.id)
            assertEquals("Pedido de teste", consultado.observacao)
            assertEquals(1, consultado.itens.size)
            assertEquals(3, consultado.itens.single().quantidade)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar pedido inexistente e id invalido`() {
        val fixture = PedidoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.buscarPorId(Long.MAX_VALUE)
        }
        assertFailsWith<BadRequestException> {
            fixture.useCase.buscarPorId(0)
        }
    }
}

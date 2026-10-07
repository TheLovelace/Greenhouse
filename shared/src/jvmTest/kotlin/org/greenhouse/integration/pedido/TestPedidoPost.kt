package org.greenhouse.integration.pedido

import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Pedido
import org.greenhouse.entity.PedidoItem
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.integration.fixture.PedidoFixture
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestPedidoPost {
    @Test
    fun `deve criar pedido imediato com um item e totais calculados`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("25.50"))

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 2)),
                    ),
                )

            assertNotNull(pedido.id)
            assertNotNull(pedido.dataPedido)
            assertEquals(StatusPedido.ABERTO, pedido.status)
            assertEquals(funcionarioId, pedido.funcionarioId)
            assertEquals(BigDecimal("25.50"), pedido.itens.single().precoUnitario)
            assertEquals(BigDecimal("51.00"), pedido.valorTotal)
            assertEquals(BigDecimal.ZERO.setScale(2), pedido.valorDesconto)
            assertNull(pedido.dataRetirada)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve criar pedido com multiplos itens somando o subtotal`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoA = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("10.00"))
        val produtoB = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("7.25"))

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens =
                            listOf(
                                PedidoItem(produtoId = produtoA, quantidade = 3),
                                PedidoItem(produtoId = produtoB, quantidade = 4),
                            ),
                    ),
                )

            assertEquals(2, pedido.itens.size)
            assertEquals(BigDecimal("59.00"), pedido.valorTotal)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve ignorar campos calculados e de controle enviados pelo cliente`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("10.00"))

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        id = 999999L,
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        status = StatusPedido.CONCLUIDO,
                        dataPedido = OffsetDateTime.parse("2000-01-01T00:00:00Z"),
                        valorDesconto = BigDecimal("999.00"),
                        valorTotal = BigDecimal("0.01"),
                        itens =
                            listOf(
                                PedidoItem(
                                    id = 999999L,
                                    produtoId = produtoId,
                                    quantidade = 1,
                                    precoUnitario = BigDecimal("999.00"),
                                ),
                            ),
                    ),
                )

            assertEquals(StatusPedido.ABERTO, pedido.status)
            assertEquals(BigDecimal("10.00"), pedido.itens.single().precoUnitario)
            assertEquals(BigDecimal("10.00"), pedido.valorTotal)
            assertEquals(BigDecimal.ZERO.setScale(2), pedido.valorDesconto)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve criar pedido agendado com retirada prevista e sem cliente`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 5)

        try {
            val retirada = OffsetDateTime.now().plusDays(3)
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = retirada,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )

            assertEquals(ModalidadePedido.AGENDADA, pedido.modalidade)
            assertNotNull(pedido.dataRetiradaPrevista)
            assertNull(pedido.clienteId)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar agendado sem retirada prevista e data anterior ao dia corrente`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 5)

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().minusDays(1),
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        dataRetiradaPrevista = OffsetDateTime.now().minusDays(1),
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve aceitar retirada prevista no dia corrente`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 5)

        try {
            val hojeCedo = LocalDate.now().atTime(0, 0).atOffset(OffsetDateTime.now().offset)
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = hojeCedo,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )

            assertNotNull(pedido.id)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar pedido sem itens ou com quantidade invalida`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 5)

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = emptyList(),
                    ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 0)),
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar produto inexistente inativo ou duplicado`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 5)
        val produtoInativo = fixture.novoProduto(ativo = false)

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = Long.MAX_VALUE, quantidade = 1)),
                    ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoInativo, quantidade = 1)),
                    ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens =
                            listOf(
                                PedidoItem(produtoId = produtoId, quantidade = 1),
                                PedidoItem(produtoId = produtoId, quantidade = 2),
                            ),
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve validar cliente opcional existente e ativo`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 5)
        val clienteId = fixture.novoCliente()
        val clienteInativo = fixture.novoCliente(ativo = false)

        try {
            val comCliente =
                fixture.criarPedido(
                    Pedido(
                        clienteId = clienteId,
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            assertEquals(clienteId, comCliente.clienteId)

            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        clienteId = Long.MAX_VALUE,
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        clienteId = clienteInativo,
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar funcionario inexistente ou inativo`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val funcionarioInativo = fixture.novoFuncionario(ativo = false)
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 5)

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = Long.MAX_VALUE,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioInativo,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar quantidade acima da disponibilidade`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 4, quantidadeReservada = 2)

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 3)),
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve aplicar desconto percentual e truncar valor fixo no subtotal`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("10.00"))
        val descontoPercentual = fixture.novoDesconto(tipo = TipoDesconto.PERCENTUAL, valor = BigDecimal("10"))
        val descontoFixo = fixture.novoDesconto(tipo = TipoDesconto.VALOR_FIXO, valor = BigDecimal("500.00"))

        try {
            val comPercentual =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        descontoId = descontoPercentual,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 2)),
                    ),
                )
            assertEquals(BigDecimal("2.00"), comPercentual.valorDesconto)
            assertEquals(BigDecimal("18.00"), comPercentual.valorTotal)

            val comFixo =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        descontoId = descontoFixo,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            assertEquals(BigDecimal("10.00"), comFixo.valorDesconto)
            assertEquals(BigDecimal("0.00"), comFixo.valorTotal)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar desconto inexistente inativo ou fora de vigencia`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)
        val descontoInativo = fixture.novoDesconto(ativo = false)
        val descontoExpirado =
            fixture.novoDesconto(
                dataInicio = OffsetDateTime.now().minusDays(10),
                dataFim = OffsetDateTime.now().minusDays(1),
            )

        fun pedidoComDesconto(descontoId: Long) =
            Pedido(
                funcionarioId = funcionarioId,
                descontoId = descontoId,
                modalidade = ModalidadePedido.IMEDIATA,
                itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
            )

        try {
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(pedidoComDesconto(Long.MAX_VALUE))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(pedidoComDesconto(descontoInativo))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.criarPedido(pedidoComDesconto(descontoExpirado))
            }
        } finally {
            fixture.limpar()
        }
    }
}

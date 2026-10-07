package org.greenhouse.integration.pedido

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Pedido
import org.greenhouse.entity.PedidoItem
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.integration.fixture.PedidoFixture
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TestPedidoPatch {
    @Test
    fun `deve atualizar observacao e desconto de pedido aberto`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("20.00"))
        val descontoId = fixture.novoDesconto(valor = BigDecimal("10"))

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 2)),
                    ),
                )

            val atualizado =
                fixture.useCase.atualizar(
                    pedido.id!!,
                    mapOf(
                        "observacao" to "Retirar no balcão",
                        "descontoId" to descontoId,
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
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("10.00"))
        val produtoNovo = fixture.novoProdutoComEstoque(quantidadeFisica = 10, preco = BigDecimal("7.00"))

        try {
            val pedido =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 2)),
                    ),
                )

            val atualizado =
                fixture.useCase.atualizar(
                    pedido.id!!,
                    mapOf(
                        "itens" to
                            listOf(
                                mapOf("produtoId" to produtoNovo, "quantidade" to 3),
                            ),
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
    fun `deve rejeitar itens em pedido confirmado e qualquer campo em status finais`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)

        try {
            val confirmado =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        modalidade = ModalidadePedido.IMEDIATA,
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )
            fixture.useCase.atualizarStatus(confirmado.id!!, StatusPedido.CONFIRMADO)

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(
                    confirmado.id!!,
                    mapOf("itens" to listOf(mapOf("produtoId" to produtoId, "quantidade" to 1))),
                )
            }
            val atualizadoEmConfirmado =
                fixture.useCase.atualizar(confirmado.id!!, mapOf("observacao" to "ok"))
            assertEquals("ok", atualizadoEmConfirmado.observacao)

            fixture.useCase.atualizarStatus(confirmado.id!!, StatusPedido.CONCLUIDO)
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(confirmado.id!!, mapOf("observacao" to "tarde demais"))
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar campos nao editaveis ou desconhecidos`() {
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

            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(pedido.id!!, mapOf("status" to "CANCELADO"))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(pedido.id!!, mapOf("modalidade" to "AGENDADA"))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(pedido.id!!, mapOf("valorTotal" to BigDecimal("1.00")))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(pedido.id!!, mapOf("campoInexistente" to "x"))
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(pedido.id!!, emptyMap())
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve validar retirada prevista e aceitar null em desconto e cliente`() {
        val fixture = PedidoFixture()
        val funcionarioId = fixture.novoFuncionario()
        val produtoId = fixture.novoProdutoComEstoque(quantidadeFisica = 10)
        val descontoId = fixture.novoDesconto()

        try {
            val agendado =
                fixture.criarPedido(
                    Pedido(
                        funcionarioId = funcionarioId,
                        descontoId = descontoId,
                        modalidade = ModalidadePedido.AGENDADA,
                        dataRetiradaPrevista = OffsetDateTime.now().plusDays(2),
                        itens = listOf(PedidoItem(produtoId = produtoId, quantidade = 1)),
                    ),
                )

            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(agendado.id!!, mapOf("dataRetiradaPrevista" to null))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.useCase.atualizar(
                    agendado.id!!,
                    mapOf("dataRetiradaPrevista" to OffsetDateTime.now().minusDays(2).toString()),
                )
            }
            assertFailsWith<BadRequestException> {
                fixture.useCase.atualizar(agendado.id!!, mapOf("dataRetiradaPrevista" to "não é data"))
            }

            val semDesconto = fixture.useCase.atualizar(agendado.id!!, mapOf("descontoId" to null))
            assertNull(semDesconto.descontoId)
            assertEquals(BigDecimal.ZERO.setScale(2), semDesconto.valorDesconto)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacao de pedido inexistente`() {
        val fixture = PedidoFixture()

        assertFailsWith<NotFoundException> {
            fixture.useCase.atualizar(Long.MAX_VALUE, mapOf("observacao" to "x"))
        }
        assertFailsWith<BadRequestException> {
            fixture.useCase.atualizar(0, mapOf("observacao" to "x"))
        }
    }
}

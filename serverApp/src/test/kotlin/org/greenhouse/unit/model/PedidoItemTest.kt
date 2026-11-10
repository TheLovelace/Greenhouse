package org.greenhouse.unit.model

import java.math.BigDecimal
import org.greenhouse.model.PedidoItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE PedidoItem.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class PedidoItemTest {
    @Test
    fun `deve guardar os dados alterados do item do pedido`() {
        val pedidoItem = PedidoItem()
        val idNovo = 11L
        val pedidoIdNovo = 12L
        val produtoIdNovo = 8L
        val quantidadeNovo = 2
        val precoUnitarioNovo = BigDecimal("39.90")

        pedidoItem.id = idNovo
        pedidoItem.pedidoId = pedidoIdNovo
        pedidoItem.produtoId = produtoIdNovo
        pedidoItem.quantidade = quantidadeNovo
        pedidoItem.precoUnitario = precoUnitarioNovo

        assertAll(
                "campos do item do pedido",
                { assertEquals(idNovo, pedidoItem.id, "id") },
                { assertEquals(pedidoIdNovo, pedidoItem.pedidoId, "pedido") },
                { assertEquals(produtoIdNovo, pedidoItem.produtoId, "produto") },
                { assertEquals(quantidadeNovo, pedidoItem.quantidade, "quantidade") },
                { assertEquals(precoUnitarioNovo, pedidoItem.precoUnitario, "preço unitário") },
        )
    }
}

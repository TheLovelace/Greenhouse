package org.greenhouse.unit.model

import java.math.BigDecimal
import org.greenhouse.model.CompraItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE CompraItem.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class CompraItemTest {
    @Test
    fun `deve guardar os dados alterados do item da compra`() {
        val compraItem = CompraItem()
        val idNovo = 7L
        val compraIdNovo = 3L
        val produtoIdNovo = 8L
        val quantidadeNovo = 12
        val valorUnitarioNovo = BigDecimal("9.90")

        compraItem.id = idNovo
        compraItem.compraId = compraIdNovo
        compraItem.produtoId = produtoIdNovo
        compraItem.quantidade = quantidadeNovo
        compraItem.valorUnitario = valorUnitarioNovo

        assertAll(
                "campos do item da compra",
                { assertEquals(idNovo, compraItem.id, "id") },
                { assertEquals(compraIdNovo, compraItem.compraId, "compra") },
                { assertEquals(produtoIdNovo, compraItem.produtoId, "produto") },
                { assertEquals(quantidadeNovo, compraItem.quantidade, "quantidade") },
                { assertEquals(valorUnitarioNovo, compraItem.valorUnitario, "valor unitário") },
        )
    }
}

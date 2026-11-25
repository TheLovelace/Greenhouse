package org.greenhouse.unit.model

import java.math.BigDecimal
import org.greenhouse.model.Produto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE Produto.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class ProdutoTest {
    @Test
    fun `deve guardar os dados alterados do produto`() {
        val produto = Produto()
        val idNovo = 8L
        val codigoNovo = "VAS-001"
        val nomeNovo = "Vaso de cerâmica"
        val descricaoNovo = "Vaso médio"
        val precoNovo = BigDecimal("39.90")
        val ativoNovo = false
        val prazoPreparoMinutosNovo = 45

        produto.id = idNovo
        produto.codigo = codigoNovo
        produto.nome = nomeNovo
        produto.descricao = descricaoNovo
        produto.preco = precoNovo
        produto.ativo = ativoNovo
        produto.prazoPreparoMinutos = prazoPreparoMinutosNovo

        assertAll(
                "campos do produto",
                { assertEquals(idNovo, produto.id, "id") },
                { assertEquals(codigoNovo, produto.codigo, "código") },
                { assertEquals(nomeNovo, produto.nome, "nome") },
                { assertEquals(descricaoNovo, produto.descricao, "descrição") },
                { assertEquals(precoNovo, produto.preco, "preço") },
                { assertEquals(ativoNovo, produto.ativo, "status ativo") },
                { assertEquals(prazoPreparoMinutosNovo, produto.prazoPreparoMinutos, "prazo de preparo") },
        )
    }
}

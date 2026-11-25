package org.greenhouse.unit.model

import org.greenhouse.model.FormaPagamento
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE FormaPagamento.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class FormaPagamentoTest {
    @Test
    fun `deve guardar os dados alterados da forma de pagamento`() {
        val formaPagamento = FormaPagamento()
        val idNovo = 6L
        val nomeNovo = "Cartão de crédito"
        val ativoNovo = false
        val descricaoNovo = "Visa e Master"
        val limiteParcelasNovo = 6
        val prazoEstornoDiasNovo = 30

        formaPagamento.id = idNovo
        formaPagamento.nome = nomeNovo
        formaPagamento.ativo = ativoNovo
        formaPagamento.descricao = descricaoNovo
        formaPagamento.limiteParcelas = limiteParcelasNovo
        formaPagamento.prazoEstornoDias = prazoEstornoDiasNovo

        assertAll(
                "campos da forma de pagamento",
                { assertEquals(idNovo, formaPagamento.id, "id") },
                { assertEquals(nomeNovo, formaPagamento.nome, "nome") },
                { assertEquals(ativoNovo, formaPagamento.ativo, "status ativo") },
                { assertEquals(descricaoNovo, formaPagamento.descricao, "descrição") },
                { assertEquals(limiteParcelasNovo, formaPagamento.limiteParcelas, "limite de parcelas") },
                { assertEquals(prazoEstornoDiasNovo, formaPagamento.prazoEstornoDias, "prazo de estorno") },
        )
    }
}

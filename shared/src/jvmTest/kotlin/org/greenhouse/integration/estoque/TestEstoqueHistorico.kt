package org.greenhouse.integration.estoque

import org.greenhouse.enums.TipoMovimentacaoEstoque
import org.greenhouse.integration.fixture.EstoqueFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestEstoqueHistorico {
    @Test
    fun `deve listar movimentacoes com origem e dados de rastreabilidade em ordem decrescente`() {
        val fixture = EstoqueFixture()

        try {
            val produtoId =
                fixture.novoProdutoComEstoque(
                    quantidadeFisica = 5,
                    quantidadeReservada = 0,
                    quantidadeMinima = 1,
                )
            val primeira =
                fixture.service.registrarMovimentacao(
                    produtoId = produtoId,
                    tipo = TipoMovimentacaoEstoque.AJUSTE_ENTRADA,
                    quantidade = 2,
                    observacao = "Primeiro ajuste",
                )
            val segunda =
                fixture.service.registrarMovimentacao(
                    produtoId = produtoId,
                    tipo = TipoMovimentacaoEstoque.PERDA,
                    quantidade = 1,
                    observacao = "Perda registrada",
                )

            val historico = fixture.service.listarMovimentacoes()
            val primeiraNoHistorico = historico.single { it.id == primeira.id }
            val segundaNoHistorico = historico.single { it.id == segunda.id }

            assertTrue(historico.indexOf(segundaNoHistorico) < historico.indexOf(primeiraNoHistorico))
            assertEquals(produtoId, segundaNoHistorico.produtoId)
            assertEquals(TipoMovimentacaoEstoque.PERDA, segundaNoHistorico.tipo)
            assertEquals(1, segundaNoHistorico.quantidade)
            assertNotNull(segundaNoHistorico.dataHora)
            assertEquals(null, segundaNoHistorico.pedidoId)
            assertEquals(null, segundaNoHistorico.compraId)
            assertEquals(null, segundaNoHistorico.funcionarioId)
            assertEquals("Perda registrada", segundaNoHistorico.observacao)
        } finally {
            fixture.limpar()
        }
    }
}

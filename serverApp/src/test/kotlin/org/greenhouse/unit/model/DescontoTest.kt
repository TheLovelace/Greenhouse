package org.greenhouse.unit.model

import java.math.BigDecimal
import java.time.OffsetDateTime
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.model.Desconto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE Desconto.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class DescontoTest {
    @Test
    fun `deve guardar os dados alterados do desconto`() {
        val desconto = Desconto()
        val idNovo = 2L
        val tituloNovo = "Black Friday"
        val descricaoNovo = "10% em vasos"
        val tipoNovo = TipoDesconto.VALOR_FIXO
        val valorNovo = BigDecimal("15.00")
        val dataInicioNovo = OffsetDateTime.parse("2026-11-27T00:00:00-03:00")
        val dataFimNovo = OffsetDateTime.parse("2026-11-30T23:59:59-03:00")
        val ativoNovo = false
        val quantidadeMinimaItensNovo = 3

        desconto.id = idNovo
        desconto.titulo = tituloNovo
        desconto.descricao = descricaoNovo
        desconto.tipo = tipoNovo
        desconto.valor = valorNovo
        desconto.dataInicio = dataInicioNovo
        desconto.dataFim = dataFimNovo
        desconto.ativo = ativoNovo
        desconto.quantidadeMinimaItens = quantidadeMinimaItensNovo

        assertAll(
                "campos do desconto",
                { assertEquals(idNovo, desconto.id, "id") },
                { assertEquals(tituloNovo, desconto.titulo, "título") },
                { assertEquals(descricaoNovo, desconto.descricao, "descrição") },
                { assertEquals(tipoNovo, desconto.tipo, "tipo") },
                { assertEquals(valorNovo, desconto.valor, "valor") },
                { assertEquals(dataInicioNovo, desconto.dataInicio, "data de início") },
                { assertEquals(dataFimNovo, desconto.dataFim, "data de fim") },
                { assertEquals(ativoNovo, desconto.ativo, "status ativo") },
                { assertEquals(quantidadeMinimaItensNovo, desconto.quantidadeMinimaItens, "quantidade mínima de itens") },
        )
    }
}

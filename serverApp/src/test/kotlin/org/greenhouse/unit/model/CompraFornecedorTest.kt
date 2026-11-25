package org.greenhouse.unit.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import org.greenhouse.enums.StatusCompra
import org.greenhouse.model.CompraFornecedor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE CompraFornecedor.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class CompraFornecedorTest {
    @Test
    fun `deve guardar os dados alterados da compra do fornecedor`() {
        val compraFornecedor = CompraFornecedor()
        val idNovo = 3L
        val funcionarioIdNovo = 4L
        val fornecedorIdNovo = 5L
        val formaPagamentoIdNovo = 6L
        val dataCompraNovo = OffsetDateTime.parse("2026-11-10T09:00:00-03:00")
        val statusNovo = StatusCompra.RECEBIDA
        val valorTotalNovo = BigDecimal("450.00")
        val valorFreteNovo = BigDecimal("30.00")
        val freteInclusoNovo = true
        val dataEntregaPrevistaNovo = LocalDate.of(2026, 11, 15)
        val observacaoNovo = "Entregar pela manhã"

        compraFornecedor.id = idNovo
        compraFornecedor.funcionarioId = funcionarioIdNovo
        compraFornecedor.fornecedorId = fornecedorIdNovo
        compraFornecedor.formaPagamentoId = formaPagamentoIdNovo
        compraFornecedor.dataCompra = dataCompraNovo
        compraFornecedor.status = statusNovo
        compraFornecedor.valorTotal = valorTotalNovo
        compraFornecedor.valorFrete = valorFreteNovo
        compraFornecedor.freteIncluso = freteInclusoNovo
        compraFornecedor.dataEntregaPrevista = dataEntregaPrevistaNovo
        compraFornecedor.observacao = observacaoNovo

        assertAll(
                "campos da compra do fornecedor",
                { assertEquals(idNovo, compraFornecedor.id, "id") },
                { assertEquals(funcionarioIdNovo, compraFornecedor.funcionarioId, "funcionário") },
                { assertEquals(fornecedorIdNovo, compraFornecedor.fornecedorId, "fornecedor") },
                { assertEquals(formaPagamentoIdNovo, compraFornecedor.formaPagamentoId, "forma de pagamento") },
                { assertEquals(dataCompraNovo, compraFornecedor.dataCompra, "data da compra") },
                { assertEquals(statusNovo, compraFornecedor.status, "status") },
                { assertEquals(valorTotalNovo, compraFornecedor.valorTotal, "valor total") },
                { assertEquals(valorFreteNovo, compraFornecedor.valorFrete, "valor do frete") },
                { assertEquals(freteInclusoNovo, compraFornecedor.freteIncluso, "frete incluso") },
                { assertEquals(dataEntregaPrevistaNovo, compraFornecedor.dataEntregaPrevista, "data de entrega prevista") },
                { assertEquals(observacaoNovo, compraFornecedor.observacao, "observação") },
        )
    }
}

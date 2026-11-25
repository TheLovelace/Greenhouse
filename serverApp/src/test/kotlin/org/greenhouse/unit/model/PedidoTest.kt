package org.greenhouse.unit.model

import java.math.BigDecimal
import java.time.OffsetDateTime
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.model.Pedido
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE Pedido.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class PedidoTest {
    @Test
    fun `deve guardar os dados alterados do pedido`() {
        val pedido = Pedido()
        val idNovo = 12L
        val clienteIdNovo = 1L
        val funcionarioIdNovo = 4L
        val descontoIdNovo = 2L
        val formaPagamentoIdNovo = 6L
        val modalidadeNovo = ModalidadePedido.AGENDADA
        val statusNovo = StatusPedido.CONFIRMADO
        val dataPedidoNovo = OffsetDateTime.parse("2026-11-10T10:00:00-03:00")
        val dataRetiradaPrevistaNovo = OffsetDateTime.parse("2026-11-12T15:00:00-03:00")
        val dataRetiradaNovo = OffsetDateTime.parse("2026-11-12T15:30:00-03:00")
        val valorDescontoNovo = BigDecimal("5.00")
        val valorTotalNovo = BigDecimal("74.80")
        val observacaoNovo = "Embrulhar separado"
        val embalagemPresenteNovo = true
        val numeroVolumesNovo = 2

        pedido.id = idNovo
        pedido.clienteId = clienteIdNovo
        pedido.funcionarioId = funcionarioIdNovo
        pedido.descontoId = descontoIdNovo
        pedido.formaPagamentoId = formaPagamentoIdNovo
        pedido.modalidade = modalidadeNovo
        pedido.status = statusNovo
        pedido.dataPedido = dataPedidoNovo
        pedido.dataRetiradaPrevista = dataRetiradaPrevistaNovo
        pedido.dataRetirada = dataRetiradaNovo
        pedido.valorDesconto = valorDescontoNovo
        pedido.valorTotal = valorTotalNovo
        pedido.observacao = observacaoNovo
        pedido.embalagemPresente = embalagemPresenteNovo
        pedido.numeroVolumes = numeroVolumesNovo

        assertAll(
                "campos do pedido",
                { assertEquals(idNovo, pedido.id, "id") },
                { assertEquals(clienteIdNovo, pedido.clienteId, "cliente") },
                { assertEquals(funcionarioIdNovo, pedido.funcionarioId, "funcionário") },
                { assertEquals(descontoIdNovo, pedido.descontoId, "desconto") },
                { assertEquals(formaPagamentoIdNovo, pedido.formaPagamentoId, "forma de pagamento") },
                { assertEquals(modalidadeNovo, pedido.modalidade, "modalidade") },
                { assertEquals(statusNovo, pedido.status, "status") },
                { assertEquals(dataPedidoNovo, pedido.dataPedido, "data do pedido") },
                { assertEquals(dataRetiradaPrevistaNovo, pedido.dataRetiradaPrevista, "data de retirada prevista") },
                { assertEquals(dataRetiradaNovo, pedido.dataRetirada, "data de retirada") },
                { assertEquals(valorDescontoNovo, pedido.valorDesconto, "valor do desconto") },
                { assertEquals(valorTotalNovo, pedido.valorTotal, "valor total") },
                { assertEquals(observacaoNovo, pedido.observacao, "observação") },
                { assertEquals(embalagemPresenteNovo, pedido.embalagemPresente, "embalagem para presente") },
                { assertEquals(numeroVolumesNovo, pedido.numeroVolumes, "número de volumes") },
        )
    }
}

package org.greenhouse.entity

import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import java.math.BigDecimal
import java.time.OffsetDateTime

data class Pedido(
    val id: Long? = null,
    val clienteId: Long? = null,
    val funcionarioId: Long,
    val descontoId: Long? = null,
    val modalidade: ModalidadePedido,
    val status: StatusPedido = StatusPedido.ABERTO,
    val dataPedido: OffsetDateTime? = null,
    val dataRetiradaPrevista: OffsetDateTime? = null,
    val dataRetirada: OffsetDateTime? = null,
    val valorDesconto: BigDecimal = BigDecimal.ZERO,
    val valorTotal: BigDecimal = BigDecimal.ZERO,
    val observacao: String? = null,
    val itens: List<PedidoItem> = emptyList(),
)

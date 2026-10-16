package org.greenhouse.dto.response

import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import java.math.BigDecimal
import java.time.OffsetDateTime

data class PedidoResponseDTO(
    val id: Long,
    val clienteId: Long?,
    val funcionarioId: Long,
    val descontoId: Long?,
    val formaPagamentoId: Long,
    val modalidade: ModalidadePedido,
    val status: StatusPedido,
    val dataPedido: OffsetDateTime,
    val dataRetiradaPrevista: OffsetDateTime?,
    val dataRetirada: OffsetDateTime?,
    val valorDesconto: BigDecimal,
    val valorTotal: BigDecimal,
    val observacao: String?,
    val embalagemPresente: Boolean,
    val numeroVolumes: Int,
    val itens: List<PedidoItemResponseDTO>,
)

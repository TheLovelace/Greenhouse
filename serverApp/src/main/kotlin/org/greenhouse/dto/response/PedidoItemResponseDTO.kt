package org.greenhouse.dto.response

import java.math.BigDecimal

data class PedidoItemResponseDTO(
    val id: Long,
    val produtoId: Long,
    val quantidade: Int,
    val precoUnitario: BigDecimal,
)

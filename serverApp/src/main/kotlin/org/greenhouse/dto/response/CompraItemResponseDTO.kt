package org.greenhouse.dto.response

import java.math.BigDecimal

data class CompraItemResponseDTO(
    val id: Long,
    val produtoId: Long,
    val quantidade: Int,
    val valorUnitario: BigDecimal,
)

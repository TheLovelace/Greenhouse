package org.greenhouse.entity

import java.math.BigDecimal

data class PedidoItem(
    val id: Long? = null,
    val produtoId: Long,
    val quantidade: Int,
    val precoUnitario: BigDecimal? = null,
)

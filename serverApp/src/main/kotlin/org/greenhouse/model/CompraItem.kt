package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "compra_item")
data class CompraItem(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(name = "compra_id", nullable = false)
    var compraId: Long = 0,
    @field:Column(name = "produto_id", nullable = false)
    var produtoId: Long = 0,
    @field:Column(nullable = false)
    var quantidade: Int = 0,
    @field:Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    var valorUnitario: BigDecimal = BigDecimal.ZERO,
)

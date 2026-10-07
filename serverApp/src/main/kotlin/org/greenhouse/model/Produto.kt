package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "produto")
data class Produto(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(length = 50, unique = true)
    var codigo: String? = null,
    @field:Column(nullable = false, length = 150)
    var nome: String = "",
    @field:Column(columnDefinition = "TEXT")
    var descricao: String? = null,
    @field:Column(nullable = false, precision = 12, scale = 2)
    var preco: BigDecimal = BigDecimal.ZERO,
    @field:Column(nullable = false)
    var ativo: Boolean = true,
)

package org.greenhouse.dto.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CompraItemRequestDTO(
    @field:Positive(message = "O id do produto deve ser maior que zero")
    val produtoId: Long,
    @field:Positive(message = "A quantidade do item deve ser maior que zero")
    val quantidade: Int,
    @field:DecimalMin(value = "0", message = "O valor unitário não pode ser negativo")
    @field:Digits(integer = 10, fraction = 2, message = "O valor unitário deve respeitar o formato NUMERIC(12,2)")
    val valorUnitario: BigDecimal,
)

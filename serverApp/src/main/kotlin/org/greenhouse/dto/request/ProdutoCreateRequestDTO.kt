package org.greenhouse.dto.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ProdutoCreateRequestDTO(
    @field:Size(max = 50)
    val codigo: String? = null,
    @field:NotNull
    @field:Size(max = 150)
    val nome: String,
    @field:Size(max = 1000)
    val descricao: String? = null,
    @field:NotNull
    @field:DecimalMin(value = "0.00", inclusive = true)
    @field:Digits(integer = 10, fraction = 2)
    val preco: BigDecimal,
    val ativo: Boolean = true,
    @field:PositiveOrZero
    val prazoPreparoMinutos: Int? = null,
)

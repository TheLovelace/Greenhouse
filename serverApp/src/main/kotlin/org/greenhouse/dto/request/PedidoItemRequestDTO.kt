package org.greenhouse.dto.request

import jakarta.validation.constraints.Positive

data class PedidoItemRequestDTO(
    @field:Positive(message = "O id do produto deve ser maior que zero")
    val produtoId: Long,
    @field:Positive(message = "A quantidade do item deve ser maior que zero")
    val quantidade: Int,
)

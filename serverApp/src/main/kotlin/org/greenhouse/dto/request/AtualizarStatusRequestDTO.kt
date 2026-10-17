package org.greenhouse.dto.request

import jakarta.validation.constraints.NotNull
import org.greenhouse.enums.StatusPedido

data class AtualizarStatusRequestDTO(
    @field:NotNull(message = "O status do pedido é obrigatório")
    val status: StatusPedido,
)

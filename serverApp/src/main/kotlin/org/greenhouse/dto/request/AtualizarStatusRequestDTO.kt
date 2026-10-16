package org.greenhouse.dto.request

import org.greenhouse.enums.StatusPedido

data class AtualizarStatusRequestDTO(
    val status: StatusPedido,
)

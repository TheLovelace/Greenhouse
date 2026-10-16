package org.greenhouse.dto.request

import org.greenhouse.enums.StatusCompra

data class AtualizarStatusCompraRequestDTO(
    val status: StatusCompra,
)

package org.greenhouse.controller

import org.greenhouse.enums.StatusPedido

data class AtualizarStatusPedidoRequest(
    val status: StatusPedido,
)

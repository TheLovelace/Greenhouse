package org.greenhouse.dto.response

import java.math.BigDecimal

data class ProdutoResponseDTO(
    val id: Long,
    val codigo: String?,
    val nome: String,
    val descricao: String?,
    val preco: BigDecimal,
    val ativo: Boolean,
    val prazoPreparoMinutos: Int?,
)

package org.greenhouse.dto

import java.math.BigDecimal

data class ProdutoDTO(
    val id: Long? = null,
    val codigo: String? = null,
    val nome: String,
    val descricao: String? = null,
    val preco: BigDecimal,
    val ativo: Boolean = true,
)

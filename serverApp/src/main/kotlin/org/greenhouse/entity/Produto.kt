package org.greenhouse.entity

import java.math.BigDecimal

data class Produto(
    val id: Long? = null,
    val codigo: String? = null,
    val nome: String,
    val descricao: String? = null,
    val preco: BigDecimal,
    val ativo: Boolean = true,
)

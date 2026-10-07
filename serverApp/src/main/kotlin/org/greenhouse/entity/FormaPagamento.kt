package org.greenhouse.entity

data class FormaPagamento(
    val id: Long? = null,
    val nome: String,
    val ativo: Boolean = true,
)

package org.greenhouse.dto

data class FormaPagamentoDTO(
    val id: Long? = null,
    val nome: String,
    val ativo: Boolean = true,
)

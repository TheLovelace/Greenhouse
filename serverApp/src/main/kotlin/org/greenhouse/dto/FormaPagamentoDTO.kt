package org.greenhouse.dto

data class FormaPagamentoDTO(
    val id: Long? = null,
    val nome: String,
    val ativo: Boolean = true,
    val descricao: String? = null,
    val limiteParcelas: Int = 1,
    val prazoEstornoDias: Int? = null,
)

package org.greenhouse.dto.response

data class FormaPagamentoResponseDTO(
    val id: Long,
    val nome: String,
    val ativo: Boolean,
    val descricao: String?,
    val limiteParcelas: Int,
    val prazoEstornoDias: Int?,
)

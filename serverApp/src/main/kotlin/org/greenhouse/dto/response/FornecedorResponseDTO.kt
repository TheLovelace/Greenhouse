package org.greenhouse.dto.response

data class FornecedorResponseDTO(
    val id: Long,
    val nome: String,
    val cnpj: String?,
    val telefone: String?,
    val email: String?,
    val contato: String?,
    val ativo: Boolean,
    val prazoMedioEntregaDias: Int?,
)

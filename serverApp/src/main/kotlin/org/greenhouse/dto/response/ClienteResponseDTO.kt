package org.greenhouse.dto.response

data class ClienteResponseDTO(
    val id: Long,
    val nome: String,
    val telefone: String,
    val whatsapp: Boolean,
    val cpf: String?,
    val email: String?,
    val ativo: Boolean,
)

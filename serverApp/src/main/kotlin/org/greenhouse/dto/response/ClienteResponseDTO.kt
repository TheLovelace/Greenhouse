package org.greenhouse.dto.response

import java.time.LocalDate

data class ClienteResponseDTO(
    val id: Long,
    val nome: String,
    val telefone: String,
    val whatsapp: Boolean,
    val cpf: String?,
    val email: String?,
    val dataNascimento: LocalDate?,
    val ativo: Boolean,
)

package org.greenhouse.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ClienteCreateRequestDTO(
    @field:NotNull
    @field:Size(max = 150)
    val nome: String,
    @field:NotNull
    @field:Size(max = 20)
    val telefone: String,
    val whatsapp: Boolean = false,
    val cpf: String? = null,
    @field:Size(max = 150)
    val email: String? = null,
    val ativo: Boolean = true,
)

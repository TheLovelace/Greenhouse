package org.greenhouse.dto.response

import java.time.LocalDate

data class FuncionarioResponseDTO(
    val id: Long,
    val nome: String,
    val cpf: String,
    val telefone: String?,
    val email: String?,
    val cargo: String?,
    val ativo: Boolean,
    val dataDeNascimento: LocalDate?,
)

package org.greenhouse.dto

import java.time.LocalDate

data class FuncionarioDTO(
    val id: Long? = null,
    val nome: String,
    val cpf: String,
    val telefone: String? = null,
    val email: String? = null,
    val cargo: String? = null,
    val ativo: Boolean = true,
    val dataDeNascimento: LocalDate? = null,
)

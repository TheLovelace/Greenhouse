package org.greenhouse.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class FuncionarioRequestDTO(
    @field:NotBlank(message = "O nome do funcionário é obrigatório")
    @field:Size(max = 150, message = "O nome do funcionário deve ter no máximo 150 caracteres")
    val nome: String,

    @field:NotBlank(message = "O CPF do funcionário é obrigatório")
    @field:Pattern(
        regexp = "^[0-9]{11}$",
        message = "CPF inválido. Deve conter exatamente 11 dígitos numéricos.",
    )
    val cpf: String,

    @field:Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    val telefone: String? = null,

    @field:Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    val email: String? = null,

    @field:Size(max = 100, message = "O cargo deve ter no máximo 100 caracteres")
    val cargo: String? = null,

    val ativo: Boolean = true,
    val dataDeNascimento: LocalDate? = null,
)

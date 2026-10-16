package org.greenhouse.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class FornecedorCreateRequestDTO(
    @field:NotBlank(message = "O nome do fornecedor é obrigatório")
    @field:Size(max = 150, message = "O nome do fornecedor deve ter no máximo 150 caracteres")
    val nome: String,
    @field:Pattern(regexp = "^[0-9]{14}$", message = "O CNPJ deve conter exatamente 14 números")
    val cnpj: String? = null,
    @field:Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    val telefone: String? = null,
    @field:Email(message = "O e-mail do fornecedor deve ser válido")
    @field:Size(max = 150, message = "O email deve ter no máximo 150 caracteres")
    val email: String? = null,
    @field:Size(max = 150, message = "O contato deve ter no máximo 150 caracteres")
    val contato: String? = null,
    val ativo: Boolean = true,
    @field:PositiveOrZero(message = "O prazo médio de entrega não pode ser negativo")
    val prazoMedioEntregaDias: Int? = null,
)

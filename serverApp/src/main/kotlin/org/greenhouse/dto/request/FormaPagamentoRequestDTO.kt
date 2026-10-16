package org.greenhouse.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class FormaPagamentoRequestDTO(
    @field:NotBlank(message = "O nome da forma de pagamento é obrigatório")
    @field:Size(max = 50, message = "O nome da forma de pagamento deve ter no máximo 50 caracteres")
    val nome: String,

    val ativo: Boolean = true,

    @field:Size(max = 250, message = "A descrição deve ter no máximo 250 caracteres")
    val descricao: String? = null,

    @field:Min(value = 1, message = "O limite de parcelas deve ser maior ou igual a 1")
    val limiteParcelas: Int = 1,

    @field:PositiveOrZero(message = "O prazo de estorno não pode ser negativo")
    val prazoEstornoDias: Int? = null,
)

package org.greenhouse.dto.request

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class EstoqueCreateRequestDTO(
    @field:Positive(message = "O id do produto deve ser maior que zero")
    val produtoId: Long,
    
    @field:PositiveOrZero(message = "A quantidade física não pode ser negativa")
    val quantidadeFisica: Int = 0,
    
    @field:PositiveOrZero(message = "A quantidade mínima não pode ser negativa")
    val quantidadeMinima: Int = 0,
    
    @field:Size(max = 100, message = "A localização deve ter no máximo 100 caracteres")
    val localizacao: String? = null,
    
    val bloqueado: Boolean = false,
)

package org.greenhouse.dto.request

import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class EstoqueUpdateRequestDTO(
    @field:PositiveOrZero(message = "A quantidade física não pode ser negativa")
    val quantidadeFisica: Int,
    
    @field:PositiveOrZero(message = "A quantidade mínima não pode ser negativa")
    val quantidadeMinima: Int,
    
    @field:Size(max = 100, message = "A localização deve ter no máximo 100 caracteres")
    val localizacao: String? = null,
    
    val bloqueado: Boolean,
)

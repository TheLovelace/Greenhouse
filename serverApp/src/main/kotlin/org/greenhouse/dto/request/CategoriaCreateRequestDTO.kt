package org.greenhouse.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CategoriaCreateRequestDTO(
    @field:NotNull
    @field:Size(max = 100)
    val nome: String,
    @field:NotNull
    @field:Size(max = 1000)
    val descricao: String,
    val ativo: Boolean = true,
    @field:Size(max = 300)
    val observacao: String? = null,
    val dataCadastro: LocalDate? = null,
)

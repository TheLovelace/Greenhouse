package org.greenhouse.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size

data class CategoriaUpdateRequestDTO(
    val id: Long? = null,
    @field:Size(max = 100)
    val nome: String? = null,
    @field:Size(max = 1000)
    val descricao: String? = null,
    val ativo: Boolean? = null,
) {
    @get:JsonIgnore
    @get:AssertTrue(message = "Informe ao menos um campo para atualizar")
    val possuiCamposParaAtualizar: Boolean
        get() = nome != null || descricao != null || ativo != null
}

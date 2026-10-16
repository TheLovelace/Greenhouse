package org.greenhouse.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size

class CategoriaUpdateRequestDTO {
    var id: Long? = null

    @field:Size(max = 100)
    var nome: String? = null

    @field:Size(max = 1000)
    var descricao: String? = null

    var ativo: Boolean? = null

    @field:Size(max = 300)
    var observacao: String? = null
        @JsonSetter("observacao")
        set(value) {
            field = value
            observacaoInformada = true
        }

    @get:JsonIgnore
    var observacaoInformada = false
        private set

    @get:JsonIgnore
    @get:AssertTrue(message = "Informe ao menos um campo para atualizar")
    val possuiCamposParaAtualizar: Boolean
        get() = nome != null || descricao != null || ativo != null || observacaoInformada
}

package org.greenhouse.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal

class ProdutoUpdateRequestDTO {
    @field:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var id: Long? = null

    @field:Size(max = 50)
    var codigo: String? = null
        @JsonSetter("codigo")
        set(value) {
            field = value
            codigoInformado = true
        }

    @field:Size(max = 150)
    var nome: String? = null
        @JsonSetter("nome")
        set(value) {
            field = value
            nomeInformado = true
        }

    @field:Size(max = 1000)
    var descricao: String? = null
        @JsonSetter("descricao")
        set(value) {
            field = value
            descricaoInformada = true
        }

    @field:DecimalMin(value = "0.00", inclusive = true)
    @field:Digits(integer = 10, fraction = 2)
    var preco: BigDecimal? = null
        @JsonSetter("preco")
        set(value) {
            field = value
            precoInformado = true
        }

    var ativo: Boolean? = null
        @JsonSetter("ativo")
        set(value) {
            field = value
            ativoInformado = true
        }

    @field:PositiveOrZero
    var prazoPreparoMinutos: Int? = null
        @JsonSetter("prazoPreparoMinutos")
        set(value) {
            field = value
            prazoPreparoMinutosInformado = true
        }

    @get:JsonIgnore
    var codigoInformado = false
        private set

    @get:JsonIgnore
    var nomeInformado = false
        private set

    @get:JsonIgnore
    var descricaoInformada = false
        private set

    @get:JsonIgnore
    var precoInformado = false
        private set

    @get:JsonIgnore
    var ativoInformado = false
        private set

    @get:JsonIgnore
    var prazoPreparoMinutosInformado = false
        private set

    @get:JsonIgnore
    @get:AssertTrue(message = "Informe ao menos um campo para atualizar")
    val possuiCamposParaAtualizar: Boolean
        get() =
            codigoInformado ||
                nomeInformado ||
                descricaoInformada ||
                precoInformado ||
                ativoInformado ||
                prazoPreparoMinutosInformado

    @get:JsonIgnore
    @get:AssertTrue(message = "Nome, preço e ativo não podem ser nulos quando informados")
    val valoresInformadosSaoValidos: Boolean
        get() =
            (!nomeInformado || nome != null) &&
                (!precoInformado || preco != null) &&
                (!ativoInformado || ativo != null)
}

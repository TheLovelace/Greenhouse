package org.greenhouse.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ClienteUpdateRequestDTO(
    val id: Long? = null,
    @field:Size(max = 150)
    val nome: String? = null,
    @field:Size(max = 20)
    val telefone: String? = null,
    val whatsapp: Boolean? = null,
    val cpf: String? = null,
    @field:Size(max = 150)
    val email: String? = null,
    val dataNascimento: LocalDate? = null,
    val ativo: Boolean? = null,
) {
    @get:JsonIgnore
    @get:AssertTrue(message = "Informe ao menos um campo para atualizar")
    val possuiCamposParaAtualizar: Boolean
        get() =
            nome != null ||
                telefone != null ||
                whatsapp != null ||
                cpf != null ||
                email != null ||
                dataNascimento != null ||
                ativo != null
}

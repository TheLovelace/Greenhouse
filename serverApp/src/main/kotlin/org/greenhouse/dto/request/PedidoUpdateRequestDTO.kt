package org.greenhouse.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class PedidoUpdateRequestDTO(
    val id: Long? = null,
    @field:Positive(message = "O id do cliente deve ser maior que zero")
    val clienteId: Long? = null,
    @field:Positive(message = "O id do funcionário deve ser maior que zero")
    val funcionarioId: Long? = null,
    @field:Positive(message = "O id do desconto deve ser maior que zero")
    val descontoId: Long? = null,
    @field:Positive(message = "O id da forma de pagamento deve ser maior que zero")
    val formaPagamentoId: Long? = null,
    val dataRetiradaPrevista: OffsetDateTime? = null,
    @field:Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
    val observacao: String? = null,
    val embalagemPresente: Boolean? = null,
    @field:Positive(message = "O número de volumes deve ser no mínimo 1")
    val numeroVolumes: Int? = null,
    @field:Valid
    val itens: List<PedidoItemRequestDTO>? = null,
) {
    @get:JsonIgnore
    @get:AssertTrue(message = "Informe ao menos um campo para atualizar")
    val possuiCamposParaAtualizar: Boolean
        get() =
            clienteId != null ||
                funcionarioId != null ||
                descontoId != null ||
                formaPagamentoId != null ||
                dataRetiradaPrevista != null ||
                observacao != null ||
                embalagemPresente != null ||
                numeroVolumes != null ||
                itens != null
}

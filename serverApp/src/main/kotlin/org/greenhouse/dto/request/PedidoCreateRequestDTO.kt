package org.greenhouse.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.greenhouse.enums.ModalidadePedido
import java.time.OffsetDateTime

data class PedidoCreateRequestDTO(
    @field:Positive(message = "O id do cliente deve ser maior que zero")
    val clienteId: Long? = null,
    @field:Positive(message = "O id do funcionário deve ser maior que zero")
    val funcionarioId: Long,
    @field:Positive(message = "O id do desconto deve ser maior que zero")
    val descontoId: Long? = null,
    @field:Positive(message = "O id da forma de pagamento deve ser maior que zero")
    val formaPagamentoId: Long,
    val modalidade: ModalidadePedido,
    val dataRetiradaPrevista: OffsetDateTime? = null,
    @field:Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
    val observacao: String? = null,
    val embalagemPresente: Boolean = false,
    @field:Positive(message = "O número de volumes deve ser no mínimo 1")
    val numeroVolumes: Int = 1,
    @field:NotEmpty(message = "O pedido deve ter ao menos um item")
    @field:Valid
    val itens: List<PedidoItemRequestDTO>,
)

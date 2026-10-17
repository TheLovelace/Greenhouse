package org.greenhouse.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

data class CompraFornecedorCreateRequestDTO(
    @field:Positive(message = "O id do funcionário deve ser maior que zero")
    val funcionarioId: Long,
    @field:Positive(message = "O id do fornecedor deve ser maior que zero")
    val fornecedorId: Long,
    @field:Positive(message = "O id da forma de pagamento deve ser maior que zero")
    val formaPagamentoId: Long,
    val dataCompra: OffsetDateTime? = null,
    @field:DecimalMin(value = "0", message = "O valor do frete não pode ser negativo")
    @field:Digits(integer = 10, fraction = 2, message = "O valor do frete deve respeitar o formato NUMERIC(12,2)")
    val valorFrete: BigDecimal? = null,
    val freteIncluso: Boolean = false,
    val dataEntregaPrevista: LocalDate? = null,
    @field:Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
    val observacao: String? = null,
    @field:NotEmpty(message = "A compra deve ter ao menos um item")
    @field:Valid
    val itens: List<CompraItemRequestDTO>,
)

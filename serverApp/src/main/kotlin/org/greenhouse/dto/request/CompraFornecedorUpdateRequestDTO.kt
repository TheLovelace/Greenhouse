package org.greenhouse.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

data class CompraFornecedorUpdateRequestDTO(
    val id: Long? = null,
    @field:Positive(message = "O id do funcionário deve ser maior que zero")
    val funcionarioId: Long? = null,
    @field:Positive(message = "O id do fornecedor deve ser maior que zero")
    val fornecedorId: Long? = null,
    @field:Positive(message = "O id da forma de pagamento deve ser maior que zero")
    val formaPagamentoId: Long? = null,
    val dataCompra: OffsetDateTime? = null,
    @field:DecimalMin(value = "0", message = "O valor do frete não pode ser negativo")
    @field:Digits(integer = 10, fraction = 2, message = "O valor do frete deve respeitar o formato NUMERIC(12,2)")
    val valorFrete: BigDecimal? = null,
    val freteIncluso: Boolean? = null,
    val dataEntregaPrevista: LocalDate? = null,
    @field:Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
    val observacao: String? = null,
    @field:Valid
    val itens: List<CompraItemRequestDTO>? = null,
) {
    @get:JsonIgnore
    @get:AssertTrue(message = "Informe ao menos um campo para atualizar")
    val possuiCamposParaAtualizar: Boolean
        get() =
            funcionarioId != null ||
                fornecedorId != null ||
                formaPagamentoId != null ||
                dataCompra != null ||
                valorFrete != null ||
                freteIncluso != null ||
                dataEntregaPrevista != null ||
                observacao != null ||
                itens != null
}

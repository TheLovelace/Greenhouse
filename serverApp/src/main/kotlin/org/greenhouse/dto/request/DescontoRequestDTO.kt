package org.greenhouse.dto.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.greenhouse.enums.TipoDesconto
import java.math.BigDecimal
import java.time.OffsetDateTime

data class DescontoRequestDTO(
    @field:NotBlank(message = "O título do desconto é obrigatório")
    @field:Size(max = 150, message = "O título do desconto deve ter no máximo 150 caracteres")
    val titulo: String,
    @field:Size(max = 1000, message = "A descrição do desconto deve ter no máximo 1000 caracteres")
    val descricao: String? = null,
    val tipo: TipoDesconto,
    @field:DecimalMin(value = "0", inclusive = false, message = "O valor do desconto deve ser maior que zero")
    @field:Digits(integer = 10, fraction = 2, message = "O valor deve respeitar o formato NUMERIC(12,2)")
    val valor: BigDecimal,
    val dataInicio: OffsetDateTime? = null,
    val dataFim: OffsetDateTime? = null,
    val ativo: Boolean = true,
    @field:Min(value = 1, message = "A quantidade mínima de itens deve ser maior que zero")
    val quantidadeMinimaItens: Int = 1,
)

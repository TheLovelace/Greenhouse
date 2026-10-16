package org.greenhouse.dto.response

import org.greenhouse.enums.StatusCompra
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

data class CompraFornecedorResponseDTO(
    val id: Long,
    val funcionarioId: Long,
    val fornecedorId: Long,
    val formaPagamentoId: Long,
    val dataCompra: OffsetDateTime,
    val status: StatusCompra,
    val valorTotal: BigDecimal,
    val valorFrete: BigDecimal?,
    val freteIncluso: Boolean,
    val dataEntregaPrevista: LocalDate?,
    val observacao: String?,
    val itens: List<CompraItemResponseDTO>,
)

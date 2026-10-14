package org.greenhouse.dto.response

import org.greenhouse.enums.TipoDesconto
import java.math.BigDecimal
import java.time.OffsetDateTime

data class DescontoResponseDTO(
    val id: Long,
    val titulo: String,
    val descricao: String?,
    val tipo: TipoDesconto,
    val valor: BigDecimal,
    val dataInicio: OffsetDateTime?,
    val dataFim: OffsetDateTime?,
    val ativo: Boolean,
    val quantidadeMinimaItens: Int,
)

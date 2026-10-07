package org.greenhouse.dto

import org.greenhouse.enums.TipoDesconto
import java.math.BigDecimal
import java.time.OffsetDateTime

data class DescontoDTO(
    val id: Long? = null,
    val titulo: String,
    val descricao: String? = null,
    val tipo: TipoDesconto,
    val valor: BigDecimal,
    val dataInicio: OffsetDateTime? = null,
    val dataFim: OffsetDateTime? = null,
    val ativo: Boolean = true,
)

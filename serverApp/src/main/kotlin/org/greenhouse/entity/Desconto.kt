package org.greenhouse.entity

import java.math.BigDecimal
import java.time.OffsetDateTime

enum class TipoDesconto {
    PERCENTUAL,
    VALOR_FIXO,
}

data class Desconto(
    val id: Long? = null,
    val titulo: String,
    val descricao: String? = null,
    val tipo: TipoDesconto,
    val valor: BigDecimal,
    val dataInicio: OffsetDateTime? = null,
    val dataFim: OffsetDateTime? = null,
    val ativo: Boolean = true,
)

package org.greenhouse.dto.request

import org.greenhouse.enums.TipoDesconto
import java.math.BigDecimal
import java.time.OffsetDateTime

data class DescontoRequestDTO(
    val titulo: String,
    val descricao: String? = null,
    val tipo: TipoDesconto,
    val valor: BigDecimal,
    val dataInicio: OffsetDateTime? = null,
    val dataFim: OffsetDateTime? = null,
    val ativo: Boolean = true,
    val quantidadeMinimaItens: Int = 1,
)

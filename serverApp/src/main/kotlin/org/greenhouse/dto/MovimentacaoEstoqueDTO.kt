package org.greenhouse.dto

import org.greenhouse.enums.TipoMovimentacaoEstoque
import java.time.OffsetDateTime

data class MovimentacaoEstoqueDTO(
    val id: Long? = null,
    val produtoId: Long,
    val pedidoId: Long? = null,
    val compraId: Long? = null,
    val funcionarioId: Long? = null,
    val tipo: TipoMovimentacaoEstoque,
    val quantidade: Int,
    val observacao: String? = null,
    val dataHora: OffsetDateTime? = null,
)

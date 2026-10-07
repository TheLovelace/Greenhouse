package org.greenhouse.controller

import org.greenhouse.enums.TipoMovimentacaoEstoque

data class RegistrarMovimentacaoEstoqueRequest(
    val produtoId: Long,
    val tipo: TipoMovimentacaoEstoque,
    val quantidade: Int,
    val observacao: String? = null,
)

package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class TipoDesconto {
    PERCENTUAL,
    VALOR_FIXO,
}

@Serializable
data class Desconto(
    val id: Long = 0,
    val titulo: String = "",
    val descricao: String? = null,
    val tipo: TipoDesconto = TipoDesconto.PERCENTUAL,
    val valor: Double = 0.0,
    val dataInicio: String? = null,
    val dataFim: String? = null,
    val ativo: Boolean = true,
    val quantidadeMinimaItens: Int = 1,
)

@Serializable
data class DescontoRequest(
    val titulo: String,
    val descricao: String?,
    val tipo: TipoDesconto,
    val valor: Double,
    val dataInicio: String?,
    val dataFim: String?,
    val ativo: Boolean,
    val quantidadeMinimaItens: Int,
)

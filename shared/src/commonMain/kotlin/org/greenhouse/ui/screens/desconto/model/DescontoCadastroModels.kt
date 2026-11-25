package org.greenhouse.ui.screens.desconto.model

data class DescontoCadastroData(
    val titulo: String,
    val descricao: String?,
    val tipo: TipoDescontoCadastro,
    val valor: String,
    val dataInicio: String?,
    val dataFim: String?,
    val ativo: Boolean,
    val quantidadeMinimaItens: String,
)

data class DescontoItem(
    val id: Long,
    val titulo: String,
    val descricao: String?,
    val tipo: TipoDescontoCadastro,
    val valor: String,
    val dataInicio: String?,
    val dataFim: String?,
    val ativo: Boolean,
    val quantidadeMinimaItens: Int,
)

enum class TipoDescontoCadastro {
    PERCENTUAL,
    VALOR_FIXO,
}

data class DescontoCadastroForm(
    val titulo: String = "",
    val descricao: String = "",
    val tipo: TipoDescontoCadastro = TipoDescontoCadastro.PERCENTUAL,
    val valor: String = "",
    val dataInicio: String = "",
    val dataFim: String = "",
    val ativo: Boolean = true,
    val quantidadeMinimaItens: String = "1",
)

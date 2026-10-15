package org.greenhouse.ui.screens.estoque.model

data class EstoqueCadastroData(
    val produtoId: Long,
    val quantidadeFisica: Int,
    val quantidadeMinima: Int,
    val localizacao: String?,
    val bloqueado: Boolean,
)

data class EstoqueCadastroForm(
    val produtoId: String = "",
    val quantidadeFisica: String = "0",
    val quantidadeMinima: String = "0",
    val localizacao: String = "",
    val bloqueado: Boolean = false,
)

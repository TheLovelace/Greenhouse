package org.greenhouse.ui.screens.categoria.model

data class CategoriaCadastroData(
    val nome: String,
    val descricao: String,
    val ativo: Boolean,
    val observacao: String?,
)

data class CategoriaCadastroForm(
    val nome: String = "",
    val descricao: String = "",
    val ativo: Boolean = true,
    val observacao: String = "",
)

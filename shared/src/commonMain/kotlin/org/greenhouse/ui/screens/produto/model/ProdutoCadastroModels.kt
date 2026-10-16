package org.greenhouse.ui.screens.produto.model

data class ProdutoCadastroData(
    val codigo: String?,
    val nome: String,
    val descricao: String?,
    val preco: String,
    val ativo: Boolean,
    val prazoPreparoMinutos: Int?,
)

data class ProdutoCadastroForm(
    val codigo: String = "",
    val nome: String = "",
    val descricao: String = "",
    val preco: String = "",
    val ativo: Boolean = true,
    val prazoPreparoMinutos: String = "",
)

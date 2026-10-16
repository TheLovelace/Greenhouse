package org.greenhouse.ui.screens.formaPagamento.model

data class FormaPagamentoCadastroData(
    val nome: String,
    val ativo: Boolean,
    val descricao: String?,
    val limiteParcelas: Int,
    val prazoEstornoDias: Int?,
)

data class FormaPagamentoCadastroForm(
    val nome: String = "",
    val ativo: Boolean = true,
    val descricao: String = "",
    val limiteParcelas: String = "1",
    val prazoEstornoDias: String = "",
)

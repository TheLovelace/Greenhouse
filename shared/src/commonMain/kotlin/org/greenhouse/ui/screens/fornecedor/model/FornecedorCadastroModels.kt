package org.greenhouse.ui.screens.fornecedor.model

data class FornecedorCadastroData(
    val nome: String,
    val cnpj: String?,
    val telefone: String?,
    val email: String?,
    val contato: String?,
    val ativo: Boolean,
    val prazoMedioEntregaDias: Int?,
)

data class FornecedorCadastroForm(
    val nome: String = "",
    val cnpj: String = "",
    val telefone: String = "",
    val email: String = "",
    val contato: String = "",
    val ativo: Boolean = true,
    val prazoMedioEntregaDias: String = "",
)

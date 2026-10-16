package org.greenhouse.ui.screens.funcionario.model

data class FuncionarioCadastroData(
    val nome: String,
    val cpf: String,
    val telefone: String?,
    val email: String,
    val cargo: String?,
    val ativo: Boolean,
    val dataDeNascimento: String?,
)

data class FuncionarioCadastroForm(
    val nome: String = "",
    val cpf: String = "",
    val telefone: String = "",
    val email: String = "",
    val cargo: String = "",
    val ativo: Boolean = true,
    val dataDeNascimento: String = "",
)

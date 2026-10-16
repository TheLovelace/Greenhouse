package org.greenhouse.ui.screens.cliente.model

data class ClienteCadastroData(
    val nome: String,
    val telefone: String,
    val whatsapp: Boolean,
    val cpf: String?,
    val email: String?,
    val ativo: Boolean,
)

data class ClienteCadastroForm(
    val nome: String = "",
    val telefone: String = "",
    val whatsapp: Boolean = false,
    val cpf: String = "",
    val email: String = "",
    val ativo: Boolean = true,
)

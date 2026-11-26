package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Funcionario(
    val id: Long = 0,
    val nome: String = "",
    val cpf: String = "",
    val telefone: String? = null,
    val email: String? = null,
    val cargo: String? = null,
    val ativo: Boolean = true,
    val dataDeNascimento: String? = null,
)

@Serializable
data class FuncionarioRequest(
    val nome: String,
    val cpf: String,
    val telefone: String?,
    val email: String,
    val cargo: String?,
    val ativo: Boolean,
    val dataDeNascimento: String?,
)

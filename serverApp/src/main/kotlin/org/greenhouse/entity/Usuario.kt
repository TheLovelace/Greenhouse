package org.greenhouse.entity

data class Usuario(
    val id: Long? = null,
    val funcionarioId: Long,
    val login: String,
    val senhaHash: String,
    val perfil: String,
    val ativo: Boolean = true,
    val primeiroAcessoFeito: Boolean = false,
)
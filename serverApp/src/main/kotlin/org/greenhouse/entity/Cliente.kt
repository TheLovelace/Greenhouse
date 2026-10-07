package org.greenhouse.entity

data class Cliente(
    val id: Long? = null,
    val nome: String,
    val telefone: String,
    val whatsapp: Boolean = false,
    val cpf: String? = null,
    val email: String? = null,
    val ativo: Boolean = true,
)

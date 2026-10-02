package org.greenhouse.entity

data class Categoria(
    val id: Long? = null,
    val nome: String,
    val descricao: String,
    val ativo: Boolean = true,
)
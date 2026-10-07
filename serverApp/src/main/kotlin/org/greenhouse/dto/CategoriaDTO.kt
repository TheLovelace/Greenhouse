package org.greenhouse.dto

data class CategoriaDTO(
    val id: Long? = null,
    val nome: String,
    val descricao: String,
    val ativo: Boolean = true,
)

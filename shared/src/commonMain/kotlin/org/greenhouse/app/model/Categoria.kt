package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Categoria(
    val id: Long = 0,
    val nome: String = "",
    val descricao: String = "",
    val ativo: Boolean = true,
    val observacao: String? = null,
    val dataCadastro: String? = null,
)

package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Produto(
    val id: Long = 0,
    val codigo: String? = null,
    val nome: String = "",
    val descricao: String? = null,
    val preco: Double = 0.0,
    val ativo: Boolean = true,
    val prazoPreparoMinutos: Int? = null,
)

package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class FormaPagamento(
    val id: Long = 0,
    val nome: String = "",
    val ativo: Boolean = true,
    val descricao: String? = null,
    val limiteParcelas: Int = 1,
    val prazoEstornoDias: Int? = null,
)

@Serializable
data class FormaPagamentoRequest(
    val nome: String,
    val ativo: Boolean,
    val descricao: String?,
    val limiteParcelas: Int,
    val prazoEstornoDias: Int?,
)

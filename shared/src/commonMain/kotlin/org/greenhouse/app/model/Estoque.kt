package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Estoque(
    val produtoId: Long = 0,
    val quantidadeFisica: Int = 0,
    val quantidadeReservada: Int = 0,
    val quantidadeDisponivel: Int = 0,
    val quantidadeMinima: Int = 0,
    val localizacao: String? = null,
    val bloqueado: Boolean = false,
)

@Serializable
data class EstoqueCreateRequest(
    val produtoId: Long,
    val quantidadeFisica: Int,
    val quantidadeMinima: Int,
    val localizacao: String?,
    val bloqueado: Boolean,
)

@Serializable
data class EstoqueUpdateRequest(
    val quantidadeFisica: Int,
    val quantidadeMinima: Int,
    val localizacao: String?,
    val bloqueado: Boolean,
)

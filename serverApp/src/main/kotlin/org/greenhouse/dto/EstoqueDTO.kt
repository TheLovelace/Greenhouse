package org.greenhouse.dto

data class EstoqueDTO(
    val produtoId: Long,
    val quantidadeFisica: Int,
    val quantidadeReservada: Int,
    val quantidadeDisponivel: Int,
    val quantidadeMinima: Int,
    val localizacao: String?,
    val bloqueado: Boolean,
)

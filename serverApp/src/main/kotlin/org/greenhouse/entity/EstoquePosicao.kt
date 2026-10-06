package org.greenhouse.entity

data class EstoquePosicao(
    val produtoId: Long,
    val quantidadeFisica: Int,
    val quantidadeReservada: Int,
    val quantidadeDisponivel: Int,
    val quantidadeMinima: Int,
)

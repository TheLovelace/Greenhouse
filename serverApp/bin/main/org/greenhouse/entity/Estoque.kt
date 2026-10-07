package org.greenhouse.entity

data class Estoque(
    val produtoId: Long,
    val quantidadeFisica: Int,
    val quantidadeReservada: Int,
    val quantidadeDisponivel: Int,
    val quantidadeMinima: Int,
)

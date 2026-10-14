package org.greenhouse.dto.response

data class EstoqueResponseDTO(
    val produtoId: Long,
    val quantidadeFisica: Int,
    val quantidadeReservada: Int,
    val quantidadeDisponivel: Int,
    val quantidadeMinima: Int,
    val localizacao: String?,
    val bloqueado: Boolean,
)

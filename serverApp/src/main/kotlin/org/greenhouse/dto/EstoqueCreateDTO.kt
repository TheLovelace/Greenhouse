package org.greenhouse.dto

data class EstoqueCreateDTO(
    val produtoId: Long,
    val quantidadeFisica: Int = 0,
    val quantidadeMinima: Int = 0,
    val localizacao: String? = null,
    val bloqueado: Boolean = false,
)

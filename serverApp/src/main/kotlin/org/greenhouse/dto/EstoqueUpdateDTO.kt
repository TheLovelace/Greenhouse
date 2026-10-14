package org.greenhouse.dto

data class EstoqueUpdateDTO(
    val quantidadeFisica: Int,
    val quantidadeMinima: Int,
    val localizacao: String? = null,
    val bloqueado: Boolean,
)

package org.greenhouse.dto.request

data class EstoqueUpdateRequestDTO(
    val quantidadeFisica: Int,
    val quantidadeMinima: Int,
    val localizacao: String? = null,
    val bloqueado: Boolean,
)

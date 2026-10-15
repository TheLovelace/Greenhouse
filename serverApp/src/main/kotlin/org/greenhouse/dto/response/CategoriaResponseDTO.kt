package org.greenhouse.dto.response

import java.time.LocalDate

data class CategoriaResponseDTO(
    val id: Long,
    val nome: String,
    val descricao: String,
    val ativo: Boolean,
    val observacao: String?,
    val dataCadastro: LocalDate,
)

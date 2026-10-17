package org.greenhouse.dto

data class DispositivoIotResponseDTO(
    val id: Long,
    val codigo: String,
    val nome: String,
    val localizacao: String?,
    val ativo: Boolean,
)
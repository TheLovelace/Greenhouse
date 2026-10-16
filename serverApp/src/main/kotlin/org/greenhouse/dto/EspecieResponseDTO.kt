package org.greenhouse.dto

import java.math.BigDecimal

data class EspecieResponseDTO(
    val id: Long,
    val nomePopular: String,
    val nomeCientifico: String?,
    val descricao: String?,
    val intervaloRegaDias: Int?,
    val ativo: Boolean,
    val temperaturaMin: BigDecimal?,
    val temperaturaMax: BigDecimal?,
    val umidadeArMin: BigDecimal?,
    val umidadeArMax: BigDecimal?,
    val umidadeSoloMin: BigDecimal?,
    val umidadeSoloMax: BigDecimal?,
)
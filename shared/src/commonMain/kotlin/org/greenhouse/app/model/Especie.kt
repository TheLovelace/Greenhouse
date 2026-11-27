package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Especie(
    val id: Long = 0,
    val nomePopular: String = "",
    val nomeCientifico: String? = null,
    val descricao: String? = null,
    val intervaloRegaDias: Int? = null,
    val ativo: Boolean = true,
    val temperaturaMin: Double? = null,
    val temperaturaMax: Double? = null,
    val umidadeArMin: Double? = null,
    val umidadeArMax: Double? = null,
    val umidadeSoloMin: Double? = null,
    val umidadeSoloMax: Double? = null,
)

@Serializable
data class EspecieRequest(
    val nomePopular: String,
    val nomeCientifico: String,
    val descricao: String?,
    val intervaloRegaDias: Int,
    val temperaturaMin: Double,
    val temperaturaMax: Double,
    val umidadeArMin: Double?,
    val umidadeArMax: Double?,
    val umidadeSoloMin: Double?,
    val umidadeSoloMax: Double?,
)
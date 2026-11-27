package org.greenhouse.ui.screens.especie.model

data class EspecieCadastroForm(
    val nomePopular: String = "",
    val nomeCientifico: String = "",
    val descricao: String = "",
    val intervaloRegaDias: String = "",
    val temperaturaMin: String = "",
    val temperaturaMax: String = "",
    val umidadeArMin: String = "",
    val umidadeArMax: String = "",
    val umidadeSoloMin: String = "",
    val umidadeSoloMax: String = "",
)

data class EspecieCadastroData(
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

data class EspecieItem(
    val id: Long,
    val nomePopular: String,
    val nomeCientifico: String?,
    val descricao: String?,
    val intervaloRegaDias: Int?,
    val ativo: Boolean,
    val temperaturaMin: Double?,
    val temperaturaMax: Double?,
    val umidadeArMin: Double?,
    val umidadeArMax: Double?,
    val umidadeSoloMin: Double?,
    val umidadeSoloMax: Double?,
)
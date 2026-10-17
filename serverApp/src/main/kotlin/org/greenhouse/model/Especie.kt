package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "especie")
data class Especie(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:Column(name = "nome_popular", nullable = false, length = 150)
    var nomePopular: String = "",

    @field:Column(name = "nome_cientifico", length = 150)
    var nomeCientifico: String? = null,

    @field:Column(name = "descricao", length = 2000)
    var descricao: String? = null,

    @field:Column(name = "temperatura_min", precision = 5, scale = 2)
    var temperaturaMin: BigDecimal? = null,

    @field:Column(name = "temperatura_max", precision = 5, scale = 2)
    var temperaturaMax: BigDecimal? = null,

    @field:Column(name = "umidade_ar_min", precision = 5, scale = 2)
    var umidadeArMin: BigDecimal? = null,

    @field:Column(name = "umidade_ar_max", precision = 5, scale = 2)
    var umidadeArMax: BigDecimal? = null,

    @field:Column(name = "umidade_solo_min", precision = 5, scale = 2)
    var umidadeSoloMin: BigDecimal? = null,

    @field:Column(name = "umidade_solo_max", precision = 5, scale = 2)
    var umidadeSoloMax: BigDecimal? = null,

    @field:Column(name = "ativo", nullable = false)
    var ativo: Boolean = true,

    @field:Column(name = "intervalo_rega_dias")
    var intervaloRegaDias: Int? = null,
)
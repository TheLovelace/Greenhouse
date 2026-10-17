package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "alerta")
data class Alerta(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:ManyToOne(fetch = FetchType.LAZY, optional = false)
    @field:JoinColumn(
        name = "dispositivo_iot_id",
        nullable = false
    )
    var dispositivo: DispositivoIot = DispositivoIot(),

    @field:ManyToOne(fetch = FetchType.LAZY, optional = false)
    @field:JoinColumn(name = "especie_id", nullable = false)
    var especie: Especie = Especie(),

    @field:Column(
        name = "titulo",
        nullable = false,
        length = 150
    )
    var titulo: String = "",

    @field:Column(name = "mensagem", length = 1000)
    var mensagem: String? = null,

    @field:Column(
        name = "monitorar_temperatura",
        nullable = false
    )
    var monitorarTemperatura: Boolean = false,

    @field:Column(
        name = "monitorar_umidade_ar",
        nullable = false
    )
    var monitorarUmidadeAr: Boolean = false,

    @field:Column(
        name = "monitorar_umidade_solo",
        nullable = false
    )
    var monitorarUmidadeSolo: Boolean = false,

    @field:Column(name = "ativo", nullable = false)
    var ativo: Boolean = true,

    @field:Column(name = "prioridade", nullable = false)
    var prioridade: Int = 2,
)
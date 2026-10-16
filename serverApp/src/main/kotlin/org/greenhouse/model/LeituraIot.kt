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
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "leitura_iot")
data class LeituraIot(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:ManyToOne(fetch = FetchType.LAZY, optional = false)
    @field:JoinColumn(
        name = "dispositivo_iot_id",
        nullable = false
    )
    var dispositivo: DispositivoIot = DispositivoIot(),

    @field:Column(
        name = "temperatura",
        nullable = false,
        precision = 5,
        scale = 2
    )
    var temperatura: BigDecimal = BigDecimal.ZERO,

    @field:Column(
        name = "umidade_ar",
        nullable = false,
        precision = 5,
        scale = 2
    )
    var umidadeAr: BigDecimal = BigDecimal.ZERO,

    @field:Column(
        name = "umidade_solo",
        nullable = false,
        precision = 5,
        scale = 2
    )
    var umidadeSolo: BigDecimal = BigDecimal.ZERO,

    @field:Column(name = "umidade_solo_raw", nullable = false)
    var umidadeSoloRaw: Int = 0,

    @field:Column(name = "data_leitura", nullable = false)
    var dataLeitura: OffsetDateTime = OffsetDateTime.now(),
)
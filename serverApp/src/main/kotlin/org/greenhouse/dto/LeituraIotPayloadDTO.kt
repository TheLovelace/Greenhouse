package org.greenhouse.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class LeituraIotPayloadDTO(
        val temperatura: BigDecimal,
        val umidadeAr: BigDecimal,
        val umidadeSoloRaw: Int,
        val dataLeitura: OffsetDateTime? = null,
)

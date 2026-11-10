package org.greenhouse.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import org.greenhouse.enums.CondicaoAmbiental
import org.greenhouse.enums.ParametroAmbiental

data class AvaliacaoLeituraDTO(
        val leituraId: Long?,
        val dispositivoId: Long,
        val temperatura: BigDecimal,
        val umidadeAr: BigDecimal,
        val umidadeSolo: BigDecimal,
        val dataLeitura: OffsetDateTime,
        val ocorrencias: List<OcorrenciaAlertaDTO>,
)

data class OcorrenciaAlertaDTO(
        val alertaId: Long?,
        val titulo: String,
        val prioridade: Int,
        val especieId: Long?,
        val parametro: ParametroAmbiental,
        val valorMedido: BigDecimal,
        val condicao: CondicaoAmbiental,
)

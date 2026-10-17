package org.greenhouse.dto

data class AlertaResponseDTO(
    val id: Long,
    val dispositivoIotId: Long,
    val dispositivoCodigo: String,
    val dispositivoNome: String,
    val especieId: Long,
    val especieNomePopular: String,
    val titulo: String,
    val mensagem: String?,
    val prioridade: Int,
    val monitorarTemperatura: Boolean,
    val monitorarUmidadeAr: Boolean,
    val monitorarUmidadeSolo: Boolean,
    val ativo: Boolean,
)
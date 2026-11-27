package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Alerta(
    val id: Long = 0,
    val dispositivoIotId: Long = 0,
    val dispositivoCodigo: String = "",
    val dispositivoNome: String = "",
    val especieId: Long = 0,
    val especieNomePopular: String = "",
    val titulo: String = "",
    val mensagem: String? = null,
    val prioridade: Int = 2,
    val monitorarTemperatura: Boolean = false,
    val monitorarUmidadeAr: Boolean = false,
    val monitorarUmidadeSolo: Boolean = false,
    val ativo: Boolean = true,
)

@Serializable
data class AlertaRequest(
    val dispositivoIotId: Long,
    val especieId: Long,
    val titulo: String,
    val mensagem: String?,
    val prioridade: Int = 2,
    val monitorarTemperatura: Boolean = false,
    val monitorarUmidadeAr: Boolean = false,
    val monitorarUmidadeSolo: Boolean = false,
)

@Serializable
data class DispositivoIotResumo(
    val id: Long,
    val codigo: String,
    val nome: String,
    val localizacao: String? = null,
    val ativo: Boolean = true,
)
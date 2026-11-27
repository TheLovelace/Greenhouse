package org.greenhouse.ui.screens.alerta.model

data class AlertaCadastroForm(
    val dispositivoIotId: String = "",
    val especieId: String = "",
    val titulo: String = "",
    val mensagem: String = "",
    val prioridade: String = "2",
    val monitorarTemperatura: Boolean = true,
    val monitorarUmidadeAr: Boolean = false,
    val monitorarUmidadeSolo: Boolean = false,
)

data class AlertaCadastroData(
    val dispositivoIotId: Long,
    val especieId: Long,
    val titulo: String,
    val mensagem: String?,
    val prioridade: Int,
    val monitorarTemperatura: Boolean,
    val monitorarUmidadeAr: Boolean,
    val monitorarUmidadeSolo: Boolean,
)

data class AlertaItem(
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
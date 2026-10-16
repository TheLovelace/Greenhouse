package org.greenhouse.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class AlertaRequestDTO(
    @field:Positive(
        message = "O ID do dispositivo IoT deve ser maior que zero"
    )
    val dispositivoIotId: Long,

    @field:Positive(
        message = "O ID da espécie deve ser maior que zero"
    )
    val especieId: Long,

    @field:NotBlank(
        message = "O título do alerta é obrigatório"
    )
    @field:Size(
        max = 150,
        message = "O título deve ter no máximo 150 caracteres"
    )
    val titulo: String,

    @field:Size(
        max = 1000,
        message = "A mensagem deve ter no máximo 1000 caracteres"
    )
    val mensagem: String? = null,

    @field:Min(
        value = 1,
        message = "A prioridade deve ser 1, 2 ou 3"
    )
    @field:Max(
        value = 3,
        message = "A prioridade deve ser 1, 2 ou 3"
    )
    val prioridade: Int = 2,

    val monitorarTemperatura: Boolean = false,
    val monitorarUmidadeAr: Boolean = false,
    val monitorarUmidadeSolo: Boolean = false,
)
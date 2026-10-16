package org.greenhouse.mapper

import org.greenhouse.dto.AlertaRequestDTO
import org.greenhouse.dto.AlertaResponseDTO
import org.greenhouse.dto.DispositivoIotResponseDTO
import org.greenhouse.model.Alerta
import org.greenhouse.model.DispositivoIot
import org.greenhouse.model.Especie
import org.springframework.stereotype.Component

@Component
class AlertaMapper {

    fun toEntity(dto: AlertaRequestDTO, dispositivo: DispositivoIot, especie: Especie): Alerta =
        Alerta().also { updateEntity(it, dto, dispositivo, especie) }

    fun updateEntity(entity: Alerta, dto: AlertaRequestDTO, dispositivo: DispositivoIot, especie: Especie) {
        entity.dispositivo = dispositivo
        entity.especie = especie
        entity.titulo = dto.titulo.trim()
        entity.mensagem = dto.mensagem?.trim()?.ifBlank { null }
        entity.prioridade = dto.prioridade
        entity.monitorarTemperatura = dto.monitorarTemperatura
        entity.monitorarUmidadeAr = dto.monitorarUmidadeAr
        entity.monitorarUmidadeSolo = dto.monitorarUmidadeSolo
    }

    fun toResponseDto(entity: Alerta): AlertaResponseDTO =
        AlertaResponseDTO(
            id = requireNotNull(entity.id) { "Alerta sem id após persistir" },
            dispositivoIotId = requireNotNull(entity.dispositivo.id),
            dispositivoCodigo = entity.dispositivo.codigo,
            dispositivoNome = entity.dispositivo.nome,
            especieId = requireNotNull(entity.especie.id),
            especieNomePopular = entity.especie.nomePopular,
            titulo = entity.titulo,
            mensagem = entity.mensagem,
            prioridade = entity.prioridade,
            monitorarTemperatura = entity.monitorarTemperatura,
            monitorarUmidadeAr = entity.monitorarUmidadeAr,
            monitorarUmidadeSolo = entity.monitorarUmidadeSolo,
            ativo = entity.ativo,
        )

    fun toDispositivoDto(dispositivo: DispositivoIot): DispositivoIotResponseDTO =
        DispositivoIotResponseDTO(
            id = requireNotNull(dispositivo.id),
            codigo = dispositivo.codigo,
            nome = dispositivo.nome,
            localizacao = dispositivo.localizacao,
            ativo = dispositivo.ativo,
        )
}
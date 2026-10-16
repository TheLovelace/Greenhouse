package org.greenhouse.mapper

import org.greenhouse.dto.EspecieRequestDTO
import org.greenhouse.dto.EspecieResponseDTO
import org.greenhouse.model.Especie
import org.springframework.stereotype.Component

@Component
class EspecieMapper {

    fun toEntity(dto: EspecieRequestDTO): Especie =
        Especie().also { updateEntity(it, dto) }

    fun updateEntity(entity: Especie, dto: EspecieRequestDTO) {
        entity.nomePopular = dto.nomePopular.trim()
        entity.nomeCientifico = dto.nomeCientifico.trim()
        entity.descricao = dto.descricao
        entity.intervaloRegaDias = dto.intervaloRegaDias
        entity.temperaturaMin = dto.temperaturaMin
        entity.temperaturaMax = dto.temperaturaMax
        entity.umidadeArMin = dto.umidadeArMin
        entity.umidadeArMax = dto.umidadeArMax
        entity.umidadeSoloMin = dto.umidadeSoloMin
        entity.umidadeSoloMax = dto.umidadeSoloMax
    }

    fun toResponseDto(entity: Especie): EspecieResponseDTO =
        EspecieResponseDTO(
            id = requireNotNull(entity.id) { "Espécie sem id após persistir" },
            nomePopular = entity.nomePopular,
            nomeCientifico = entity.nomeCientifico,
            descricao = entity.descricao,
            intervaloRegaDias = entity.intervaloRegaDias,
            ativo = entity.ativo,
            temperaturaMin = entity.temperaturaMin,
            temperaturaMax = entity.temperaturaMax,
            umidadeArMin = entity.umidadeArMin,
            umidadeArMax = entity.umidadeArMax,
            umidadeSoloMin = entity.umidadeSoloMin,
            umidadeSoloMax = entity.umidadeSoloMax,
        )
}
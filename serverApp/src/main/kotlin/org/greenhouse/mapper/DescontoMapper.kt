package org.greenhouse.mapper

import org.greenhouse.dto.request.DescontoRequestDTO
import org.greenhouse.dto.response.DescontoResponseDTO
import org.greenhouse.model.Desconto
import org.springframework.stereotype.Component

@Component
class DescontoMapper {
    fun toResponseDto(model: Desconto) =
        DescontoResponseDTO(
            id = requireNotNull(model.id),
            titulo = model.titulo,
            descricao = model.descricao,
            tipo = model.tipo,
            valor = model.valor,
            dataInicio = model.dataInicio,
            dataFim = model.dataFim,
            ativo = model.ativo,
            quantidadeMinimaItens = model.quantidadeMinimaItens,
        )

    fun toModel(dto: DescontoRequestDTO) =
        Desconto(
            titulo = dto.titulo,
            descricao = dto.descricao,
            tipo = dto.tipo,
            valor = dto.valor,
            dataInicio = dto.dataInicio,
            dataFim = dto.dataFim,
            ativo = dto.ativo,
            quantidadeMinimaItens = dto.quantidadeMinimaItens,
        )
}

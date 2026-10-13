package org.greenhouse.mapper

import org.greenhouse.dto.DescontoDTO
import org.greenhouse.model.Desconto
import org.springframework.stereotype.Component

@Component
class DescontoMapper {
    fun toDto(model: Desconto) =
        DescontoDTO(
            model.id,
            model.titulo,
            model.descricao,
            model.tipo,
            model.valor,
            model.dataInicio,
            model.dataFim,
            model.ativo,
            model.quantidadeMinimaItens,
        )

    fun toModel(dto: DescontoDTO) =
        Desconto(
            dto.id,
            dto.titulo,
            dto.descricao,
            dto.tipo,
            dto.valor,
            dto.dataInicio,
            dto.dataFim,
            dto.ativo,
            dto.quantidadeMinimaItens,
        )
}

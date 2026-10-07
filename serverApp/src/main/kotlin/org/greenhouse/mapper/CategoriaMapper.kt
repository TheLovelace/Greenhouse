package org.greenhouse.mapper

import org.greenhouse.dto.CategoriaDTO
import org.greenhouse.model.Categoria
import org.springframework.stereotype.Component

@Component
class CategoriaMapper {
    fun toDto(model: Categoria) =
        CategoriaDTO(model.id, model.nome, model.descricao, model.ativo)

    fun toModel(dto: CategoriaDTO) =
        Categoria(dto.id, dto.nome, dto.descricao, dto.ativo)
}

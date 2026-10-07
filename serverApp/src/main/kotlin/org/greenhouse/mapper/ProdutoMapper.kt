package org.greenhouse.mapper

import org.greenhouse.dto.ProdutoDTO
import org.greenhouse.model.Produto
import org.springframework.stereotype.Component

@Component
class ProdutoMapper {
    fun toDto(model: Produto) =
        ProdutoDTO(model.id, model.codigo, model.nome, model.descricao, model.preco, model.ativo)

    fun toModel(dto: ProdutoDTO) =
        Produto(dto.id, dto.codigo, dto.nome, dto.descricao, dto.preco, dto.ativo)
}

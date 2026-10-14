package org.greenhouse.mapper

import org.greenhouse.dto.request.ProdutoCreateRequestDTO
import org.greenhouse.dto.request.ProdutoUpdateRequestDTO
import org.greenhouse.dto.response.ProdutoResponseDTO
import org.greenhouse.model.Produto
import org.springframework.stereotype.Component

@Component
class ProdutoMapper {
    fun toResponse(model: Produto) =
        ProdutoResponseDTO(
            id = requireNotNull(model.id),
            codigo = model.codigo,
            nome = model.nome,
            descricao = model.descricao,
            preco = model.preco,
            ativo = model.ativo,
            prazoPreparoMinutos = model.prazoPreparoMinutos,
        )

    fun toModel(dto: ProdutoCreateRequestDTO) =
        Produto(
            codigo = dto.codigo,
            nome = dto.nome,
            descricao = dto.descricao,
            preco = dto.preco,
            ativo = dto.ativo,
            prazoPreparoMinutos = dto.prazoPreparoMinutos,
        )

    fun toModel(
        dto: ProdutoUpdateRequestDTO,
        current: Produto,
    ) = current.copy(
        codigo = if (dto.codigoInformado) dto.codigo else current.codigo,
        nome = if (dto.nomeInformado) requireNotNull(dto.nome) else current.nome,
        descricao = if (dto.descricaoInformada) dto.descricao else current.descricao,
        preco = if (dto.precoInformado) requireNotNull(dto.preco) else current.preco,
        ativo = if (dto.ativoInformado) requireNotNull(dto.ativo) else current.ativo,
    )
}

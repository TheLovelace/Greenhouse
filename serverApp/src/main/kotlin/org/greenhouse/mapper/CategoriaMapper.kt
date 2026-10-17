package org.greenhouse.mapper

import org.greenhouse.dto.request.CategoriaCreateRequestDTO
import org.greenhouse.dto.request.CategoriaUpdateRequestDTO
import org.greenhouse.dto.response.CategoriaResponseDTO
import org.greenhouse.model.Categoria
import org.springframework.stereotype.Component

@Component
class CategoriaMapper {
    fun toResponse(model: Categoria) =
        CategoriaResponseDTO(
            id = requireNotNull(model.id),
            nome = model.nome,
            descricao = model.descricao,
            ativo = model.ativo,
            observacao = model.observacao,
            dataCadastro = requireNotNull(model.dataCadastro),
        )

    fun toModel(dto: CategoriaCreateRequestDTO) =
        Categoria(
            nome = dto.nome,
            descricao = dto.descricao,
            ativo = dto.ativo,
            observacao = dto.observacao,
            dataCadastro = dto.dataCadastro,
        )

    fun toModel(
        dto: CategoriaUpdateRequestDTO,
        current: Categoria,
    ) = current.copy(
        nome = dto.nome ?: current.nome,
        descricao = dto.descricao ?: current.descricao,
        ativo = dto.ativo ?: current.ativo,
        observacao = if (dto.observacaoInformada) dto.observacao else current.observacao,
    )
}

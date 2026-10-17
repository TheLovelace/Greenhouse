package org.greenhouse.mapper

import org.greenhouse.dto.request.EstoqueCreateRequestDTO
import org.greenhouse.dto.request.EstoqueUpdateRequestDTO
import org.greenhouse.dto.response.EstoqueResponseDTO
import org.greenhouse.model.Estoque
import org.springframework.stereotype.Component

@Component
class EstoqueMapper {
    fun toModel(
        dto: EstoqueCreateRequestDTO,
        produtoId: Long = dto.produtoId,
    ) = Estoque(
        produtoId = produtoId,
        quantidadeFisica = dto.quantidadeFisica,
        quantidadeMinima = dto.quantidadeMinima,
        localizacao = dto.localizacao,
        bloqueado = dto.bloqueado,
    )

    fun updateModel(
        dto: EstoqueUpdateRequestDTO,
        model: Estoque,
    ): Estoque {
        model.quantidadeFisica = dto.quantidadeFisica
        model.quantidadeMinima = dto.quantidadeMinima
        model.localizacao = dto.localizacao
        model.bloqueado = dto.bloqueado
        return model
    }

    fun toResponseDto(model: Estoque) =
        EstoqueResponseDTO(
            produtoId = model.produtoId,
            quantidadeFisica = model.quantidadeFisica,
            quantidadeReservada = model.quantidadeReservada,
            quantidadeDisponivel = model.quantidadeFisica - model.quantidadeReservada,
            quantidadeMinima = model.quantidadeMinima,
            localizacao = model.localizacao,
            bloqueado = model.bloqueado,
        )
}

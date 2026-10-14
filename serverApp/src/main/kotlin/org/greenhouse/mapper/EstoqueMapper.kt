package org.greenhouse.mapper

import org.greenhouse.dto.response.EstoqueResponseDTO
import org.greenhouse.model.Estoque
import org.springframework.stereotype.Component

@Component
class EstoqueMapper {
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

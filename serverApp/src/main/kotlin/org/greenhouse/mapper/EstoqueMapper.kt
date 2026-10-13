package org.greenhouse.mapper

import org.greenhouse.dto.EstoqueDTO
import org.greenhouse.model.Estoque
import org.springframework.stereotype.Component

@Component
class EstoqueMapper {
    fun toDto(model: Estoque) =
        EstoqueDTO(
            produtoId = model.produtoId,
            quantidadeFisica = model.quantidadeFisica,
            quantidadeReservada = model.quantidadeReservada,
            quantidadeDisponivel = model.quantidadeFisica - model.quantidadeReservada,
            quantidadeMinima = model.quantidadeMinima,
            localizacao = model.localizacao,
            bloqueado = model.bloqueado,
        )
}

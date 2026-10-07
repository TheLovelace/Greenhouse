package org.greenhouse.mapper

import org.greenhouse.dto.MovimentacaoEstoqueDTO
import org.greenhouse.model.MovimentacaoEstoque
import org.springframework.stereotype.Component

@Component
class MovimentacaoEstoqueMapper {
    fun toDto(model: MovimentacaoEstoque) =
        MovimentacaoEstoqueDTO(
            id = model.id,
            produtoId = model.produtoId,
            pedidoId = model.pedidoId,
            compraId = model.compraId,
            funcionarioId = model.funcionarioId,
            tipo = model.tipo,
            quantidade = model.quantidade,
            observacao = model.observacao,
            dataHora = model.dataHora,
        )

    fun toModel(dto: MovimentacaoEstoqueDTO) =
        MovimentacaoEstoque(
            id = dto.id,
            produtoId = dto.produtoId,
            pedidoId = dto.pedidoId,
            compraId = dto.compraId,
            funcionarioId = dto.funcionarioId,
            tipo = dto.tipo,
            quantidade = dto.quantidade,
            observacao = dto.observacao,
            dataHora = dto.dataHora,
        )
}

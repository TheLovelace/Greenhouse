package org.greenhouse.mapper

import org.greenhouse.dto.request.PedidoCreateRequestDTO
import org.greenhouse.dto.request.PedidoUpdateRequestDTO
import org.greenhouse.dto.response.PedidoItemResponseDTO
import org.greenhouse.dto.response.PedidoResponseDTO
import org.greenhouse.model.Pedido
import org.greenhouse.model.PedidoItem
import org.springframework.stereotype.Component

@Component
class PedidoMapper {
    fun toModel(dto: PedidoCreateRequestDTO) =
        Pedido(
            clienteId = dto.clienteId,
            funcionarioId = dto.funcionarioId,
            descontoId = dto.descontoId,
            formaPagamentoId = dto.formaPagamentoId,
            modalidade = dto.modalidade,
            dataRetiradaPrevista = dto.dataRetiradaPrevista,
            observacao = dto.observacao,
            embalagemPresente = dto.embalagemPresente,
            numeroVolumes = dto.numeroVolumes,
        )

    fun toModel(
        dto: PedidoUpdateRequestDTO,
        current: Pedido,
    ) = current.copy(
        clienteId = dto.clienteId ?: current.clienteId,
        funcionarioId = dto.funcionarioId ?: current.funcionarioId,
        descontoId = dto.descontoId ?: current.descontoId,
        formaPagamentoId = dto.formaPagamentoId ?: current.formaPagamentoId,
        dataRetiradaPrevista = dto.dataRetiradaPrevista ?: current.dataRetiradaPrevista,
        observacao = dto.observacao ?: current.observacao,
        embalagemPresente = dto.embalagemPresente ?: current.embalagemPresente,
        numeroVolumes = dto.numeroVolumes ?: current.numeroVolumes,
    )

    fun toResponse(
        model: Pedido,
        itens: List<PedidoItem>,
    ) =
        PedidoResponseDTO(
            id = requireNotNull(model.id),
            clienteId = model.clienteId,
            funcionarioId = model.funcionarioId,
            descontoId = model.descontoId,
            formaPagamentoId = model.formaPagamentoId,
            modalidade = model.modalidade,
            status = model.status,
            dataPedido = model.dataPedido,
            dataRetiradaPrevista = model.dataRetiradaPrevista,
            dataRetirada = model.dataRetirada,
            valorDesconto = model.valorDesconto,
            valorTotal = model.valorTotal,
            observacao = model.observacao,
            embalagemPresente = model.embalagemPresente,
            numeroVolumes = model.numeroVolumes,
            itens = itens.map(::toItemResponse),
        )

    fun toItemResponse(model: PedidoItem) =
        PedidoItemResponseDTO(
            id = requireNotNull(model.id),
            produtoId = model.produtoId,
            quantidade = model.quantidade,
            precoUnitario = model.precoUnitario,
        )
}

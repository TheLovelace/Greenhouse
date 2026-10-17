package org.greenhouse.mapper

import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraFornecedorUpdateRequestDTO
import org.greenhouse.dto.response.CompraFornecedorResponseDTO
import org.greenhouse.dto.response.CompraItemResponseDTO
import org.greenhouse.model.CompraFornecedor
import org.greenhouse.model.CompraItem
import org.springframework.stereotype.Component

@Component
class CompraFornecedorMapper {
    fun toModel(dto: CompraFornecedorCreateRequestDTO) =
        CompraFornecedor(
            funcionarioId = dto.funcionarioId,
            fornecedorId = dto.fornecedorId,
            formaPagamentoId = dto.formaPagamentoId,
            dataCompra = dto.dataCompra ?: java.time.OffsetDateTime.now(),
            valorFrete = dto.valorFrete,
            freteIncluso = dto.freteIncluso,
            dataEntregaPrevista = dto.dataEntregaPrevista,
            observacao = dto.observacao,
        )

    fun toModel(
        dto: CompraFornecedorUpdateRequestDTO,
        current: CompraFornecedor,
    ) = current.copy(
        funcionarioId = dto.funcionarioId ?: current.funcionarioId,
        fornecedorId = dto.fornecedorId ?: current.fornecedorId,
        formaPagamentoId = dto.formaPagamentoId ?: current.formaPagamentoId,
        dataCompra = dto.dataCompra ?: current.dataCompra,
        valorFrete = dto.valorFrete ?: current.valorFrete,
        freteIncluso = dto.freteIncluso ?: current.freteIncluso,
        dataEntregaPrevista = dto.dataEntregaPrevista ?: current.dataEntregaPrevista,
        observacao = dto.observacao ?: current.observacao,
    )

    fun toResponse(
        model: CompraFornecedor,
        itens: List<CompraItem>,
    ) =
        CompraFornecedorResponseDTO(
            id = requireNotNull(model.id),
            funcionarioId = model.funcionarioId,
            fornecedorId = model.fornecedorId,
            formaPagamentoId = model.formaPagamentoId,
            dataCompra = model.dataCompra,
            status = model.status,
            valorTotal = model.valorTotal,
            valorFrete = model.valorFrete,
            freteIncluso = model.freteIncluso,
            dataEntregaPrevista = model.dataEntregaPrevista,
            observacao = model.observacao,
            itens = itens.map(::toItemResponse),
        )

    fun toItemResponse(model: CompraItem) =
        CompraItemResponseDTO(
            id = requireNotNull(model.id),
            produtoId = model.produtoId,
            quantidade = model.quantidade,
            valorUnitario = model.valorUnitario,
        )
}

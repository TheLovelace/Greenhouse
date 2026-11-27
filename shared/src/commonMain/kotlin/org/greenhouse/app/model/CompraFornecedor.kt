package org.greenhouse.app.model

import kotlinx.serialization.Serializable
import org.greenhouse.ui.screens.compraFornecedor.model.StatusCompraCadastro

@Serializable
data class CompraFornecedor(
    val id: Long,
    val funcionarioId: Long,
    val fornecedorId: Long,
    val formaPagamentoId: Long,
    val dataCompra: String,
    val status: StatusCompraCadastro,
    val valorTotal: Double,
    val valorFrete: Double?,
    val freteIncluso: Boolean,
    val dataEntregaPrevista: String?,
    val observacao: String?,
    val itens: List<CompraItem>,
)

@Serializable
data class CompraItem(
    val id: Long,
    val produtoId: Long,
    val quantidade: Int,
    val valorUnitario: Double,
)

@Serializable
data class CompraFornecedorCreateRequest(
    val funcionarioId: Long,
    val fornecedorId: Long,
    val formaPagamentoId: Long,
    val dataCompra: String?,
    val valorFrete: Double?,
    val freteIncluso: Boolean,
    val dataEntregaPrevista: String?,
    val observacao: String?,
    val itens: List<CompraItemRequest>,
)

@Serializable
data class CompraFornecedorUpdateRequest(
    val funcionarioId: Long?,
    val fornecedorId: Long?,
    val formaPagamentoId: Long?,
    val dataCompra: String?,
    val valorFrete: Double?,
    val freteIncluso: Boolean,
    val dataEntregaPrevista: String?,
    val observacao: String?,
    val itens: List<CompraItemRequest>?,
)

@Serializable
data class CompraItemRequest(
    val produtoId: Long,
    val quantidade: Int,
    val valorUnitario: Double,
)

@Serializable
data class CompraFornecedorStatusRequest(
    val status: StatusCompraCadastro,
)

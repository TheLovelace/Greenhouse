package org.greenhouse.ui.screens.compraFornecedor.model

enum class StatusCompraCadastro {
    PEDIDA,
    RECEBIDA,
    CANCELADA,
}

data class CompraItemCadastroData(
    val produtoId: Long,
    val quantidade: Int,
    val valorUnitario: String,
)

data class CompraFornecedorCadastroData(
    val funcionarioId: Long,
    val fornecedorId: Long,
    val formaPagamentoId: Long,
    val dataCompra: String?,
    val status: StatusCompraCadastro,
    val valorTotal: String,
    val valorFrete: String?,
    val freteIncluso: Boolean,
    val dataEntregaPrevista: String?,
    val observacao: String?,
    val itens: List<CompraItemCadastroData>,
)

data class CompraItemForm(
    val produtoId: String = "",
    val quantidade: String = "",
    val valorUnitario: String = "",
)

data class CompraFornecedorCadastroForm(
    val funcionarioId: String = "",
    val fornecedorId: String = "",
    val formaPagamentoId: String = "",
    val dataCompra: String = "",
    val status: StatusCompraCadastro = StatusCompraCadastro.PEDIDA,
    val valorFrete: String = "",
    val freteIncluso: Boolean = false,
    val dataEntregaPrevista: String = "",
    val observacao: String = "",
    val itens: List<CompraItemForm> = listOf(CompraItemForm()),
)

package org.greenhouse.ui.screens.pedido.model

enum class ModalidadePedidoCadastro {
    IMEDIATA,
    AGENDADA,
}

data class PedidoItemCadastroData(
    val produtoId: Long,
    val quantidade: Int,
)

data class PedidoCadastroData(
    val clienteId: Long?,
    val funcionarioId: Long,
    val descontoId: Long?,
    val formaPagamentoId: Long,
    val modalidade: ModalidadePedidoCadastro,
    val dataRetiradaPrevista: String?,
    val observacao: String?,
    val embalagemPresente: Boolean,
    val numeroVolumes: Int,
    val itens: List<PedidoItemCadastroData>,
)

data class PedidoItemForm(
    val produtoId: String = "",
    val quantidade: String = "",
)

data class PedidoCadastroForm(
    val clienteId: String = "",
    val funcionarioId: String = "",
    val descontoId: String = "",
    val formaPagamentoId: String = "",
    val modalidade: ModalidadePedidoCadastro = ModalidadePedidoCadastro.IMEDIATA,
    val dataRetiradaPrevista: String = "",
    val observacao: String = "",
    val embalagemPresente: Boolean = false,
    val numeroVolumes: String = "1",
    val itens: List<PedidoItemForm> = listOf(PedidoItemForm()),
)

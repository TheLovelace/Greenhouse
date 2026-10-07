package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Pedido
import org.greenhouse.entity.PedidoItem
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

private object Pedidos : Table("pedido") {
    val id = long("id").autoIncrement()
    val clienteId = long("cliente_id").nullable()
    val funcionarioId = long("funcionario_id")
    val descontoId = long("desconto_id").nullable()
    val modalidade = varchar("modalidade", 30)
    val status = varchar("status", 30).default("ABERTO")
    val dataPedido = timestampWithTimeZone("data_pedido")
    val dataRetiradaPrevista = timestampWithTimeZone("data_retirada_prevista").nullable()
    val dataRetirada = timestampWithTimeZone("data_retirada").nullable()
    val valorDesconto = decimal("valor_desconto", 12, 2)
    val valorTotal = decimal("valor_total", 12, 2)
    val observacao = text("observacao").nullable()

    override val primaryKey = PrimaryKey(id)
}

private object PedidoItens : Table("pedido_item") {
    val id = long("id").autoIncrement()
    val pedidoId = long("pedido_id")
    val produtoId = long("produto_id")
    val quantidade = integer("quantidade")
    val precoUnitario = decimal("preco_unitario", 12, 2)

    override val primaryKey = PrimaryKey(id)
}

private object Funcionarios : Table("funcionario") {
    val id = long("id")
    val ativo = bool("ativo")

    override val primaryKey = PrimaryKey(id)
}

@Repository
class PedidoRepository(
    private val databaseConfig: DatabaseConfig,
) {
    fun criar(pedido: Pedido): Pedido =
        databaseConfig.session {
            val id =
                Pedidos.insert {
                    it[clienteId] = pedido.clienteId
                    it[funcionarioId] = pedido.funcionarioId
                    it[descontoId] = pedido.descontoId
                    it[modalidade] = pedido.modalidade.name
                    it[status] = pedido.status.name
                    it[dataRetiradaPrevista] = pedido.dataRetiradaPrevista
                    it[valorDesconto] = pedido.valorDesconto
                    it[valorTotal] = pedido.valorTotal
                    it[observacao] = pedido.observacao
                }[Pedidos.id]

            inserirItens(id, pedido.itens)
            consultarPedido(id)!!
        }

    fun listar(
        clienteId: Long? = null,
        funcionarioId: Long? = null,
        status: StatusPedido? = null,
        modalidade: ModalidadePedido? = null,
    ): List<Pedido> =
        databaseConfig.session {
            val consulta = Pedidos.selectAll()
            clienteId?.let { valor -> consulta.andWhere { Pedidos.clienteId eq valor } }
            funcionarioId?.let { valor -> consulta.andWhere { Pedidos.funcionarioId eq valor } }
            status?.let { valor -> consulta.andWhere { Pedidos.status eq valor.name } }
            modalidade?.let { valor -> consulta.andWhere { Pedidos.modalidade eq valor.name } }

            val pedidos =
                consulta
                    .orderBy(Pedidos.dataPedido to SortOrder.DESC)
                    .map { it.toPedido() }

            val itensPorPedido = buscarItensPorPedidoIds(pedidos.mapNotNull { it.id })
            pedidos.map { it.copy(itens = itensPorPedido[it.id] ?: emptyList()) }
        }

    fun buscarPorId(id: Long): Pedido? =
        databaseConfig.session {
            consultarPedido(id)
        }

    fun bloquearPorId(id: Long): Pedido? =
        databaseConfig.session {
            val pedido =
                Pedidos
                    .selectAll()
                    .where { Pedidos.id eq id }
                    .forUpdate()
                    .singleOrNull()
                    ?.toPedido()
                    ?: return@session null

            pedido.copy(itens = buscarItensPorPedidoIds(listOf(id))[id] ?: emptyList())
        }

    fun atualizar(
        id: Long,
        pedido: Pedido,
        substituirItens: Boolean,
    ): Pedido? =
        databaseConfig.session {
            val linhasAtualizadas =
                Pedidos.update({ Pedidos.id eq id }) {
                    it[clienteId] = pedido.clienteId
                    it[funcionarioId] = pedido.funcionarioId
                    it[descontoId] = pedido.descontoId
                    it[dataRetiradaPrevista] = pedido.dataRetiradaPrevista
                    it[valorDesconto] = pedido.valorDesconto
                    it[valorTotal] = pedido.valorTotal
                    it[observacao] = pedido.observacao
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                if (substituirItens) {
                    PedidoItens.deleteWhere { PedidoItens.pedidoId eq id }
                    inserirItens(id, pedido.itens)
                }
                consultarPedido(id)
            }
        }

    fun atualizarStatus(
        id: Long,
        status: StatusPedido,
        dataRetirada: OffsetDateTime?,
    ): Pedido? =
        databaseConfig.session {
            val linhasAtualizadas =
                Pedidos.update({ Pedidos.id eq id }) {
                    it[Pedidos.status] = status.name
                    if (dataRetirada != null) {
                        it[Pedidos.dataRetirada] = dataRetirada
                    }
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                consultarPedido(id)
            }
        }

    fun funcionarioAtivo(id: Long): Boolean? =
        databaseConfig.session {
            Funcionarios
                .selectAll()
                .where { Funcionarios.id eq id }
                .singleOrNull()
                ?.get(Funcionarios.ativo)
        }

    fun excluir(id: Long): Boolean =
        databaseConfig.session {
            Pedidos.deleteWhere { Pedidos.id eq id } > 0
        }

    private fun consultarPedido(id: Long): Pedido? {
        val pedido =
            Pedidos
                .selectAll()
                .where { Pedidos.id eq id }
                .singleOrNull()
                ?.toPedido()
                ?: return null

        return pedido.copy(itens = buscarItensPorPedidoIds(listOf(id))[id] ?: emptyList())
    }

    private fun inserirItens(
        pedidoId: Long,
        itens: List<PedidoItem>,
    ) {
        itens.forEach { item ->
            PedidoItens.insert {
                it[PedidoItens.pedidoId] = pedidoId
                it[produtoId] = item.produtoId
                it[quantidade] = item.quantidade
                it[precoUnitario] = item.precoUnitario!!
            }
        }
    }

    private fun buscarItensPorPedidoIds(pedidoIds: List<Long>): Map<Long, List<PedidoItem>> {
        if (pedidoIds.isEmpty()) return emptyMap()

        return PedidoItens
            .selectAll()
            .where { PedidoItens.pedidoId inList pedidoIds }
            .map { it.toPedidoItem() }
            .groupBy { item -> item.first }
            .mapValues { entry -> entry.value.map { it.second } }
    }

    private fun ResultRow.toPedido() =
        Pedido(
            id = this[Pedidos.id],
            clienteId = this[Pedidos.clienteId],
            funcionarioId = this[Pedidos.funcionarioId],
            descontoId = this[Pedidos.descontoId],
            modalidade = ModalidadePedido.valueOf(this[Pedidos.modalidade]),
            status = StatusPedido.valueOf(this[Pedidos.status]),
            dataPedido = this[Pedidos.dataPedido],
            dataRetiradaPrevista = this[Pedidos.dataRetiradaPrevista],
            dataRetirada = this[Pedidos.dataRetirada],
            valorDesconto = this[Pedidos.valorDesconto],
            valorTotal = this[Pedidos.valorTotal],
            observacao = this[Pedidos.observacao],
        )

    private fun ResultRow.toPedidoItem(): Pair<Long, PedidoItem> =
        this[PedidoItens.pedidoId] to
            PedidoItem(
                id = this[PedidoItens.id],
                produtoId = this[PedidoItens.produtoId],
                quantidade = this[PedidoItens.quantidade],
                precoUnitario = this[PedidoItens.precoUnitario],
            )
}

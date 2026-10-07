package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Estoque
import org.greenhouse.entity.MovimentacaoEstoque
import org.greenhouse.enums.TipoMovimentacaoEstoque
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

private object ProdutosEstoqueTable : Table("produto") {
    val id = long("id")

    override val primaryKey = PrimaryKey(id)
}

private object EstoquesTable : Table("estoque") {
    val produtoId = long("produto_id")
    val quantidadeFisica = integer("quantidade_fisica")
    val quantidadeReservada = integer("quantidade_reservada")
    val quantidadeMinima = integer("quantidade_minima")

    override val primaryKey = PrimaryKey(produtoId)
}

private object MovimentacoesEstoqueTable : Table("movimentacao_estoque") {
    val id = long("id").autoIncrement()
    val produtoId = long("produto_id")
    val pedidoId = long("pedido_id").nullable()
    val compraId = long("compra_id").nullable()
    val funcionarioId = long("funcionario_id").nullable()
    val tipo = varchar("tipo", 30)
    val quantidade = integer("quantidade")
    val observacao = text("observacao").nullable()
    val dataHora = timestampWithTimeZone("data_hora")

    override val primaryKey = PrimaryKey(id)
}

@Repository
class EstoqueRepository(
    private val databaseConfig: DatabaseConfig,
) {
    fun listarMovimentacoes(): List<MovimentacaoEstoque> =
        databaseConfig.session {
            MovimentacoesEstoqueTable
                .selectAll()
                .orderBy(
                    MovimentacoesEstoqueTable.dataHora to SortOrder.DESC,
                    MovimentacoesEstoqueTable.id to SortOrder.DESC,
                ).map { it.toMovimentacaoEstoque() }
        }

    fun buscarPosicaoPorProdutoId(produtoId: Long): Estoque? =
        databaseConfig.session {
            val produtoExiste =
                ProdutosEstoqueTable
                    .selectAll()
                    .where { ProdutosEstoqueTable.id eq produtoId }
                    .singleOrNull() != null

            if (!produtoExiste) {
                null
            } else {
                val estoque =
                    EstoquesTable
                        .selectAll()
                        .where { EstoquesTable.produtoId eq produtoId }
                        .singleOrNull()

                val quantidadeFisica =
                    estoque?.get(EstoquesTable.quantidadeFisica) ?: 0

                val quantidadeReservada =
                    estoque?.get(EstoquesTable.quantidadeReservada) ?: 0

                val quantidadeMinima =
                    estoque?.get(EstoquesTable.quantidadeMinima) ?: 0

                Estoque(
                    produtoId = produtoId,
                    quantidadeFisica = quantidadeFisica,
                    quantidadeReservada = quantidadeReservada,
                    quantidadeDisponivel = quantidadeFisica - quantidadeReservada,
                    quantidadeMinima = quantidadeMinima,
                )
            }
        }

    fun registrarMovimentacao(
        movimentacao: MovimentacaoEstoque,
    ): MovimentacaoEstoque? =
        databaseConfig.session {
            val produtoExiste =
                ProdutosEstoqueTable
                    .selectAll()
                    .where { ProdutosEstoqueTable.id eq movimentacao.produtoId }
                    .forUpdate()
                    .singleOrNull() != null

            if (!produtoExiste) {
                return@session null
            }

            val estoque =
                EstoquesTable
                    .selectAll()
                    .where {
                        EstoquesTable.produtoId eq movimentacao.produtoId
                    }
                    .forUpdate()
                    .singleOrNull()

            val quantidadeFisica =
                estoque?.get(EstoquesTable.quantidadeFisica) ?: 0

            val quantidadeReservada =
                estoque?.get(EstoquesTable.quantidadeReservada) ?: 0

            val novaQuantidadeFisica =
                when (movimentacao.tipo) {
                    TipoMovimentacaoEstoque.ENTRADA,
                    TipoMovimentacaoEstoque.AJUSTE_ENTRADA,
                    -> quantidadeFisica.toLong() + movimentacao.quantidade

                    TipoMovimentacaoEstoque.SAIDA,
                    TipoMovimentacaoEstoque.AJUSTE_SAIDA,
                    TipoMovimentacaoEstoque.PERDA,
                    -> quantidadeFisica.toLong() - movimentacao.quantidade
                }

            if (novaQuantidadeFisica < quantidadeReservada) {
                throw UnprocessableEntityException(
                    "A movimentação não pode reduzir o estoque físico abaixo da quantidade reservada",
                )
            }

            if (novaQuantidadeFisica > Int.MAX_VALUE) {
                throw UnprocessableEntityException(
                    "A quantidade física excede o limite suportado pelo estoque",
                )
            }

            if (estoque == null) {
                EstoquesTable.insert {
                    it[EstoquesTable.produtoId] = movimentacao.produtoId
                    it[EstoquesTable.quantidadeFisica] = novaQuantidadeFisica.toInt()
                    it[EstoquesTable.quantidadeReservada] = 0
                    it[EstoquesTable.quantidadeMinima] = 0
                }
            } else {
                EstoquesTable.update(
                    { EstoquesTable.produtoId eq movimentacao.produtoId },
                ) {
                    it[EstoquesTable.quantidadeFisica] = novaQuantidadeFisica.toInt()
                }
            }

            val movimentacaoId =
                MovimentacoesEstoqueTable.insert {
                    it[produtoId] = movimentacao.produtoId
                    it[pedidoId] = movimentacao.pedidoId
                    it[compraId] = movimentacao.compraId
                    it[funcionarioId] = movimentacao.funcionarioId
                    it[tipo] = movimentacao.tipo.name
                    it[quantidade] = movimentacao.quantidade
                    it[observacao] = movimentacao.observacao
                }[MovimentacoesEstoqueTable.id]

            MovimentacoesEstoqueTable
                .selectAll()
                .where {
                    MovimentacoesEstoqueTable.id eq movimentacaoId
                }
                .single()
                .toMovimentacaoEstoque()
        }

    private fun ResultRow.toMovimentacaoEstoque() =
        MovimentacaoEstoque(
            id = this[MovimentacoesEstoqueTable.id],
            produtoId = this[MovimentacoesEstoqueTable.produtoId],
            pedidoId = this[MovimentacoesEstoqueTable.pedidoId],
            compraId = this[MovimentacoesEstoqueTable.compraId],
            funcionarioId = this[MovimentacoesEstoqueTable.funcionarioId],
            tipo =
                TipoMovimentacaoEstoque.valueOf(
                    this[MovimentacoesEstoqueTable.tipo],
                ),
            quantidade = this[MovimentacoesEstoqueTable.quantidade],
            observacao = this[MovimentacoesEstoqueTable.observacao],
            dataHora = this[MovimentacoesEstoqueTable.dataHora],
        )
}

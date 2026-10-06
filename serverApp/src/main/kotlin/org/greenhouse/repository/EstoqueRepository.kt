package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.EstoquePosicao
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository

private object Produtos : Table("produto") {
    val id = long("id")

    override val primaryKey = PrimaryKey(id)
}

private object Estoques : Table("estoque") {
    val produtoId = long("produto_id")
    val quantidadeFisica = integer("quantidade_fisica")
    val quantidadeReservada = integer("quantidade_reservada")
    val quantidadeMinima = integer("quantidade_minima")

    override val primaryKey = PrimaryKey(produtoId)
}

@Repository
class EstoqueRepository(
    private val databaseConfig: DatabaseConfig,
) {
    fun buscarPosicaoPorProdutoId(produtoId: Long): EstoquePosicao? =
        databaseConfig.session {
            val produtoExiste =
                Produtos
                    .selectAll()
                    .where { Produtos.id eq produtoId }
                    .singleOrNull() != null

            if (!produtoExiste) {
                null
            } else {
                val estoque =
                    Estoques
                        .selectAll()
                        .where { Estoques.produtoId eq produtoId }
                        .singleOrNull()

                val quantidadeFisica = estoque?.get(Estoques.quantidadeFisica) ?: 0
                val quantidadeReservada = estoque?.get(Estoques.quantidadeReservada) ?: 0
                val quantidadeMinima = estoque?.get(Estoques.quantidadeMinima) ?: 0

                EstoquePosicao(
                    produtoId = produtoId,
                    quantidadeFisica = quantidadeFisica,
                    quantidadeReservada = quantidadeReservada,
                    quantidadeDisponivel = quantidadeFisica - quantidadeReservada,
                    quantidadeMinima = quantidadeMinima,
                )
            }
        }
}

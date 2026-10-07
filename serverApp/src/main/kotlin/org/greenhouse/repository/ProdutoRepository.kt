package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Produto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

private object Produtos : Table("produto") {
    val id = long("id").autoIncrement()
    val codigo = varchar("codigo", 50).nullable().uniqueIndex()
    val nome = varchar("nome", 150)
    val descricao = text("descricao").nullable()
    val preco = decimal("preco", 12, 2)
    val ativo = bool("ativo").default(true)

    override val primaryKey = PrimaryKey(id)
}

private object ItensPedido : Table("pedido_item") {
    val produtoId = long("produto_id")
}

private object ItensCompra : Table("compra_item") {
    val produtoId = long("produto_id")
}

private object Estoques : Table("estoque") {
    val produtoId = long("produto_id")
}

private object MovimentacoesEstoque : Table("movimentacao_estoque") {
    val produtoId = long("produto_id")
}

@Repository
class ProdutoRepository(
    private val databaseConfig: DatabaseConfig,
) {

    fun criar(produto: Produto): Produto =
        databaseConfig.session {
            val id =
                Produtos.insert {
                    it[codigo] = produto.codigo
                    it[nome] = produto.nome
                    it[descricao] = produto.descricao
                    it[preco] = produto.preco
                    it[ativo] = produto.ativo
                }[Produtos.id]

            produto.copy(id = id)
        }

    fun listar(): List<Produto> =
        databaseConfig.session {
            Produtos.selectAll().map { it.toProduto() }
        }

    fun buscarPorId(id: Long): Produto? =
        databaseConfig.session {
            Produtos.selectAll()
                .where { Produtos.id eq id }
                .singleOrNull()
                ?.toProduto()
        }

    fun codigoEmUso(codigo: String, ignorarId: Long? = null): Boolean =
        databaseConfig.session {
            val produtoExistente =
                Produtos.selectAll()
                    .where { Produtos.codigo eq codigo }
                    .singleOrNull()

            produtoExistente != null && produtoExistente[Produtos.id] != ignorarId
        }

    fun atualizar(id: Long, produto: Produto): Produto? =
        databaseConfig.session {
            val linhasAtualizadas =
                Produtos.update({ Produtos.id eq id }) {
                    it[codigo] = produto.codigo
                    it[nome] = produto.nome
                    it[descricao] = produto.descricao
                    it[preco] = produto.preco
                    it[ativo] = produto.ativo
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                Produtos.selectAll()
                    .where { Produtos.id eq id }
                    .single()
                    .toProduto()
            }
        }

    fun possuiDependencias(id: Long): Boolean =
        databaseConfig.session {
            ItensPedido.selectAll().where { ItensPedido.produtoId eq id }.limit(1).singleOrNull() != null ||
                ItensCompra.selectAll().where { ItensCompra.produtoId eq id }.limit(1).singleOrNull() != null ||
                Estoques.selectAll().where { Estoques.produtoId eq id }.limit(1).singleOrNull() != null ||
                MovimentacoesEstoque.selectAll()
                    .where { MovimentacoesEstoque.produtoId eq id }
                    .limit(1)
                    .singleOrNull() != null
        }

    fun excluir(id: Long): Boolean =
        databaseConfig.session {
            Produtos.deleteWhere { Produtos.id eq id } > 0
        }

    private fun ResultRow.toProduto() =
        Produto(
            id = this[Produtos.id],
            codigo = this[Produtos.codigo],
            nome = this[Produtos.nome],
            descricao = this[Produtos.descricao],
            preco = this[Produtos.preco],
            ativo = this[Produtos.ativo],
        )
}

package org.greenhouse.integration.fixture

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Produto
import org.greenhouse.repository.ProdutoRepository
import org.greenhouse.usecase.ProdutoUseCase
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import java.math.BigDecimal
import java.util.UUID

private object EstoquesProduto : Table("estoque") {
    val produtoId = long("produto_id")
}

class ProdutoFixture {
    private val databaseConfig = DatabaseConfig(System.getenv("DATABASE_URL"))
    private val repository = ProdutoRepository(databaseConfig)
    val useCase = ProdutoUseCase(repository)

    fun novoProduto(
        codigo: String = "SKU-${UUID.randomUUID()}",
        nome: String = "Produto de teste ${UUID.randomUUID()}",
        descricao: String? = "Descrição de teste",
        preco: BigDecimal = BigDecimal("12.50"),
    ): Produto =
        useCase.criar(
            Produto(
                codigo = codigo,
                nome = nome,
                descricao = descricao,
                preco = preco,
            ),
        )

    fun adicionarEstoque(produtoId: Long) {
        databaseConfig.session {
            EstoquesProduto.insert {
                it[EstoquesProduto.produtoId] = produtoId
            }
        }
    }

    fun limpar(id: Long?) {
        id?.let { produtoId ->
            databaseConfig.session {
                EstoquesProduto.deleteWhere { EstoquesProduto.produtoId eq produtoId }
            }
            repository.excluir(produtoId)
        }
    }
}

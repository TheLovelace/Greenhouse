package org.greenhouse.integration.fixture

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.repository.EstoqueRepository
import org.greenhouse.usecase.EstoqueUseCase
import java.math.BigDecimal
import java.sql.DriverManager
import java.util.UUID

class EstoqueFixture {
    private val databaseUrl =
        requireNotNull(System.getenv("DATABASE_URL")) {
            "A variável DATABASE_URL é necessária para os testes de integração"
        }
    private val produtoIds = mutableListOf<Long>()
    private val repository = EstoqueRepository(DatabaseConfig(databaseUrl))
    val useCase = EstoqueUseCase(repository)

    fun novoProdutoComEstoque(
        quantidadeFisica: Int,
        quantidadeReservada: Int,
        quantidadeMinima: Int,
    ): Long {
        val produtoId =
            DriverManager.getConnection(databaseUrl).use { connection ->
                connection
                    .prepareStatement(
                        """
                        INSERT INTO produto (codigo, nome, preco, ativo)
                        VALUES (?, ?, ?, TRUE)
                        RETURNING id
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setString(1, "TEST-${UUID.randomUUID()}")
                        statement.setString(2, "Produto teste ${UUID.randomUUID()}")
                        statement.setBigDecimal(3, BigDecimal("10.00"))
                        statement.executeQuery().use { resultSet ->
                            check(resultSet.next()) { "O banco não retornou o ID do produto criado" }
                            resultSet.getLong("id")
                        }
                    }
            }
        produtoIds.add(produtoId)

        DriverManager.getConnection(databaseUrl).use { connection ->
            connection
                .prepareStatement(
                    """
                    INSERT INTO estoque (produto_id, quantidade_fisica, quantidade_reservada, quantidade_minima)
                    VALUES (?, ?, ?, ?)
                    """.trimIndent(),
                ).use { statement ->
                    statement.setLong(1, produtoId)
                    statement.setInt(2, quantidadeFisica)
                    statement.setInt(3, quantidadeReservada)
                    statement.setInt(4, quantidadeMinima)
                    statement.executeUpdate()
                }
        }

        return produtoId
    }

    fun novoProdutoSemEstoque(): Long {
        val produtoId =
            DriverManager.getConnection(databaseUrl).use { connection ->
                connection
                    .prepareStatement(
                        """
                        INSERT INTO produto (codigo, nome, preco, ativo)
                        VALUES (?, ?, ?, TRUE)
                        RETURNING id
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setString(1, "TEST-${UUID.randomUUID()}")
                        statement.setString(2, "Produto teste ${UUID.randomUUID()}")
                        statement.setBigDecimal(3, BigDecimal("10.00"))
                        statement.executeQuery().use { resultSet ->
                            check(resultSet.next()) { "O banco não retornou o ID do produto criado" }
                            resultSet.getLong("id")
                        }
                    }
            }
        produtoIds.add(produtoId)
        return produtoId
    }

    fun limpar() {
        if (produtoIds.isEmpty()) return

        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.prepareStatement("DELETE FROM movimentacao_estoque WHERE produto_id = ?").use { statement ->
                produtoIds.forEach { produtoId ->
                    statement.setLong(1, produtoId)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
            connection.prepareStatement("DELETE FROM estoque WHERE produto_id = ?").use { estoqueStatement ->
                produtoIds.forEach { produtoId ->
                    estoqueStatement.setLong(1, produtoId)
                    estoqueStatement.addBatch()
                }
                estoqueStatement.executeBatch()
            }
            connection.prepareStatement("DELETE FROM produto WHERE id = ?").use { produtoStatement ->
                produtoIds.forEach { produtoId ->
                    produtoStatement.setLong(1, produtoId)
                    produtoStatement.addBatch()
                }
                produtoStatement.executeBatch()
            }
        }
        produtoIds.clear()
    }
}

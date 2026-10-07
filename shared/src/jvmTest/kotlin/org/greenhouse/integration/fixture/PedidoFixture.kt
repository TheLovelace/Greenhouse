package org.greenhouse.integration.fixture

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Estoque
import org.greenhouse.entity.MovimentacaoEstoque
import org.greenhouse.entity.Pedido
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.repository.ClienteRepository
import org.greenhouse.repository.DescontoRepository
import org.greenhouse.repository.EstoqueRepository
import org.greenhouse.repository.PedidoRepository
import org.greenhouse.repository.ProdutoRepository
import org.greenhouse.usecase.EstoqueUseCase
import org.greenhouse.usecase.PedidoUseCase
import java.math.BigDecimal
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Types
import java.time.OffsetDateTime
import java.util.UUID

class PedidoFixture {
    private val databaseUrl =
        requireNotNull(System.getenv("DATABASE_URL")) {
            "A variável DATABASE_URL é necessária para os testes de integração"
        }

    // Uma única instância compartilhada: é o que faz as sessions aninhadas
    // (pedido + estoque + movimentacao) participarem da mesma transação.
    private val databaseConfig = DatabaseConfig(databaseUrl)
    private val estoqueRepository = EstoqueRepository(databaseConfig)
    val estoqueUseCase = EstoqueUseCase(estoqueRepository)

    val useCase =
        PedidoUseCase(
            databaseConfig = databaseConfig,
            pedidoRepository = PedidoRepository(databaseConfig),
            clienteRepository = ClienteRepository(databaseConfig),
            produtoRepository = ProdutoRepository(databaseConfig),
            descontoRepository = DescontoRepository(databaseConfig),
            estoqueUseCase = estoqueUseCase,
        )

    private val pedidoIds = mutableListOf<Long>()
    private val produtoIds = mutableListOf<Long>()
    private val clienteIds = mutableListOf<Long>()
    private val funcionarioIds = mutableListOf<Long>()
    private val descontoIds = mutableListOf<Long>()

    fun novoFuncionario(ativo: Boolean = true): Long =
        inserir(
            """
            INSERT INTO funcionario (nome, cpf, ativo)
            VALUES (?, ?, ?)
            RETURNING id
            """.trimIndent(),
        ) { statement ->
            statement.setString(1, "Funcionário teste ${UUID.randomUUID()}")
            statement.setString(2, digitosUnicos())
            statement.setBoolean(3, ativo)
        }.also { funcionarioIds.add(it) }

    fun novoCliente(ativo: Boolean = true): Long =
        inserir(
            """
            INSERT INTO cliente (nome, telefone, whatsapp, cpf, email, ativo)
            VALUES (?, ?, FALSE, NULL, NULL, ?)
            RETURNING id
            """.trimIndent(),
        ) { statement ->
            statement.setString(1, "Cliente teste ${UUID.randomUUID()}")
            statement.setString(2, "11987654321")
            statement.setBoolean(3, ativo)
        }.also { clienteIds.add(it) }

    fun novoDesconto(
        tipo: TipoDesconto = TipoDesconto.PERCENTUAL,
        valor: BigDecimal = BigDecimal("10.00"),
        ativo: Boolean = true,
        dataInicio: OffsetDateTime? = null,
        dataFim: OffsetDateTime? = null,
    ): Long =
        inserir(
            """
            INSERT INTO desconto (titulo, descricao, tipo, valor, data_inicio, data_fim, ativo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """.trimIndent(),
        ) { statement ->
            statement.setString(1, "Desconto teste ${UUID.randomUUID()}")
            statement.setString(2, "Desconto criado pela fixture de pedido")
            statement.setString(3, tipo.name)
            statement.setBigDecimal(4, valor)
            if (dataInicio == null) {
                statement.setNull(5, Types.TIMESTAMP_WITH_TIMEZONE)
            } else {
                statement.setObject(5, dataInicio)
            }
            if (dataFim == null) {
                statement.setNull(6, Types.TIMESTAMP_WITH_TIMEZONE)
            } else {
                statement.setObject(6, dataFim)
            }
            statement.setBoolean(7, ativo)
        }.also { descontoIds.add(it) }

    fun novoProduto(
        preco: BigDecimal = BigDecimal("10.00"),
        ativo: Boolean = true,
    ): Long =
        inserir(
            """
            INSERT INTO produto (codigo, nome, preco, ativo)
            VALUES (?, ?, ?, ?)
            RETURNING id
            """.trimIndent(),
        ) { statement ->
            statement.setString(1, "TEST-${UUID.randomUUID()}")
            statement.setString(2, "Produto teste ${UUID.randomUUID()}")
            statement.setBigDecimal(3, preco)
            statement.setBoolean(4, ativo)
        }.also { produtoIds.add(it) }

    fun novoProdutoComEstoque(
        quantidadeFisica: Int,
        quantidadeReservada: Int = 0,
        quantidadeMinima: Int = 0,
        preco: BigDecimal = BigDecimal("10.00"),
    ): Long {
        val produtoId = novoProduto(preco = preco)
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

    fun criarPedido(pedido: Pedido): Pedido =
        useCase.criar(pedido).also { criado ->
            pedidoIds.add(requireNotNull(criado.id) { "O banco não retornou o ID do pedido criado" })
        }

    fun rastrear(pedido: Pedido): Pedido =
        pedido.also {
            pedidoIds.add(requireNotNull(it.id) { "Pedido sem id" })
        }

    fun reservarEstoque(
        produtoId: Long,
        quantidade: Int,
    ) {
        DriverManager.getConnection(databaseUrl).use { connection ->
            connection
                .prepareStatement(
                    "UPDATE estoque SET quantidade_reservada = quantidade_reservada + ? WHERE produto_id = ?",
                ).use { statement ->
                    statement.setInt(1, quantidade)
                    statement.setLong(2, produtoId)
                    check(statement.executeUpdate() == 1) { "Produto sem linha de estoque" }
                }
        }
    }

    fun posicaoEstoque(produtoId: Long): Estoque = estoqueUseCase.buscarPosicaoPorProdutoId(produtoId)

    fun movimentacoesDoPedido(pedidoId: Long): List<MovimentacaoEstoque> =
        estoqueUseCase.listarMovimentacoes().filter { it.pedidoId == pedidoId }

    fun limpar() {
        DriverManager.getConnection(databaseUrl).use { connection ->
            excluirEmLote(connection, "DELETE FROM movimentacao_estoque WHERE pedido_id = ?", pedidoIds)
            excluirEmLote(connection, "DELETE FROM movimentacao_estoque WHERE produto_id = ?", produtoIds)
            excluirEmLote(connection, "DELETE FROM pedido WHERE id = ?", pedidoIds)
            excluirEmLote(connection, "DELETE FROM estoque WHERE produto_id = ?", produtoIds)
            excluirEmLote(connection, "DELETE FROM produto WHERE id = ?", produtoIds)
            excluirEmLote(connection, "DELETE FROM desconto WHERE id = ?", descontoIds)
            excluirEmLote(connection, "DELETE FROM cliente WHERE id = ?", clienteIds)
            excluirEmLote(connection, "DELETE FROM funcionario WHERE id = ?", funcionarioIds)
        }
        pedidoIds.clear()
        produtoIds.clear()
        clienteIds.clear()
        funcionarioIds.clear()
        descontoIds.clear()
    }

    private fun digitosUnicos(tamanho: Int = 11): String =
        (UUID.randomUUID().toString().filter { it.isDigit() } + "00000000000")
            .take(tamanho)

    private fun inserir(
        sql: String,
        bind: (PreparedStatement) -> Unit,
    ): Long =
        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.prepareStatement(sql).use { statement ->
                bind(statement)
                statement.executeQuery().use { resultSet ->
                    check(resultSet.next()) { "O banco não retornou o ID criado" }
                    resultSet.getLong("id")
                }
            }
        }

    private fun excluirEmLote(
        connection: Connection,
        sql: String,
        ids: List<Long>,
    ) {
        if (ids.isEmpty()) return
        connection.prepareStatement(sql).use { statement ->
            ids.forEach { id ->
                statement.setLong(1, id)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }
}

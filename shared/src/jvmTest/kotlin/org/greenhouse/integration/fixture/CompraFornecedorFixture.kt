package org.greenhouse.integration.fixture

import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.response.CompraFornecedorResponseDTO
import org.greenhouse.dto.response.EstoqueResponseDTO
import org.greenhouse.repository.CompraFornecedorRepository
import org.greenhouse.service.CompraFornecedorService
import org.greenhouse.service.EstoqueService
import java.math.BigDecimal
import java.sql.DriverManager
import java.util.UUID

class CompraFornecedorFixture {
    private val databaseUrl =
        requireNotNull(System.getenv("DATABASE_URL")) {
            "A variável DATABASE_URL é necessária para os testes de integração"
        }
    private val compraRepository = SpringTestContext.bean<CompraFornecedorRepository>()
    private val estoqueService = SpringTestContext.bean<EstoqueService>()

    val fornecedorFixture = FornecedorFixture()
    val formaPagamentoFixture = FormaPagamentoFixture()
    val estoqueFixture = EstoqueFixture()

    val service = SpringTestContext.bean<CompraFornecedorService>()

    private val compraIds = mutableListOf<Long>()
    private val funcionarioIds = mutableListOf<Long>()

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

    fun novoFornecedor(ativo: Boolean = true): Long =
        fornecedorFixture.novoFornecedor(ativo = ativo).id

    fun novaFormaPagamento(): Long = formaPagamentoFixture.novaFormaPagamento().id

    fun novoProdutoComEstoque(
        quantidadeFisica: Int,
        quantidadeReservada: Int = 0,
        quantidadeMinima: Int = 0,
        preco: BigDecimal = BigDecimal("10.00"),
    ): Long =
        estoqueFixture.novoProdutoComEstoque(
            quantidadeFisica = quantidadeFisica,
            quantidadeReservada = quantidadeReservada,
            quantidadeMinima = quantidadeMinima,
            preco = preco,
        )

    fun novoProdutoSemEstoque(): Long = estoqueFixture.novoProdutoSemEstoque()

    fun posicaoEstoque(produtoId: Long): EstoqueResponseDTO =
        estoqueService.buscarRegistroEstoquePorProdutoId(produtoId)

    fun elevarEstoqueAoLimite(produtoId: Long) {
        DriverManager.getConnection(databaseUrl).use { connection ->
            connection
                .prepareStatement("UPDATE estoque SET quantidade_fisica = ? WHERE produto_id = ?")
                .use { statement ->
                    statement.setInt(1, Int.MAX_VALUE)
                    statement.setLong(2, produtoId)
                    check(statement.executeUpdate() == 1) { "Produto sem linha de estoque" }
                }
        }
    }

    fun criarCompra(compra: CompraFornecedorCreateRequestDTO): CompraFornecedorResponseDTO =
        service.criar(compra).also { criada ->
            compraIds.add(criada.id)
        }

    fun limpar() {
        compraIds.forEach(compraRepository::excluir)
        compraIds.clear()

        funcionarioIds.forEach { funcionarioId ->
            DriverManager.getConnection(databaseUrl).use { connection ->
                connection.prepareStatement("DELETE FROM funcionario WHERE id = ?").use { statement ->
                    statement.setLong(1, funcionarioId)
                    statement.executeUpdate()
                }
            }
        }
        funcionarioIds.clear()

        fornecedorFixture.limpar()
        formaPagamentoFixture.limpar()
        estoqueFixture.limpar()
    }

    private fun digitosUnicos(tamanho: Int = 11): String =
        (UUID.randomUUID().toString().filter { it.isDigit() } + "00000000000")
            .take(tamanho)

    private fun inserir(
        sql: String,
        bind: (java.sql.PreparedStatement) -> Unit,
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
}

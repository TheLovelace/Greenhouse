package org.greenhouse.integration.fixture

import org.greenhouse.dto.request.ClienteUpdateRequestDTO
import org.greenhouse.dto.request.PedidoCreateRequestDTO
import org.greenhouse.dto.response.EstoqueResponseDTO
import org.greenhouse.dto.response.PedidoResponseDTO
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.repository.PedidoRepository
import org.greenhouse.service.EstoqueService
import org.greenhouse.service.PedidoService
import java.math.BigDecimal
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.time.OffsetDateTime
import java.util.UUID

class PedidoFixture {
    private val databaseUrl =
        requireNotNull(System.getenv("DATABASE_URL")) {
            "A variável DATABASE_URL é necessária para os testes de integração"
        }
    private val pedidoRepository = SpringTestContext.bean<PedidoRepository>()
    private val estoqueService = SpringTestContext.bean<EstoqueService>()

    val clienteFixture = ClienteFixture()
    val descontoFixture = DescontoFixture()
    val formaPagamentoFixture = FormaPagamentoFixture()
    val estoqueFixture = EstoqueFixture()

    val useCase = SpringTestContext.bean<PedidoService>()

    private val pedidoIds = mutableListOf<Long>()
    private val funcionarioIds = mutableListOf<Long>()
    private val clienteIds = mutableListOf<Long>()

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

    fun novoCliente(ativo: Boolean = true): Long {
        val criado = clienteFixture.novoCliente()
        if (!ativo) {
            clienteFixture.service.atualizar(criado.id, ClienteUpdateRequestDTO(ativo = false))
        }
        return criado.id.also { clienteIds.add(it) }
    }

    fun novaFormaPagamento(): Long =
        requireNotNull(formaPagamentoFixture.novaFormaPagamento().id) {
            "O banco não retornou o ID da forma de pagamento"
        }

    fun inativarFormaPagamento(id: Long) {
        DriverManager.getConnection(databaseUrl).use { connection ->
            connection
                .prepareStatement("UPDATE forma_pagamento SET ativo = FALSE WHERE id = ?")
                .use { statement ->
                    statement.setLong(1, id)
                    check(statement.executeUpdate() == 1) { "Forma de pagamento não encontrada para inativar" }
                }
        }
    }

    fun novoDesconto(
        tipo: TipoDesconto = TipoDesconto.PERCENTUAL,
        valor: BigDecimal = BigDecimal("10.00"),
        ativo: Boolean = true,
        dataInicio: OffsetDateTime? = null,
        dataFim: OffsetDateTime? = null,
    ): Long = descontoFixture.novoDesconto(tipo, valor, ativo, dataInicio, dataFim)

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

    fun novoProduto(ativo: Boolean = true): Long {
        val produto =
            estoqueFixture.novoProdutoComEstoque(
                quantidadeFisica = 0,
                quantidadeReservada = 0,
                quantidadeMinima = 0,
            )
        if (!ativo) {
            atualizarProdutoInativo(produto)
        }
        return produto
    }

    fun posicaoEstoque(produtoId: Long): EstoqueResponseDTO =
        estoqueService.buscarRegistroEstoquePorProdutoId(produtoId)

    fun criarPedido(pedido: PedidoCreateRequestDTO): PedidoResponseDTO =
        useCase.criar(pedido).also { criado ->
            pedidoIds.add(criado.id)
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

    fun limpar() {
        pedidoIds.forEach { pedidoId ->
            pedidoRepository.excluir(pedidoId)
        }
        pedidoIds.clear()

        funcionarioIds.forEach { funcionarioId ->
            DriverManager.getConnection(databaseUrl).use { connection ->
                connection.prepareStatement("DELETE FROM funcionario WHERE id = ?").use { statement ->
                    statement.setLong(1, funcionarioId)
                    statement.executeUpdate()
                }
            }
        }
        funcionarioIds.clear()

        clienteIds.forEach { clienteId ->
            clienteFixture.limpar(clienteId)
        }
        clienteIds.clear()

        descontoFixture.limpar()
        formaPagamentoFixture.limpar()
        estoqueFixture.limpar()
    }

    private fun atualizarProdutoInativo(produtoId: Long) {
        DriverManager.getConnection(databaseUrl).use { connection ->
            connection
                .prepareStatement("UPDATE produto SET ativo = FALSE WHERE id = ?")
                .use { statement ->
                    statement.setLong(1, produtoId)
                    check(statement.executeUpdate() == 1) { "Produto não encontrado para inativar" }
                }
        }
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

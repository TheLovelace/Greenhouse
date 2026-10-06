package org.greenhouse.integration.fixture

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Desconto
import org.greenhouse.entity.TipoDesconto
import org.greenhouse.repository.DescontoRepository
import org.greenhouse.usecase.DescontoUseCase
import java.math.BigDecimal
import java.sql.DriverManager
import java.sql.Types
import java.time.OffsetDateTime
import java.util.UUID

class DescontoFixture {
    private val databaseUrl =
        requireNotNull(System.getenv("DATABASE_URL")) {
            "A variável DATABASE_URL é necessária para os testes de integração"
        }
    private val repository = DescontoRepository(DatabaseConfig(databaseUrl))
    private val idsCriados = mutableListOf<Long>()
    val useCase = DescontoUseCase(repository)

    fun criarDesconto(desconto: Desconto): Desconto =
        useCase.criar(desconto).also { criado ->
            val id = requireNotNull(criado.id) { "O banco não retornou o ID do desconto criado" }
            idsCriados.add(id)
        }

    fun novoDesconto(
        tipo: TipoDesconto = TipoDesconto.PERCENTUAL,
        valor: BigDecimal = BigDecimal("10.00"),
        ativo: Boolean = true,
        dataInicio: OffsetDateTime? = null,
        dataFim: OffsetDateTime? = null,
    ): Long {
        val titulo = "Desconto ${UUID.randomUUID()}"
        val id =
            DriverManager.getConnection(databaseUrl).use { connection ->
                val statement =
                    connection.prepareStatement(
                        """
                        INSERT INTO desconto (titulo, descricao, tipo, valor, data_inicio, data_fim, ativo)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        RETURNING id
                        """.trimIndent(),
                    )
                statement.use {
                    it.setString(1, titulo)
                    it.setString(2, "Desconto criado pela fixture de integração")
                    it.setString(3, tipo.name)
                    it.setBigDecimal(4, valor)
                    if (dataInicio == null) {
                        it.setNull(5, Types.TIMESTAMP_WITH_TIMEZONE)
                    } else {
                        it.setObject(5, dataInicio)
                    }
                    if (dataFim == null) {
                        it.setNull(6, Types.TIMESTAMP_WITH_TIMEZONE)
                    } else {
                        it.setObject(6, dataFim)
                    }
                    it.setBoolean(7, ativo)
                    it.executeQuery().use { resultSet ->
                        check(resultSet.next()) { "O banco não retornou o ID do desconto criado" }
                        resultSet.getLong("id")
                    }
                }
            }
        idsCriados.add(id)
        return id
    }

    fun limpar() {
        if (idsCriados.isEmpty()) return

        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.prepareStatement("DELETE FROM desconto WHERE id = ?").use { statement ->
                idsCriados.forEach { id ->
                    statement.setLong(1, id)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
        }
        idsCriados.clear()
    }
}

package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Fornecedor
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

private object Fornecedores : Table("fornecedor") {
    val id = long("id").autoIncrement()
    val nome = varchar("nome", 150)
    val cnpj = varchar("cnpj", 14).nullable()
    val telefone = varchar("telefone", 20).nullable()
    val email = varchar("email", 150).nullable()
    val contato = varchar("contato", 150).nullable()
    val ativo = bool("ativo").default(true)

    override val primaryKey = PrimaryKey(id)
}

@Repository
class FornecedorRepository(
    private val databaseConfig: DatabaseConfig,
) {

    fun criar(fornecedor: Fornecedor): Fornecedor =
        databaseConfig.session {
            val id =
                Fornecedores.insert {
                    it[nome] = fornecedor.nome
                    it[cnpj] = fornecedor.cnpj
                    it[telefone] = fornecedor.telefone
                    it[email] = fornecedor.email
                    it[contato] = fornecedor.contato
                    it[ativo] = fornecedor.ativo
                }[Fornecedores.id]

            fornecedor.copy(id = id)
        }

    fun listar(): List<Fornecedor> =
        databaseConfig.session {
            Fornecedores.selectAll().map { it.toFornecedor() }
        }

    fun buscarPorId(id: Long): Fornecedor? =
        databaseConfig.session {
            Fornecedores.selectAll()
                .where { Fornecedores.id eq id }
                .singleOrNull()
                ?.toFornecedor()
        }

    fun atualizar(id: Long, fornecedor: Fornecedor): Fornecedor? =
        databaseConfig.session {
            val linhasAtualizadas =
                Fornecedores.update({ Fornecedores.id eq id }) {
                    it[nome] = fornecedor.nome
                    it[cnpj] = fornecedor.cnpj
                    it[telefone] = fornecedor.telefone
                    it[email] = fornecedor.email
                    it[contato] = fornecedor.contato
                    it[ativo] = fornecedor.ativo
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                Fornecedores.selectAll()
                    .where { Fornecedores.id eq id }
                    .single()
                    .toFornecedor()
            }
        }

    fun excluir(id: Long): Boolean =
        databaseConfig.session {
            Fornecedores.deleteWhere { Fornecedores.id eq id } > 0
        }

    private fun ResultRow.toFornecedor() =
        Fornecedor(
            id = this[Fornecedores.id],
            nome = this[Fornecedores.nome],
            cnpj = this[Fornecedores.cnpj],
            telefone = this[Fornecedores.telefone],
            email = this[Fornecedores.email],
            contato = this[Fornecedores.contato],
            ativo = this[Fornecedores.ativo],
        )
}
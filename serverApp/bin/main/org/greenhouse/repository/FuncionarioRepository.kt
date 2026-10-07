package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Funcionario
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

private object FuncionarioTable : Table("funcionario") {
    val id = long("id").autoIncrement()
    val nome = varchar("nome", 150)
    val cpf = varchar("cpf", 11).uniqueIndex()
    val telefone = varchar("telefone", 20).nullable()
    val email = varchar("email", 150).uniqueIndex().nullable()
    val cargo = varchar("cargo", 100).nullable()
    val ativo = bool("ativo").default(true)

    override val primaryKey = PrimaryKey(id)
}

private object UsuarioTable : Table("usuario") {
    val id = long("id").autoIncrement()
    val funcionarioId = long("funcionario_id").references(FuncionarioTable.id)
    val login = varchar("login", 100).uniqueIndex()
    val senhaHash = varchar("senha_hash", 255)
    val perfil = varchar("perfil", 20)
    val ativo = bool("ativo").default(true)
    val primeiroAcessoFeito = bool("primeiro_acesso_feito").default(false)

    override val primaryKey = PrimaryKey(id)
}

@Repository
class FuncionarioRepository(
    private val databaseConfig: DatabaseConfig,
) {

    fun criar(
        funcionario: Funcionario,
        login: String,
        senhaPlaceholder: String,
    ): Funcionario =
        databaseConfig.session {
            if (UsuarioTable.selectAll().where { UsuarioTable.login eq login }.count() > 0) {
                throw UnprocessableEntityException("Já existe um usuário com o login derivado deste e-mail")
            }

            val id =
                FuncionarioTable.insert {
                    it[nome] = funcionario.nome
                    it[cpf] = funcionario.cpf
                    it[telefone] = funcionario.telefone
                    it[email] = funcionario.email
                    it[cargo] = funcionario.cargo
                    it[ativo] = funcionario.ativo
                }[FuncionarioTable.id]

            UsuarioTable.insert {
                it[UsuarioTable.funcionarioId] = id
                it[UsuarioTable.login] = login
                it[UsuarioTable.senhaHash] = senhaPlaceholder
                it[UsuarioTable.perfil] = "FUNCIONARIO"
                it[UsuarioTable.ativo] = funcionario.ativo
                it[UsuarioTable.primeiroAcessoFeito] = false
            }

            funcionario.copy(id = id)
        }

    fun listar(): List<Funcionario> =
        databaseConfig.session {
            FuncionarioTable.selectAll().map { it.toFuncionario() }
        }

    fun buscarPorId(id: Long): Funcionario? =
        databaseConfig.session {
            FuncionarioTable.selectAll()
                .where { FuncionarioTable.id eq id }
                .singleOrNull()
                ?.toFuncionario()
        }

    fun atualizar(id: Long, funcionario: Funcionario): Funcionario? =
        databaseConfig.session {
            val linhasAtualizadas =
                FuncionarioTable.update({ FuncionarioTable.id eq id }) {
                    it[nome] = funcionario.nome
                    it[cpf] = funcionario.cpf
                    it[telefone] = funcionario.telefone
                    it[email] = funcionario.email
                    it[cargo] = funcionario.cargo
                    it[ativo] = funcionario.ativo
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                UsuarioTable.update({ UsuarioTable.funcionarioId eq id }) {
                    it[UsuarioTable.ativo] = funcionario.ativo
                }

                FuncionarioTable.selectAll()
                    .where { FuncionarioTable.id eq id }
                    .single()
                    .toFuncionario()
            }
        }

    fun excluir(id: Long): Boolean =
        databaseConfig.session {
            FuncionarioTable.deleteWhere { FuncionarioTable.id eq id } > 0
        }

    private fun ResultRow.toFuncionario() =
        Funcionario(
            id = this[FuncionarioTable.id],
            nome = this[FuncionarioTable.nome],
            cpf = this[FuncionarioTable.cpf],
            telefone = this[FuncionarioTable.telefone],
            email = this[FuncionarioTable.email],
            cargo = this[FuncionarioTable.cargo],
            ativo = this[FuncionarioTable.ativo],
        )
}
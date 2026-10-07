package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Usuario
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

// Mapeamento auxiliar do Funcionario apenas para o Join do Primeiro Acesso
private object Funcionarios : Table("funcionario") {
    val id = long("id").autoIncrement()
    val nome = varchar("nome", 150)
    val cpf = varchar("cpf", 11)
    val telefone = varchar("telefone", 20).nullable()
    val email = varchar("email", 150).nullable()
    val cargo = varchar("cargo", 100).nullable()
    val ativo = bool("ativo").default(true)

    override val primaryKey = PrimaryKey(id)
}

private object Usuarios : Table("usuario") {
    val id = long("id").autoIncrement()
    val funcionarioId = long("funcionario_id").references(Funcionarios.id)
    val login = varchar("login", 100)
    val senhaHash = varchar("senha_hash", 255)
    val perfil = varchar("perfil", 20)
    val ativo = bool("ativo").default(true)
    val primeiroAcessoFeito = bool("primeiro_acesso_feito").default(false)

    override val primaryKey = PrimaryKey(id)
}

@Repository
class UsuarioRepository(
    private val databaseConfig: DatabaseConfig,
) {

    fun buscarPorLogin(login: String): Usuario? =
        databaseConfig.session {
            Usuarios.selectAll()
                .where { Usuarios.login eq login }
                .singleOrNull()
                ?.toUsuario()
        }

    fun buscarPorCpfEEmail(cpf: String, email: String): Usuario? =
        databaseConfig.session {
            (Usuarios innerJoin Funcionarios)
                .selectAll()
                .where { (Funcionarios.cpf eq cpf) and (Funcionarios.email eq email) }
                .singleOrNull()
                ?.toUsuario()
        }

    fun atualizarSenha(usuarioId: Long, novaSenhaHash: String): Boolean =
        databaseConfig.session {
            Usuarios.update({ Usuarios.id eq usuarioId }) {
                it[senhaHash] = novaSenhaHash
            } > 0
        }

    fun concluirPrimeiroAcesso(usuarioId: Long, novaSenhaHash: String): Boolean =
        databaseConfig.session {
            Usuarios.update({
                (Usuarios.id eq usuarioId) and (Usuarios.primeiroAcessoFeito eq false)
            }) {
                it[senhaHash] = novaSenhaHash
                it[primeiroAcessoFeito] = true
            } > 0
        }

    private fun ResultRow.toUsuario() =
        Usuario(
            id = this[Usuarios.id],
            funcionarioId = this[Usuarios.funcionarioId],
            login = this[Usuarios.login],
            senhaHash = this[Usuarios.senhaHash],
            perfil = this[Usuarios.perfil],
            ativo = this[Usuarios.ativo],
            primeiroAcessoFeito = this[Usuarios.primeiroAcessoFeito],
        )
}
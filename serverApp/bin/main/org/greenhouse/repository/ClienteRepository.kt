package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Cliente
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

private object Clientes : Table("cliente") {
    val id = long("id").autoIncrement()
    val nome = varchar("nome", 150)
    val telefone = varchar("telefone", 20)
    val whatsapp = bool("whatsapp").default(false)
    val cpf = varchar("cpf", 11).nullable()
    val email = varchar("email", 150).nullable()
    val ativo = bool("ativo").default(true)

    override val primaryKey = PrimaryKey(id)
}

@Repository
class ClienteRepository(
    private val databaseConfig: DatabaseConfig,
) {

    fun criar(cliente: Cliente): Cliente =
        databaseConfig.session {
            val id =
                Clientes.insert {
                    it[nome] = cliente.nome
                    it[telefone] = cliente.telefone
                    it[whatsapp] = cliente.whatsapp
                    it[cpf] = cliente.cpf
                    it[email] = cliente.email
                    it[ativo] = cliente.ativo
                }[Clientes.id]

            cliente.copy(id = id)
        }

    fun listar(
        nome: String? = null,
        telefone: String? = null,
        ativo: Boolean? = null,
    ): List<Cliente> =
        databaseConfig.session {
            val consulta = Clientes.selectAll()
            nome?.let { consulta.andWhere { Clientes.nome.lowerCase() like "%${it.lowercase()}%" } }
            telefone?.let { consulta.andWhere { Clientes.telefone like "%$it%" } }
            ativo?.let { valor -> consulta.andWhere { Clientes.ativo eq valor } }
            consulta.map { it.toCliente() }
        }

    fun buscarPorId(id: Long): Cliente? =
        databaseConfig.session {
            Clientes.selectAll()
                .where { Clientes.id eq id }
                .singleOrNull()
                ?.toCliente()
        }

    fun buscarPorCpf(cpf: String): Cliente? =
        databaseConfig.session {
            Clientes.selectAll()
                .where { Clientes.cpf eq cpf }
                .singleOrNull()
                ?.toCliente()
        }

    fun atualizar(id: Long, cliente: Cliente): Cliente? =
        databaseConfig.session {
            val linhasAtualizadas =
                Clientes.update({ Clientes.id eq id }) {
                    it[nome] = cliente.nome
                    it[telefone] = cliente.telefone
                    it[whatsapp] = cliente.whatsapp
                    it[cpf] = cliente.cpf
                    it[email] = cliente.email
                    it[ativo] = cliente.ativo
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                Clientes.selectAll()
                    .where { Clientes.id eq id }
                    .single()
                    .toCliente()
            }
        }

    fun excluir(id: Long): Boolean =
        databaseConfig.session {
            Clientes.deleteWhere { Clientes.id eq id } > 0
        }

    private fun ResultRow.toCliente() =
        Cliente(
            id = this[Clientes.id],
            nome = this[Clientes.nome],
            telefone = this[Clientes.telefone],
            whatsapp = this[Clientes.whatsapp],
            cpf = this[Clientes.cpf],
            email = this[Clientes.email],
            ativo = this[Clientes.ativo],
        )
}

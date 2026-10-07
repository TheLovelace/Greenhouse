package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.FormaPagamento
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

private object FormasPagamento : Table("forma_pagamento") {
    val id = long("id").autoIncrement()
    val nome = varchar("nome", 50)
    val ativo = bool("ativo").default(true)

    override val primaryKey = PrimaryKey(id)
}

@Repository
class FormaPagamentoRepository(
    private val databaseConfig: DatabaseConfig,
) {
    fun criar(formaPagamento: FormaPagamento): FormaPagamento =
        databaseConfig.session {
            val id =
                FormasPagamento.insert {
                    it[nome] = formaPagamento.nome
                    it[ativo] = formaPagamento.ativo
                }[FormasPagamento.id]

            formaPagamento.copy(id = id)
        }

    fun listar(): List<FormaPagamento> =
        databaseConfig.session {
            FormasPagamento
                .selectAll()
                .orderBy(FormasPagamento.id)
                .map { it.toFormaPagamento() }
        }

    fun buscarPorId(id: Long): FormaPagamento? =
        databaseConfig.session {
            FormasPagamento
                .selectAll()
                .where { FormasPagamento.id eq id }
                .singleOrNull()
                ?.toFormaPagamento()
        }

    fun atualizar(
        id: Long,
        formaPagamento: FormaPagamento,
    ): FormaPagamento? =
        databaseConfig.session {
            val linhasAtualizadas =
                FormasPagamento.update({ FormasPagamento.id eq id }) {
                    it[nome] = formaPagamento.nome
                    it[ativo] = formaPagamento.ativo
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                FormasPagamento
                    .selectAll()
                    .where { FormasPagamento.id eq id }
                    .single()
                    .toFormaPagamento()
            }
        }

    fun inativar(id: Long): Boolean =
        databaseConfig.session {
            FormasPagamento.update({ FormasPagamento.id eq id }) {
                it[ativo] = false
            } > 0
        }

    private fun ResultRow.toFormaPagamento() =
        FormaPagamento(
            id = this[FormasPagamento.id],
            nome = this[FormasPagamento.nome],
            ativo = this[FormasPagamento.ativo],
        )
}

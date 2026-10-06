package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Desconto
import org.greenhouse.entity.TipoDesconto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

private object Descontos : Table("desconto") {
    val id = long("id").autoIncrement()
    val titulo = varchar("titulo", 150)
    val descricao = text("descricao").nullable()
    val tipo = varchar("tipo", 20)
    val valor = decimal("valor", 12, 2)
    val dataInicio = timestampWithTimeZone("data_inicio").nullable()
    val dataFim = timestampWithTimeZone("data_fim").nullable()
    val ativo = bool("ativo").default(true)

    override val primaryKey = PrimaryKey(id)
}

@Repository
class DescontoRepository(
    private val databaseConfig: DatabaseConfig,
) {
    fun criar(desconto: Desconto): Desconto =
        databaseConfig.session {
            val id =
                Descontos.insert {
                    it[titulo] = desconto.titulo
                    it[descricao] = desconto.descricao
                    it[tipo] = desconto.tipo.name
                    it[valor] = desconto.valor
                    it[dataInicio] = desconto.dataInicio
                    it[dataFim] = desconto.dataFim
                    it[ativo] = desconto.ativo
                }[Descontos.id]

            desconto.copy(id = id)
        }

    fun listar(): List<Desconto> =
        databaseConfig.session {
            Descontos
                .selectAll()
                .orderBy(Descontos.id)
                .map { it.toDesconto() }
        }

    fun buscarPorId(id: Long): Desconto? =
        databaseConfig.session {
            Descontos
                .selectAll()
                .where { Descontos.id eq id }
                .singleOrNull()
                ?.toDesconto()
        }

    fun atualizar(
        id: Long,
        desconto: Desconto,
    ): Desconto? =
        databaseConfig.session {
            val linhasAtualizadas =
                Descontos.update(
                    { Descontos.id eq id },
                ) {
                    it[titulo] = desconto.titulo
                    it[descricao] = desconto.descricao
                    it[tipo] = desconto.tipo.name
                    it[valor] = desconto.valor
                    it[dataInicio] = desconto.dataInicio
                    it[dataFim] = desconto.dataFim
                    it[ativo] = desconto.ativo
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                Descontos
                    .selectAll()
                    .where { Descontos.id eq id }
                    .single()
                    .toDesconto()
            }
        }

    fun atualizarStatus(
        id: Long,
        ativo: Boolean,
    ): Desconto? =
        databaseConfig.session {
            val linhasAtualizadas =
                Descontos.update(
                    { Descontos.id eq id },
                ) {
                    it[Descontos.ativo] = ativo
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                Descontos
                    .selectAll()
                    .where { Descontos.id eq id }
                    .single()
                    .toDesconto()
            }
        }

    private fun ResultRow.toDesconto() =
        Desconto(
            id = this[Descontos.id],
            titulo = this[Descontos.titulo],
            descricao = this[Descontos.descricao],
            tipo = TipoDesconto.valueOf(this[Descontos.tipo]),
            valor = this[Descontos.valor],
            dataInicio = this[Descontos.dataInicio],
            dataFim = this[Descontos.dataFim],
            ativo = this[Descontos.ativo],
        )
}

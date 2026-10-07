package org.greenhouse.repository

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Categoria
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

private object Categorias : Table("categoria") {
    val id = long("id").autoIncrement()
    val nome = varchar("nome", 100)
    val descricao = text("descricao")
    val ativo = bool("ativo").default(true)

    override val primaryKey = PrimaryKey(id)
}

@Repository
class CategoriaRepository(
    private val databaseConfig: DatabaseConfig,
) {

    fun criar(categoria: Categoria): Categoria =
        databaseConfig.session {
            val id =
                Categorias.insert {
                    it[nome] = categoria.nome
                    it[descricao] = categoria.descricao
                    it[ativo] = categoria.ativo
                }[Categorias.id]

            categoria.copy(id = id)
        }

    fun listar(): List<Categoria> =
        databaseConfig.session {
            Categorias.selectAll().map { it.toCategoria() }
        }

    fun buscarPorId(id: Long): Categoria? =
        databaseConfig.session {
            Categorias.selectAll()
                .where { Categorias.id eq id }
                .singleOrNull()
                ?.toCategoria()
        }

    fun atualizar(id: Long, categoria: Categoria): Categoria? =
        databaseConfig.session {
            val linhasAtualizadas =
                Categorias.update({ Categorias.id eq id }) {
                    it[nome] = categoria.nome
                    it[descricao] = categoria.descricao
                    it[ativo] = categoria.ativo
                }

            if (linhasAtualizadas == 0) {
                null
            } else {
                Categorias.selectAll()
                    .where { Categorias.id eq id }
                    .single()
                    .toCategoria()
            }
        }

    fun excluir(id: Long): Boolean =
        databaseConfig.session {
            Categorias.deleteWhere { Categorias.id eq id } > 0
        }

    private fun ResultRow.toCategoria() =
        Categoria(
            id = this[Categorias.id],
            nome = this[Categorias.nome],
            descricao = this[Categorias.descricao],
            ativo = this[Categorias.ativo],
        )
}
package org.greenhouse.repository

import org.greenhouse.model.Categoria
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

interface CategoriaRepository : JpaRepository<Categoria, Long> {
    @Query("SELECT c FROM Categoria c")
    fun listar(): List<Categoria>

    @Query("SELECT c FROM Categoria c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    fun buscarPorNome(@Param("nome") nome: String): List<Categoria>

    @Query(
        """
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Categoria c
        WHERE c.nome = :nome AND (:ignorarId IS NULL OR c.id <> :ignorarId)
        """,
    )
    fun nomeEmUso(@Param("nome") nome: String, @Param("ignorarId") ignorarId: Long?): Boolean

    @Query("SELECT c FROM Categoria c WHERE c.id = :id")
    fun buscarPorId(@Param("id") id: Long): Categoria?

    fun criar(categoria: Categoria): Categoria = saveAndFlush(categoria)

    @Transactional
    fun atualizar(id: Long, categoria: Categoria): Categoria? {
        if (
            atualizarCampos(
                id,
                categoria.nome,
                categoria.descricao,
                categoria.ativo,
                categoria.observacao,
                requireNotNull(categoria.dataCadastro),
            ) == 0
        ) {
            return null
        }
        return buscarPorId(id)
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        """
        UPDATE Categoria c
        SET c.nome = :nome, c.descricao = :descricao, c.ativo = :ativo,
            c.observacao = :observacao, c.dataCadastro = :dataCadastro
        WHERE c.id = :id
        """,
    )
    fun atualizarCampos(
        @Param("id") id: Long,
        @Param("nome") nome: String,
        @Param("descricao") descricao: String,
        @Param("ativo") ativo: Boolean,
        @Param("observacao") observacao: String?,
        @Param("dataCadastro") dataCadastro: LocalDate,
    ): Int

    fun excluir(id: Long): Boolean = excluirPorId(id) > 0

    @Modifying
    @Transactional
    @Query("DELETE FROM Categoria c WHERE c.id = :id")
    fun excluirPorId(@Param("id") id: Long): Int
}

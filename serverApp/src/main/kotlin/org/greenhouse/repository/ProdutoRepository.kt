package org.greenhouse.repository

import jakarta.persistence.LockModeType
import org.greenhouse.model.Produto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

interface ProdutoRepository : JpaRepository<Produto, Long> {
    @Query("SELECT p FROM Produto p")
    fun listar(): List<Produto>

    @Query("SELECT p FROM Produto p WHERE p.id = :id")
    fun buscarPorId(@Param("id") id: Long): Produto?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Produto p WHERE p.id = :id")
    fun buscarParaAtualizar(@Param("id") id: Long): Produto?

    @Query(
        """
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM Produto p
        WHERE p.codigo = :codigo AND (:ignorarId IS NULL OR p.id <> :ignorarId)
        """,
    )
    fun codigoEmUso(
        @Param("codigo") codigo: String,
        @Param("ignorarId") ignorarId: Long?,
    ): Boolean

    fun criar(produto: Produto): Produto = save(produto)

    @Transactional
    fun atualizar(id: Long, produto: Produto): Produto? {
        if (atualizarCampos(
                id,
                produto.codigo,
                produto.nome,
                produto.descricao,
                produto.preco,
                produto.ativo,
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
        UPDATE Produto p
        SET p.codigo = :codigo, p.nome = :nome, p.descricao = :descricao,
            p.preco = :preco, p.ativo = :ativo
        WHERE p.id = :id
        """,
    )
    fun atualizarCampos(
        @Param("id") id: Long,
        @Param("codigo") codigo: String?,
        @Param("nome") nome: String,
        @Param("descricao") descricao: String?,
        @Param("preco") preco: BigDecimal,
        @Param("ativo") ativo: Boolean,
    ): Int

    @Query(
        value = """
            SELECT
                EXISTS (SELECT 1 FROM pedido_item WHERE produto_id = :id)
                OR EXISTS (SELECT 1 FROM compra_item WHERE produto_id = :id)
                OR EXISTS (SELECT 1 FROM estoque WHERE produto_id = :id)
                OR EXISTS (SELECT 1 FROM movimentacao_estoque WHERE produto_id = :id)
        """,
        nativeQuery = true,
    )
    fun possuiDependencias(@Param("id") id: Long): Boolean

    @Modifying
    @Transactional
    @Query("DELETE FROM Produto p WHERE p.id = :id")
    fun excluirPorId(@Param("id") id: Long): Int

    fun excluir(id: Long): Boolean = excluirPorId(id) > 0
}

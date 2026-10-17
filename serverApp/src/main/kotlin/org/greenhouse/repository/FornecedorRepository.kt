package org.greenhouse.repository

import org.greenhouse.model.Fornecedor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface FornecedorRepository : JpaRepository<Fornecedor, Long> {
    @Query("SELECT f FROM Fornecedor f ORDER BY f.id")
    fun listar(): List<Fornecedor>

    @Query("SELECT f FROM Fornecedor f WHERE LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY f.id")
    fun buscarPorNome(@Param("nome") nome: String): List<Fornecedor>

    @Query("SELECT f FROM Fornecedor f WHERE f.id = :id")
    fun buscarPorId(@Param("id") id: Long): Fornecedor?

    @Query(
        """
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM Fornecedor f
        WHERE f.cnpj = :cnpj AND (:ignorarId IS NULL OR f.id <> :ignorarId)
        """,
    )
    fun cnpjEmUso(
        @Param("cnpj") cnpj: String,
        @Param("ignorarId") ignorarId: Long?,
    ): Boolean

    @Query(
        value = "SELECT EXISTS (SELECT 1 FROM compra_fornecedor WHERE fornecedor_id = :id)",
        nativeQuery = true,
    )
    fun possuiCompras(@Param("id") id: Long): Boolean

    fun criar(fornecedor: Fornecedor): Fornecedor = save(fornecedor)

    @Transactional
    fun atualizar(
        id: Long,
        fornecedor: Fornecedor,
    ): Fornecedor? {
        if (atualizarCampos(
                id,
                fornecedor.nome,
                fornecedor.cnpj,
                fornecedor.telefone,
                fornecedor.email,
                fornecedor.contato,
                fornecedor.ativo,
                fornecedor.prazoMedioEntregaDias,
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
        UPDATE Fornecedor f
        SET f.nome = :nome, f.cnpj = :cnpj, f.telefone = :telefone,
            f.email = :email, f.contato = :contato, f.ativo = :ativo,
            f.prazoMedioEntregaDias = :prazoMedioEntregaDias
        WHERE f.id = :id
        """,
    )
    fun atualizarCampos(
        @Param("id") id: Long,
        @Param("nome") nome: String,
        @Param("cnpj") cnpj: String?,
        @Param("telefone") telefone: String?,
        @Param("email") email: String?,
        @Param("contato") contato: String?,
        @Param("ativo") ativo: Boolean,
        @Param("prazoMedioEntregaDias") prazoMedioEntregaDias: Int?,
    ): Int

    fun excluir(id: Long): Boolean = excluirPorId(id) > 0

    @Modifying
    @Transactional
    @Query("DELETE FROM Fornecedor f WHERE f.id = :id")
    fun excluirPorId(@Param("id") id: Long): Int
}

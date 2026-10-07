package org.greenhouse.repository

import org.greenhouse.model.Fornecedor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface FornecedorRepository : JpaRepository<Fornecedor, Long> {
    @Query("SELECT f FROM Fornecedor f")
    fun listar(): List<Fornecedor>

    @Query("SELECT f FROM Fornecedor f WHERE f.id = :id")
    fun buscarPorId(@Param("id") id: Long): Fornecedor?

    fun criar(fornecedor: Fornecedor): Fornecedor = save(fornecedor)

    @Transactional
    fun atualizar(id: Long, fornecedor: Fornecedor): Fornecedor? {
        if (atualizarCampos(
                id,
                fornecedor.nome,
                fornecedor.cnpj,
                fornecedor.telefone,
                fornecedor.email,
                fornecedor.contato,
                fornecedor.ativo,
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
            f.email = :email, f.contato = :contato, f.ativo = :ativo
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
    ): Int

    fun excluir(id: Long): Boolean = excluirPorId(id) > 0

    @Modifying
    @Transactional
    @Query("DELETE FROM Fornecedor f WHERE f.id = :id")
    fun excluirPorId(@Param("id") id: Long): Int
}

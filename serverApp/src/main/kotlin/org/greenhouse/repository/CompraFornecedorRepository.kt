package org.greenhouse.repository

import jakarta.persistence.LockModeType
import org.greenhouse.model.CompraFornecedor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface CompraFornecedorRepository : JpaRepository<CompraFornecedor, Long> {
    @Query("SELECT c FROM CompraFornecedor c ORDER BY c.dataCompra DESC, c.id DESC")
    fun listar(): List<CompraFornecedor>

    @Query(
        """
        SELECT c FROM CompraFornecedor c
        WHERE LOWER(c.observacao) LIKE LOWER(CONCAT('%', :observacao, '%'))
        ORDER BY c.dataCompra DESC, c.id DESC
        """,
    )
    fun buscarPorObservacao(@Param("observacao") observacao: String): List<CompraFornecedor>

    @Query("SELECT c FROM CompraFornecedor c WHERE c.id = :id")
    fun buscarPorId(@Param("id") id: Long): CompraFornecedor?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CompraFornecedor c WHERE c.id = :id")
    fun buscarParaAtualizar(@Param("id") id: Long): CompraFornecedor?

    fun excluir(id: Long): Boolean = excluirPorId(id) > 0

    @Modifying
    @Transactional
    @Query("DELETE FROM CompraFornecedor c WHERE c.id = :id")
    fun excluirPorId(@Param("id") id: Long): Int
}

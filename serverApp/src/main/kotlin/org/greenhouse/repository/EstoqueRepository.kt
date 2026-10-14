package org.greenhouse.repository

import jakarta.persistence.LockModeType
import org.greenhouse.model.Estoque
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EstoqueRepository : JpaRepository<Estoque, Long> {
    @Query("SELECT e FROM Estoque e")
    fun listar(): List<Estoque>

    @Query("SELECT e FROM Estoque e WHERE e.produtoId = :produtoId")
    fun buscarPorProdutoId(@Param("produtoId") produtoId: Long): Estoque?

    @Query("SELECT e FROM Estoque e WHERE LOWER(e.localizacao) LIKE LOWER(CONCAT('%', :localizacao, '%'))")
    fun buscarPorLocalizacao(@Param("localizacao") localizacao: String): List<Estoque>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Estoque e WHERE e.produtoId = :produtoId")
    fun buscarParaAtualizar(@Param("produtoId") produtoId: Long): Estoque?
}

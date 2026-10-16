package org.greenhouse.repository

import org.greenhouse.model.CompraItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface CompraItemRepository : JpaRepository<CompraItem, Long> {
    @Query("SELECT i FROM CompraItem i WHERE i.compraId = :compraId ORDER BY i.id")
    fun buscarPorCompraId(@Param("compraId") compraId: Long): List<CompraItem>

    @Query("SELECT i FROM CompraItem i WHERE i.compraId IN :compraIds ORDER BY i.id")
    fun buscarPorCompraIds(@Param("compraIds") compraIds: List<Long>): List<CompraItem>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM CompraItem i WHERE i.compraId = :compraId")
    fun excluirPorCompraId(@Param("compraId") compraId: Long): Int
}

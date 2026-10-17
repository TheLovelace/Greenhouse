package org.greenhouse.repository

import org.greenhouse.model.PedidoItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface PedidoItemRepository : JpaRepository<PedidoItem, Long> {
    @Query("SELECT i FROM PedidoItem i WHERE i.pedidoId = :pedidoId ORDER BY i.id")
    fun buscarPorPedidoId(@Param("pedidoId") pedidoId: Long): List<PedidoItem>

    @Query("SELECT i FROM PedidoItem i WHERE i.pedidoId IN :pedidoIds ORDER BY i.id")
    fun buscarPorPedidoIds(@Param("pedidoIds") pedidoIds: List<Long>): List<PedidoItem>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM PedidoItem i WHERE i.pedidoId = :pedidoId")
    fun excluirPorPedidoId(@Param("pedidoId") pedidoId: Long): Int
}

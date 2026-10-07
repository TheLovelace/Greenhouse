package org.greenhouse.repository

import org.greenhouse.model.MovimentacaoEstoque
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MovimentacaoEstoqueRepository : JpaRepository<MovimentacaoEstoque, Long> {
    @Query(
        """
        SELECT m FROM MovimentacaoEstoque m
        ORDER BY m.dataHora DESC, m.id DESC
        """,
    )
    fun listarMovimentacoes(): List<MovimentacaoEstoque>

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.id = :id")
    fun buscarPorId(@Param("id") id: Long): MovimentacaoEstoque?
}

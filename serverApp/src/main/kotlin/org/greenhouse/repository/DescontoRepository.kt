package org.greenhouse.repository

import org.greenhouse.model.Desconto
import org.greenhouse.enums.TipoDesconto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

interface DescontoRepository : JpaRepository<Desconto, Long> {
    @Query("SELECT d FROM Desconto d ORDER BY d.id")
    fun listar(): List<Desconto>

    @Query("SELECT d FROM Desconto d WHERE d.id = :id")
    fun buscarPorId(@Param("id") id: Long): Desconto?

    @Query("SELECT d FROM Desconto d WHERE LOWER(d.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    fun buscarPorTitulo(@Param("titulo") titulo: String): List<Desconto>

    fun criar(desconto: Desconto): Desconto = save(desconto)

    @Transactional
    fun atualizar(id: Long, desconto: Desconto): Desconto? {
        if (atualizarCampos(
                id,
                desconto.titulo,
                desconto.descricao,
                desconto.tipo,
                desconto.valor,
                desconto.dataInicio,
                desconto.dataFim,
                desconto.ativo,
                desconto.quantidadeMinimaItens,
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
        UPDATE Desconto d
        SET d.titulo = :titulo, d.descricao = :descricao, d.tipo = :tipo,
            d.valor = :valor, d.dataInicio = :dataInicio, d.dataFim = :dataFim,
            d.ativo = :ativo, d.quantidadeMinimaItens = :quantidadeMinimaItens
        WHERE d.id = :id
        """,
    )
    fun atualizarCampos(
        @Param("id") id: Long,
        @Param("titulo") titulo: String,
        @Param("descricao") descricao: String?,
        @Param("tipo") tipo: TipoDesconto,
        @Param("valor") valor: BigDecimal,
        @Param("dataInicio") dataInicio: OffsetDateTime?,
        @Param("dataFim") dataFim: OffsetDateTime?,
        @Param("ativo") ativo: Boolean,
        @Param("quantidadeMinimaItens") quantidadeMinimaItens: Int,
    ): Int

    fun inativar(id: Long): Boolean = inativarPorId(id) > 0

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Desconto d SET d.ativo = false WHERE d.id = :id")
    fun inativarPorId(@Param("id") id: Long): Int
}

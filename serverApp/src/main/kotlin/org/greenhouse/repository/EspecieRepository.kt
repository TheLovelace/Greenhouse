package org.greenhouse.repository

import org.greenhouse.model.Especie
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EspecieRepository : JpaRepository<Especie, Long> {

    @Query(
        """
        SELECT e FROM Especie e
        WHERE LOWER(e.nomePopular) LIKE LOWER(CONCAT('%', :nome, '%'))
        ORDER BY e.nomePopular
        """,
    )
    fun buscarPorNomePopular(@Param("nome") nome: String): List<Especie>

    /** Regras de alerta que referenciam a espécie (histórico que não pode ser perdido). */
    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.especie.id = :id")
    fun contarAlertas(@Param("id") id: Long): Long

    /** Vínculos com produtos (tabela produto_especie, sem entidade JPA própria). */
    @Query(value = "SELECT COUNT(*) FROM produto_especie WHERE especie_id = :id", nativeQuery = true)
    fun contarProdutosVinculados(@Param("id") id: Long): Long
}
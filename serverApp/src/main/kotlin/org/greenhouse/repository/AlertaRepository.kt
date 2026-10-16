package org.greenhouse.repository

import org.greenhouse.model.Alerta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AlertaRepository : JpaRepository<Alerta, Long> {

    @Query("SELECT a FROM Alerta a JOIN FETCH a.dispositivo JOIN FETCH a.especie ORDER BY a.id")
    fun listarTodos(): List<Alerta>

    @Query(
        """
        SELECT a FROM Alerta a JOIN FETCH a.dispositivo JOIN FETCH a.especie
        WHERE LOWER(a.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))
        ORDER BY a.id
        """,
    )
    fun buscarPorTitulo(@Param("titulo") titulo: String): List<Alerta>

    @Query("SELECT a FROM Alerta a JOIN FETCH a.dispositivo JOIN FETCH a.especie WHERE a.id = :id")
    fun buscarComRelacionamentos(@Param("id") id: Long): Alerta?
}
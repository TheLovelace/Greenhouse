package org.greenhouse.repository

import org.greenhouse.model.LeituraIot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface LeituraIotRepository :
    JpaRepository<LeituraIot, Long> {

    @Query(
        """
        SELECT l FROM LeituraIot l
        JOIN FETCH l.dispositivo
        WHERE l.id = :id
        """
    )
    fun buscarPorIdComDispositivo(
        @Param("id") id: Long,
    ): LeituraIot?

    @Query(
        """
        SELECT l FROM LeituraIot l
        JOIN FETCH l.dispositivo
        WHERE l.dispositivo.id = :dispositivoId
          AND l.dataLeitura >= :desde
        ORDER BY l.dataLeitura ASC, l.id ASC
        """
    )
    fun listarDesdePorDispositivo(
        @Param("dispositivoId") dispositivoId: Long,
        @Param("desde") desde: OffsetDateTime,
    ): List<LeituraIot>

    @Query(
        """
        SELECT l FROM LeituraIot l
        JOIN FETCH l.dispositivo
        WHERE l.dataLeitura >= :desde
        ORDER BY l.dataLeitura ASC, l.id ASC
        """
    )
    fun listarDesde(
        @Param("desde") desde: OffsetDateTime,
    ): List<LeituraIot>
}
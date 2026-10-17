package org.greenhouse.repository

import org.greenhouse.model.DispositivoIot
import org.springframework.data.jpa.repository.JpaRepository

interface DispositivoIotRepository :
    JpaRepository<DispositivoIot, Long> {

    fun findByAtivoTrueOrderByIdAsc(): List<DispositivoIot>
}
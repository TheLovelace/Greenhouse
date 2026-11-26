package org.greenhouse.repository

import jakarta.persistence.LockModeType
import org.greenhouse.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT token FROM RefreshToken token WHERE token.tokenHash = :tokenHash")
    fun buscarPorHashParaAtualizacao(@Param("tokenHash") tokenHash: String): RefreshToken?
}

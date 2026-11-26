package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "refresh_token")
data class RefreshToken(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(name = "usuario_id", nullable = false)
    var usuarioId: Long = 0,
    @field:Column(name = "token_hash", nullable = false, unique = true, length = 64)
    var tokenHash: String = "",
    @field:Column(name = "criado_em", nullable = false)
    var criadoEm: Instant = Instant.now(),
    @field:Column(name = "expira_em", nullable = false)
    var expiraEm: Instant = Instant.now(),
    @field:Column(name = "revogado_em")
    var revogadoEm: Instant? = null,
)

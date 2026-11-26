package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(
    val accessToken: String,
    val profile: String,
    val refreshToken: String? = null,
    val expiresInSeconds: Long = 86_400,
)

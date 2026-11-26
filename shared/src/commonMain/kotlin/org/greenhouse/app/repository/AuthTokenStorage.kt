package org.greenhouse.app.repository

import org.greenhouse.app.model.AuthSession

interface AuthTokenStorage {
    suspend fun load(): AuthSession?

    suspend fun save(
        session: AuthSession,
        rememberDevice: Boolean,
    )

    suspend fun clear()
}

expect fun createAuthTokenStorage(): AuthTokenStorage

internal fun AuthSession.persistedFor(rememberDevice: Boolean): AuthSession =
    if (rememberDevice) {
        copy(accessToken = "", profile = "", expiresInSeconds = 0)
    } else {
        this
    }

object InMemoryAuthTokenStorage : AuthTokenStorage {
    private var session: AuthSession? = null

    override suspend fun load(): AuthSession? = session

    override suspend fun save(
        session: AuthSession,
        rememberDevice: Boolean,
    ) {
        this.session = session
    }

    override suspend fun clear() {
        session = null
    }
}

package org.greenhouse.app.repository

import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.greenhouse.app.model.AuthSession

actual fun createAuthTokenStorage(): AuthTokenStorage = BrowserAuthTokenStorage

private object BrowserAuthTokenStorage : AuthTokenStorage {
    override suspend fun load(): AuthSession? =
        (sessionStorage.getItem(SESSION_KEY) ?: localStorage.getItem(SESSION_KEY))
            ?.let { Json.decodeFromString<AuthSession>(it) }

    override suspend fun save(
        session: AuthSession,
        rememberDevice: Boolean,
    ) {
        if (rememberDevice) {
            localStorage.setItem(SESSION_KEY, Json.encodeToString(session.persistedFor(rememberDevice = true)))
            sessionStorage.removeItem(SESSION_KEY)
        } else {
            sessionStorage.setItem(SESSION_KEY, Json.encodeToString(session))
            localStorage.removeItem(SESSION_KEY)
        }
    }

    override suspend fun clear() {
        sessionStorage.removeItem(SESSION_KEY)
        localStorage.removeItem(SESSION_KEY)
    }

    private const val SESSION_KEY = "greenhouse.auth.session"
}

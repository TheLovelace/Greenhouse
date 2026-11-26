package org.greenhouse.app.repository

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.greenhouse.app.model.AuthSession
import platform.Foundation.NSUserDefaults

object IosAuthTokenStorage : AuthTokenStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun load(): AuthSession? =
        defaults.stringForKey(SESSION_KEY)?.let { Json.decodeFromString<AuthSession>(it) }

    override suspend fun save(
        session: AuthSession,
        rememberDevice: Boolean,
    ) {
        if (rememberDevice) {
            defaults.setObject(Json.encodeToString(session.persistedFor(rememberDevice = true)), forKey = SESSION_KEY)
        } else {
            defaults.removeObjectForKey(SESSION_KEY)
        }
    }

    override suspend fun clear() {
        defaults.removeObjectForKey(SESSION_KEY)
    }

    private const val SESSION_KEY = "session"
}

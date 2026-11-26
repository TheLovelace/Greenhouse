package org.greenhouse.app.repository

import java.util.prefs.Preferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.greenhouse.app.model.AuthSession

object DesktopAuthTokenStorage : AuthTokenStorage {
    private val preferences = Preferences.userNodeForPackage(DesktopAuthTokenStorage::class.java)

    override suspend fun load(): AuthSession? =
        preferences.get(SESSION_KEY, null)?.let { Json.decodeFromString<AuthSession>(it) }

    override suspend fun save(
        session: AuthSession,
        rememberDevice: Boolean,
    ) {
        if (rememberDevice) {
            preferences.put(SESSION_KEY, Json.encodeToString(session.persistedFor(rememberDevice = true)))
        } else {
            preferences.remove(SESSION_KEY)
        }
        preferences.flush()
    }

    override suspend fun clear() {
        preferences.remove(SESSION_KEY)
        preferences.flush()
    }

    private const val SESSION_KEY = "session"
}

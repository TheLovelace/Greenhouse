package org.greenhouse.app.repository

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.greenhouse.app.model.AuthSession

class AndroidAuthTokenStorage(
    context: Context,
) : AuthTokenStorage {
    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )

    override suspend fun load(): AuthSession? = preferences.getString(SESSION_KEY, null)?.let { Json.decodeFromString<AuthSession>(it) }

    override suspend fun save(
        session: AuthSession,
        rememberDevice: Boolean,
    ) {
        val editor = preferences.edit()
        if (rememberDevice) {
            editor.putString(SESSION_KEY, Json.encodeToString(session.persistedFor(rememberDevice = true)))
        } else {
            editor.remove(SESSION_KEY)
        }
        check(editor.commit()) { "Não foi possível salvar a sessão neste dispositivo." }
    }

    override suspend fun clear() {
        check(preferences.edit().remove(SESSION_KEY).commit()) {
            "Não foi possível remover a sessão deste dispositivo."
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "greenhouse_auth"
        const val SESSION_KEY = "session"
    }
}

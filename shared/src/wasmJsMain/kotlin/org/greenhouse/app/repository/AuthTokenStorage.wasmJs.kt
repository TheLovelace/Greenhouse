@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.greenhouse.app.repository

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.greenhouse.app.model.AuthSession

actual fun createAuthTokenStorage(): AuthTokenStorage = BrowserAuthTokenStorage

private object BrowserAuthTokenStorage : AuthTokenStorage {
    override suspend fun load(): AuthSession? =
        (getSessionValue() ?: getRememberedValue())
            ?.let { Json.decodeFromString<AuthSession>(it) }

    override suspend fun save(
        session: AuthSession,
        rememberDevice: Boolean,
    ) {
        if (rememberDevice) {
            setRememberedValue(Json.encodeToString(session.persistedFor(rememberDevice = true)))
            removeSessionValue()
        } else {
            setSessionValue(Json.encodeToString(session))
            removeRememberedValue()
        }
    }

    override suspend fun clear() {
        removeSessionValue()
        removeRememberedValue()
    }

    private const val SESSION_KEY = "greenhouse.auth.session"

}

private fun getSessionValue(): String? = js("globalThis.sessionStorage.getItem('greenhouse.auth.session')")

private fun getRememberedValue(): String? = js("globalThis.localStorage.getItem('greenhouse.auth.session')")

private fun setSessionValue(value: String): Unit =
    js("globalThis.sessionStorage.setItem('greenhouse.auth.session', value)")

private fun setRememberedValue(value: String): Unit =
    js("globalThis.localStorage.setItem('greenhouse.auth.session', value)")

private fun removeSessionValue(): Unit =
    js("globalThis.sessionStorage.removeItem('greenhouse.auth.session')")

private fun removeRememberedValue(): Unit =
    js("globalThis.localStorage.removeItem('greenhouse.auth.session')")

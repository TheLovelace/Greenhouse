package org.greenhouse.app.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.greenhouse.app.model.AuthSession

import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

object AuthSessionManager {
    private val _session = MutableStateFlow<AuthSession?>(null)
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val refreshMutex = Mutex()
    private var storage: AuthTokenStorage? = null
    private var refreshSession: (suspend (String) -> AuthSession)? = null
    private var accessTokenDeadline: TimeMark? = null

    val accessToken: String?
        get() = _session.value?.accessToken

    fun configure(
        tokenStorage: AuthTokenStorage,
        refresh: suspend (String) -> AuthSession,
    ) {
        storage = tokenStorage
        refreshSession = refresh
    }

    suspend fun restoreSession() {
        val persistedSession = storage?.load() ?: return
        val refreshToken = persistedSession.refreshToken
        if (refreshToken == null) {
            publish(persistedSession)
            return
        }

        try {
            val renewedSession =
                refreshSession?.invoke(refreshToken)
                    ?: error("Refresh do token não foi configurado")
            storage?.save(renewedSession, rememberDevice = true)
            publish(renewedSession)
        } catch (exception: ResponseException) {
            if (exception.response.status == HttpStatusCode.Unauthorized) {
                clearSession("Sua sessão expirou. Entre novamente.")
            } else {
                throw exception
            }
        }
    }

    suspend fun startSession(
        session: AuthSession,
        rememberDevice: Boolean,
    ) {
        storage?.save(session, rememberDevice)
        publish(session)
    }

    suspend fun logout() {
        storage?.clear()
        publish(null)
        _message.value = null
    }

    suspend fun refreshAccessTokenIfNeeded() {
        val currentSession = _session.value ?: return
        if (currentSession.refreshToken == null || !accessTokenIsNearExpiry()) return

        refreshMutex.withLock {
            val latestSession = _session.value ?: return@withLock
            val refreshToken = latestSession.refreshToken ?: return@withLock
            if (!accessTokenIsNearExpiry()) return@withLock

            try {
                val renewedSession =
                    refreshSession?.invoke(refreshToken)
                        ?: error("Refresh do token não foi configurado")
                storage?.save(renewedSession, rememberDevice = true)
                publish(renewedSession)
            } catch (exception: ResponseException) {
                if (exception.response.status == HttpStatusCode.Unauthorized) {
                    clearSession("Sua sessão expirou. Entre novamente.")
                } else if (accessTokenDeadline?.elapsedNow()?.let { it >= 0.seconds } == true) {
                    throw exception
                }
            }
        }
    }

    suspend fun handleUnauthorizedResponse() {
        if (_session.value != null) {
            clearSession("Sua sessão expirou. Entre novamente.")
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private suspend fun clearSession(message: String) {
        storage?.clear()
        publish(null)
        _message.value = message
    }

    private fun publish(session: AuthSession?) {
        _session.value = session
        accessTokenDeadline =
            session?.let {
                TimeSource.Monotonic.markNow() + it.expiresInSeconds.seconds
            }
    }

    private fun accessTokenIsNearExpiry(): Boolean {
        val remaining = accessTokenDeadline?.elapsedNow() ?: return true
        return remaining >= (-60).seconds
    }
}

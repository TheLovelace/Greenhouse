package org.greenhouse.app.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.greenhouse.app.model.AuthSession

class AuthRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/auth"

    suspend fun entrar(
        emailOuLogin: String,
        senha: String,
        lembrarDispositivo: Boolean,
    ): AuthSession =
        httpClient
            .post("$endPoint/signin") {
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(
                        login = loginIdFrom(emailOuLogin),
                        senhaHash = senha,
                        lembrarDispositivo = lembrarDispositivo,
                    ),
                )
            }.body<AuthTokenResponse>()
            .toSession()

    suspend fun renovar(refreshToken: String): AuthSession =
        httpClient
            .post("$endPoint/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }.body<AuthTokenResponse>()
            .toSession()

    suspend fun validarPrimeiroAcesso(login: String) {
        httpClient.post("$endPoint/first-access/check") {
            contentType(ContentType.Application.Json)
            setBody(FirstAccessCheckRequest(loginIdFrom(login)))
        }
    }

    suspend fun definirSenhaPrimeiroAcesso(
        login: String,
        cpf: String,
        email: String,
        novaSenha: String,
    ) {
        httpClient.post("$endPoint/signup") {
            contentType(ContentType.Application.Json)
            setBody(
                FirstAccessRequest(
                    login = loginIdFrom(login),
                    cpf = cpf,
                    email = email.trim(),
                    novaSenha = novaSenha,
                ),
            )
        }
    }

    private fun loginIdFrom(emailOuLogin: String): String =
        emailOuLogin
            .trim()
            .substringBefore('@')
            .lowercase()
}

@Serializable
private data class LoginRequest(
    val login: String,
    val senhaHash: String,
    val lembrarDispositivo: Boolean,
)

@Serializable
private data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
private data class FirstAccessCheckRequest(
    val login: String,
)

@Serializable
private data class FirstAccessRequest(
    val login: String,
    val cpf: String,
    val email: String,
    val novaSenha: String,
)

@Serializable
private data class AuthTokenResponse(
    val token: String,
    val perfil: String,
    val refreshToken: String? = null,
    val expiresInSeconds: Long = 86_400,
) {
    fun toSession() =
        AuthSession(
            accessToken = token,
            profile = perfil,
            refreshToken = refreshToken,
            expiresInSeconds = expiresInSeconds,
        )
}

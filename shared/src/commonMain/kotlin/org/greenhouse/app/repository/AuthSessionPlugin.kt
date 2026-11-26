package org.greenhouse.app.repository

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

val AuthSessionPlugin =
    createClientPlugin("AuthSessionPlugin") {
        onRequest { request, _ ->
            val requestPath =
                request.url
                    .buildString()
                    .substringBefore('?')
            if (!requestPath.isSessionEndpoint()) {
                AuthSessionManager.refreshAccessTokenIfNeeded()
                AuthSessionManager.accessToken?.let { token ->
                    request.headers[HttpHeaders.Authorization] = "Bearer $token"
                }
            }
        }

        onResponse { response ->
            val requestPath =
                response.call
                    .request
                    .url
                    .toString()
                    .substringBefore('?')
            if (
                response.status == HttpStatusCode.Unauthorized &&
                !requestPath.isSessionEndpoint()
            ) {
                AuthSessionManager.handleUnauthorizedResponse()
            }
        }
    }

private fun String.isSessionEndpoint(): Boolean = endsWith("/auth/signin") || endsWith("/auth/refresh")

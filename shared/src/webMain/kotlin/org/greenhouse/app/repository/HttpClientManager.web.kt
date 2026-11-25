package org.greenhouse.app.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.JsClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual val API_URL: String = ApiConfig.API_URL

actual fun createHttpClient() =
    HttpClient(JsClient()) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(
                Json {
                    encodeDefaults = true
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                },
            )
        }
        install(Logging) {
            level = LogLevel.INFO
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }

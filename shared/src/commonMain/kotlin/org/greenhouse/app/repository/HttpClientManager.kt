package org.greenhouse.app.repository

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient

expect val API_URL: String

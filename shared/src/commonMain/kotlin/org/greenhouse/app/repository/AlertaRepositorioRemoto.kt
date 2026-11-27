package org.greenhouse.app.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.greenhouse.app.model.Alerta
import org.greenhouse.app.model.AlertaRequest
import org.greenhouse.app.model.DispositivoIotResumo

class AlertaRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/alertas"

    suspend fun listar(): List<Alerta> = httpClient.get(endPoint).body()

    suspend fun listarDispositivos(): List<DispositivoIotResumo> =
        httpClient.get("$endPoint/dispositivos").body()

    suspend fun buscarPorId(id: Long): Alerta = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(alerta: AlertaRequest): Alerta =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(alerta)
            }.body()

    suspend fun atualizar(
        id: Long,
        alerta: AlertaRequest,
    ): Alerta =
        httpClient
            .put("$endPoint/$id") {
                contentType(ContentType.Application.Json)
                setBody(alerta)
            }.body()

    suspend fun alternarStatus(id: Long): Alerta =
        httpClient.patch("$endPoint/$id/status").body()
}
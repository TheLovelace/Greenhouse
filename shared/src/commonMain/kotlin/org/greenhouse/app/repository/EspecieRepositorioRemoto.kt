package org.greenhouse.app.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.greenhouse.app.model.Especie
import org.greenhouse.app.model.EspecieRequest

class EspecieRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/especies"

    suspend fun listar(): List<Especie> = httpClient.get(endPoint).body()

    suspend fun buscarPorId(id: Long): Especie = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(especie: EspecieRequest): Especie =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(especie)
            }.body()

    suspend fun atualizar(
        id: Long,
        especie: EspecieRequest,
    ): Especie =
        httpClient
            .put("$endPoint/$id") {
                contentType(ContentType.Application.Json)
                setBody(especie)
            }.body()

    suspend fun alternarStatus(id: Long): Especie =
        httpClient.patch("$endPoint/$id/status").body()

    suspend fun excluir(id: Long) {
        httpClient.delete("$endPoint/$id")
    }
}
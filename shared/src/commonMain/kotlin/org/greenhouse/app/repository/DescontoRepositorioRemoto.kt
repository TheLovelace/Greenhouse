package org.greenhouse.app.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.greenhouse.app.model.Desconto
import org.greenhouse.app.model.DescontoRequest

class DescontoRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/desconto"

    suspend fun listar(): List<Desconto> = httpClient.get(endPoint).body()

    suspend fun buscarPorId(id: Long): Desconto = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(desconto: DescontoRequest): Desconto =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(desconto)
            }.body()

    suspend fun atualizar(
        id: Long,
        desconto: DescontoRequest,
    ): Desconto =
        httpClient
            .put("$endPoint/$id") {
                contentType(ContentType.Application.Json)
                setBody(desconto)
            }.body()

    suspend fun excluir(id: Long) {
        httpClient.delete("$endPoint/$id")
    }
}

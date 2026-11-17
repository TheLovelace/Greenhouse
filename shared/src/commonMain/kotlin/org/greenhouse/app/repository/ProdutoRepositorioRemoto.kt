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
import org.greenhouse.app.model.Produto

class ProdutoRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/produto"

    suspend fun listar(): List<Produto> = httpClient.get(endPoint).body()

    suspend fun buscarPorId(id: Long): Produto = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(produto: Produto): Produto =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(produto)
            }.body()

    suspend fun atualizar(produto: Produto): Produto =
        httpClient
            .put("$endPoint/${produto.id}") {
                contentType(ContentType.Application.Json)
                setBody(produto)
            }.body()

    suspend fun apagar(id: Long) {
        httpClient.delete("$endPoint/$id")
    }
}

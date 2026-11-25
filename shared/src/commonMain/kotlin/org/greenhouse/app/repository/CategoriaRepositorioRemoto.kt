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
import org.greenhouse.app.model.Categoria

class CategoriaRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/categoria"

    suspend fun listar(): List<Categoria> = httpClient.get(endPoint).body()

    suspend fun buscarPorId(id: Long): Categoria = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(categoria: Categoria): Categoria =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(categoria)
            }.body()

    suspend fun atualizar(categoria: Categoria): Categoria =
        httpClient
            .put("$endPoint/${categoria.id}") {
                contentType(ContentType.Application.Json)
                setBody(categoria)
            }.body()

    suspend fun apagar(id: Long) {
        httpClient.delete("$endPoint/$id")
    }
}

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
import org.greenhouse.app.model.Estoque
import org.greenhouse.app.model.EstoqueCreateRequest
import org.greenhouse.app.model.EstoqueUpdateRequest

class EstoqueRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/estoque"

    suspend fun listar(): List<Estoque> = httpClient.get(endPoint).body()

    suspend fun buscarPorProdutoId(produtoId: Long): Estoque = httpClient.get("$endPoint/$produtoId").body()

    suspend fun salvar(estoque: EstoqueCreateRequest): Estoque =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(estoque)
            }.body()

    suspend fun atualizar(
        produtoId: Long,
        estoque: EstoqueUpdateRequest,
    ): Estoque =
        httpClient
            .put("$endPoint/$produtoId") {
                contentType(ContentType.Application.Json)
                setBody(estoque)
            }.body()

    suspend fun excluir(produtoId: Long) {
        httpClient.delete("$endPoint/$produtoId")
    }
}

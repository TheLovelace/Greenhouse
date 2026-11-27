package org.greenhouse.app.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.greenhouse.app.model.Fornecedor
import org.greenhouse.app.model.FornecedorRequest

class FornecedorRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/fornecedor"

    suspend fun listar(nome: String? = null): List<Fornecedor> =
        httpClient
            .get(endPoint) {
                parameter("nome", nome)
            }.body()

    suspend fun buscarPorId(id: Long): Fornecedor = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(fornecedor: FornecedorRequest): Fornecedor =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(fornecedor)
            }.body()

    suspend fun atualizar(
        id: Long,
        fornecedor: FornecedorRequest,
    ): Fornecedor =
        httpClient
            .put("$endPoint/$id") {
                contentType(ContentType.Application.Json)
                setBody(fornecedor)
            }.body()

    suspend fun excluir(id: Long) {
        httpClient.delete("$endPoint/$id")
    }
}

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
import org.greenhouse.app.model.Funcionario
import org.greenhouse.app.model.FuncionarioRequest

class FuncionarioRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/funcionario"

    suspend fun listar(): List<Funcionario> = httpClient.get(endPoint).body()

    suspend fun buscarPorId(id: Long): Funcionario = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(funcionario: FuncionarioRequest): Funcionario =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(funcionario)
            }.body()

    suspend fun atualizar(
        id: Long,
        funcionario: FuncionarioRequest,
    ): Funcionario =
        httpClient
            .put("$endPoint/$id") {
                contentType(ContentType.Application.Json)
                setBody(funcionario)
            }.body()

    suspend fun desativar(id: Long) {
        httpClient.delete("$endPoint/$id")
    }
}

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
import org.greenhouse.app.model.FormaPagamento
import org.greenhouse.app.model.FormaPagamentoRequest

class FormaPagamentoRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/forma-pagamento"

    suspend fun listar(): List<FormaPagamento> = httpClient.get(endPoint).body()

    suspend fun buscarPorId(id: Long): FormaPagamento = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(formaPagamento: FormaPagamentoRequest): FormaPagamento =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(formaPagamento)
            }.body()

    suspend fun atualizar(
        id: Long,
        formaPagamento: FormaPagamentoRequest,
    ): FormaPagamento =
        httpClient
            .put("$endPoint/$id") {
                contentType(ContentType.Application.Json)
                setBody(formaPagamento)
            }.body()

    suspend fun excluir(id: Long) {
        httpClient.delete("$endPoint/$id")
    }
}

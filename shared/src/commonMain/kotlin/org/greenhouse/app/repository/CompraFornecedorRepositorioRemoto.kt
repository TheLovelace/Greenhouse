package org.greenhouse.app.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.greenhouse.app.model.CompraFornecedor
import org.greenhouse.app.model.CompraFornecedorCreateRequest
import org.greenhouse.app.model.CompraFornecedorStatusRequest
import org.greenhouse.app.model.CompraFornecedorUpdateRequest
import org.greenhouse.ui.screens.compraFornecedor.model.StatusCompraCadastro

class CompraFornecedorRepositorioRemoto(
    private val httpClient: HttpClient,
) {
    private val endPoint = "$API_URL/compra-fornecedor"

    suspend fun listar(observacao: String? = null): List<CompraFornecedor> =
        httpClient
            .get(endPoint) {
                parameter("observacao", observacao)
            }.body()

    suspend fun buscarPorId(id: Long): CompraFornecedor = httpClient.get("$endPoint/$id").body()

    suspend fun salvar(compra: CompraFornecedorCreateRequest): CompraFornecedor =
        httpClient
            .post(endPoint) {
                contentType(ContentType.Application.Json)
                setBody(compra)
            }.body()

    suspend fun atualizar(
        id: Long,
        compra: CompraFornecedorUpdateRequest,
    ): CompraFornecedor =
        httpClient
            .put("$endPoint/$id") {
                contentType(ContentType.Application.Json)
                setBody(compra)
            }.body()

    suspend fun atualizarStatus(
        id: Long,
        status: StatusCompraCadastro,
    ): CompraFornecedor =
        httpClient
            .patch("$endPoint/$id/status") {
                contentType(ContentType.Application.Json)
                setBody(CompraFornecedorStatusRequest(status))
            }.body()

    suspend fun excluir(id: Long) {
        httpClient.delete("$endPoint/$id")
    }
}

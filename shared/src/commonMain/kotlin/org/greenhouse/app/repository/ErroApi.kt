package org.greenhouse.app.repository

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.Serializable

@Serializable
private data class ErroResposta(
    val error: String? = null,
)

suspend fun mensagemDeErro(
    erro: Throwable,
    mensagemPadrao: String,
): String {
    if (erro !is ResponseException) {
        return "Não foi possível conectar ao servidor. Verifique se o backend está rodando."
    }
    val mensagemBackend = runCatching { erro.response.body<ErroResposta>().error }.getOrNull()
    return mensagemBackend ?: "$mensagemPadrao (HTTP ${erro.response.status.value})"
}

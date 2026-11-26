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
        val detalhe = erro.message?.takeIf { it.isNotBlank() }
        return detalhe?.let { "Falha na comunicação com a API: $it" }
            ?: "Falha na comunicação com a API. Confira o endereço configurado e se ela está acessível."
    }
    val mensagemBackend = runCatching { erro.response.body<ErroResposta>().error }.getOrNull()
    return mensagemBackend ?: "$mensagemPadrao (HTTP ${erro.response.status.value})"
}

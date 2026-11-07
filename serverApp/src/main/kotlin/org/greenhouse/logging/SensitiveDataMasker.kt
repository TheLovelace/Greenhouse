package org.greenhouse.logging

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Component
import java.net.URLDecoder

@Component
class SensitiveDataMasker {
    private val mapper = ObjectMapper()

    fun isSensitive(field: String): Boolean {
        val normalized = field.lowercase()
        return SENSITIVE_TERMS.any { normalized.contains(it) }
    }

    fun maskJson(json: String): String =
        try {
            val node = mapper.readTree(json) ?: return ""
            mapper.writeValueAsString(mask(node))
        } catch (_: Exception) {
            "<corpo não é um JSON válido (${json.length} caracteres) - omitido>"
        }

    fun maskQueryString(query: String): String =
        query.split("&").joinToString("&") { pair ->
            val key = pair.substringBefore("=")
            val decodedKey = runCatching { URLDecoder.decode(key, Charsets.UTF_8) }.getOrDefault(key)
            if (pair.contains("=") && isSensitive(decodedKey)) "$key=$MASK" else pair
        }

    private fun mask(node: JsonNode): JsonNode {
        when (node) {
            is ObjectNode ->
                node.properties().forEach { (name, value) ->
                    if (isSensitive(name) && !value.isNull) node.put(name, MASK) else mask(value)
                }
            is ArrayNode -> node.forEach { mask(it) }
        }
        return node
    }

    companion object {
        const val MASK = "***"

        private val SENSITIVE_TERMS =
            listOf(
                "senha",
                "password",
                "token",
                "secret",
                "authorization",
                "cpf",
                "cnpj",
                "cartao",
                "card",
            )
    }
}

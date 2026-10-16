package org.greenhouse.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.greenhouse.adapter.NotFoundException

@RestController
class OpenApiController {

    @GetMapping("/openapi.yaml", produces = ["application/yaml"])
    fun openApiSpec(): ResponseEntity<Resource> =
        ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/yaml"))
            .body(ClassPathResource("openapi.yaml"))

    @GetMapping("/openapi/{fileName}.yml", produces = ["application/yaml"])
    fun openApiFragment(@PathVariable fileName: String): ResponseEntity<Resource> {
        val resourcePath =
            when (fileName) {
                "categoria",
                "cliente",
                "desconto",
                "produto",
                "estoque",
                "status",
                "fornecedor",
                "funcionario",
                "forma-pagamento",
                "pedido",
                "especie",
                "alerta",
                "auth" -> "openapi/$fileName.yml"
                else -> throw NotFoundException("Documento OpenAPI não encontrado")
            }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/yaml"))
            .body(ClassPathResource(resourcePath))
    }
}
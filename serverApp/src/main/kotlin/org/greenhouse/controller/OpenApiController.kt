package org.greenhouse.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class OpenApiController {

    @GetMapping("/openapi.yaml", produces = ["application/yaml"])
    fun openApiSpec(): ResponseEntity<Resource> =
        ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/yaml"))
            .body(ClassPathResource("openapi.yaml"))
}
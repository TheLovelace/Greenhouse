package org.greenhouse.controller

import org.greenhouse.adapter.DatabaseConfig
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatusController(
    private val databaseConfig: DatabaseConfig,
) {

    @GetMapping("/status")
    fun status(): Map<String, String> {
        databaseConfig.session { Unit }
        return mapOf("status" to "ok", "database" to "ok")
    }
}
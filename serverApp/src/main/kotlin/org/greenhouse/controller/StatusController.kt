package org.greenhouse.controller

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatusController(
    private val jdbcTemplate: JdbcTemplate,
) {

    @GetMapping("/status")
    @Transactional(readOnly = true)
    fun status(): Map<String, String> {
        jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
        return mapOf("status" to "ok", "database" to "ok")
    }
}
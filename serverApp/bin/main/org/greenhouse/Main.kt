package org.greenhouse

import org.greenhouse.adapter.DatabaseConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class GreenhouseApplication {

    @Bean
    fun databaseConfig(): DatabaseConfig =
        DatabaseConfig(
            url = System.getenv("DATABASE_URL"),
        )
}

fun main(args: Array<String>) {
    runApplication<GreenhouseApplication>(*args)
}
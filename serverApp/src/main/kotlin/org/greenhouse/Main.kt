package org.greenhouse

import org.greenhouse.adapter.DatabaseConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@SpringBootApplication
class GreenhouseApplication {

    @Bean
    fun databaseConfig(dataSource: DataSource): DatabaseConfig =
        DatabaseConfig(dataSource)
}

fun main(args: Array<String>) {
    runApplication<GreenhouseApplication>(*args)
}
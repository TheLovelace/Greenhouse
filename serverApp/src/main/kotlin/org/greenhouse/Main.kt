package org.greenhouse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GreenhouseApplication

fun main(args: Array<String>) {
    runApplication<GreenhouseApplication>(*args)
}
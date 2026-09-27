package org.greenhouse.adapter

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConfig(url: String) {
    private val database = Database.connect(url)

    fun <T> session(block: () -> T): T =
        transaction(database) {
            exec("SELECT 1")
            block()
        }
}
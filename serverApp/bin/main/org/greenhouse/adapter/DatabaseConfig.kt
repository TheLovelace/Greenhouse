package org.greenhouse.adapter

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

class DatabaseConfig(url: String) {
    private val database =
        try {
            Database.connect(url)
        } catch (e: Exception) {
            throw mapException(e)
        }

    fun <T> session(block: () -> T): T =
        try {
            transaction(database) {
                exec("SELECT 1")
                block()
            }
        } catch (e: SQLException) {
            throw mapException(e)
        }

    private fun mapException(e: Exception): AppException =
        when {
            e !is SQLException -> DatabaseConnectionException(cause = e)
            e.sqlState == "28P01" -> AuthenticationException(cause = e)
            e.sqlState?.startsWith("08") == true -> DatabaseConnectionException(cause = e)
            else -> DatabaseConnectionException(cause = e)
        }
}
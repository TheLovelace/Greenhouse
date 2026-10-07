package org.greenhouse.integration.adapter

import org.greenhouse.integration.fixture.SpringTestContext
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Database_Config {
    @Test
    fun `deve estabelecer conexao com o banco`() {
        val result = SpringTestContext.bean<DataSource>().connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT 'conexao realizada'").use { resultSet ->
                    check(resultSet.next())
                    resultSet.getString(1)
                }
            }
        }
        assertEquals("conexao realizada", result)
    }
}

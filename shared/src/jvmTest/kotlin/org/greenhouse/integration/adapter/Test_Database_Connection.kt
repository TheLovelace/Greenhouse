package org.greenhouse.integration.adapter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.AuthenticationException
import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.adapter.DatabaseConnectionException

class Test_Database_Config {

    /**
     * Testa a conexão quando a URL não é fornecida.
     */
    @Test
    fun `deve falhar quando a URL nao for fornecida`() {
        assertFailsWith<DatabaseConnectionException> {
            DatabaseConfig("")
                .session { Unit }
        }
    }

    /**
     * Testa a conexão quando as credenciais fornecidas na URL são inválidas.
     *
     */
    @Test
    fun `deve falhar quando houver erro de autenticacao`() {
        val realUrl = System.getenv("DATABASE_URL")
        val urlComCredenciaisInvalidas =
            realUrl.replace(Regex("user=[^&]*"), "user=invalid")
                .replace(Regex("password=[^&]*"), "password=invalid")

        assertFailsWith<AuthenticationException> {
            DatabaseConfig(urlComCredenciaisInvalidas)
                .session { Unit }
        }
    }

    /**
     * Testa uma conexão válida executando uma consulta básica.
     */
    @Test
    fun `deve estabelecer conexao com o banco`() {
        val url = System.getenv("DATABASE_URL")

        val database = DatabaseConfig(url)

        val result = database.session {
            "conexao realizada"
        }

        assertEquals("conexao realizada", result)
    }
}
package org.greenhouse.integration.adapter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import org.greenhouse.adapter.DatabaseConfig

class Test_Database_Config {

    /**
     * Testa a conexão quando a URL não é fornecida.
     *
     * TODO: substituir pela exceção específica de conexão com o banco.
     */
    @Test
    fun `deve falhar quando a URL nao for fornecida`() {
        assertFails {
            DatabaseConfig("")
                .session { Unit }
        }
    }

    /**
     * Testa a conexão quando as credenciais fornecidas na URL são inválidas.
     *
     * TODO: substituir pela exceção específica de conexão com o banco.
     */
    @Test
    fun `deve falhar quando houver erro de autenticacao`() {
        val url = "jdbc:postgresql://localhost:5432/neondb?user=invalid&password=invalid"

        assertFails {
            DatabaseConfig(url)
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
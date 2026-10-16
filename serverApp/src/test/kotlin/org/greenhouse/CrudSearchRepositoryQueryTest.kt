package org.greenhouse

import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.repository.FuncionarioRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.repository.Query

class CrudSearchRepositoryQueryTest {
    @Test
    fun `funcionario search query matches a substring of the name`() {
        val query = FuncionarioRepository::class.java
            .getMethod("buscarPorNome", String::class.java)
            .getAnnotation(Query::class.java)
            .value

        assertTrue(query.contains("f.nome LIKE CONCAT('%', :nome, '%')"))
    }

    @Test
    fun `forma de pagamento search query matches a substring of the name`() {
        val query = FormaPagamentoRepository::class.java
            .getMethod("buscarPorNome", String::class.java)
            .getAnnotation(Query::class.java)
            .value

        assertTrue(query.contains("f.nome LIKE CONCAT('%', :nome, '%')"))
    }
}

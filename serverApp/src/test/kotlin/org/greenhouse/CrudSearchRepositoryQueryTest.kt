package org.greenhouse

import org.greenhouse.repository.CompraFornecedorRepository
import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.repository.FornecedorRepository
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

    @Test
    fun `fornecedor search query matches a substring of the name`() {
        val query = FornecedorRepository::class.java
            .getMethod("buscarPorNome", String::class.java)
            .getAnnotation(Query::class.java)
            .value

        assertTrue(query.contains("LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))"))
    }

    @Test
    fun `compra fornecedor search query matches a substring of the observation`() {
        val query = CompraFornecedorRepository::class.java
            .getMethod("buscarPorObservacao", String::class.java)
            .getAnnotation(Query::class.java)
            .value

        assertTrue(query.contains("LOWER(c.observacao) LIKE LOWER(CONCAT('%', :observacao, '%'))"))
    }
}

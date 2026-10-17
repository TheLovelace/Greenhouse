package org.greenhouse

import org.assertj.core.api.Assertions.assertThat
import org.greenhouse.model.CompraFornecedor
import org.greenhouse.model.FormaPagamento
import org.greenhouse.model.Fornecedor
import org.greenhouse.model.Funcionario
import org.greenhouse.repository.CompraFornecedorRepository
import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.repository.FornecedorRepository
import org.greenhouse.repository.FuncionarioRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
    ],
)
class CrudSearchRepositoryIntegrationTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val funcionarioRepository: FuncionarioRepository,
    private val formaPagamentoRepository: FormaPagamentoRepository,
    private val fornecedorRepository: FornecedorRepository,
    private val compraFornecedorRepository: CompraFornecedorRepository,
) {
    @Test
    fun `funcionario query returns names containing the search term`() {
        entityManager.persistAndFlush(Funcionario(nome = "Luna Ferreira", cpf = "12345678901"))
        entityManager.persistAndFlush(Funcionario(nome = "Carlos Silva", cpf = "12345678902"))

        val resultados = funcionarioRepository.buscarPorNome("una")

        assertThat(resultados).extracting<String> { it.nome }.containsExactly("Luna Ferreira")
    }

    @Test
    fun `forma de pagamento query returns names containing the search term`() {
        entityManager.persistAndFlush(FormaPagamento(nome = "Cartão de crédito"))
        entityManager.persistAndFlush(FormaPagamento(nome = "Pix"))

        val resultados = formaPagamentoRepository.buscarPorNome("artão")

        assertThat(resultados).extracting<String> { it.nome }.containsExactly("Cartão de crédito")
    }

    @Test
    fun `fornecedor query returns names containing the search term`() {
        entityManager.persistAndFlush(Fornecedor(nome = "Flores do Campo"))
        entityManager.persistAndFlush(Fornecedor(nome = "Vasos e Cia"))

        val resultados = fornecedorRepository.buscarPorNome("campo")

        assertThat(resultados).extracting<String> { it.nome }.containsExactly("Flores do Campo")
    }

    @Test
    fun `compra fornecedor query returns observations containing the search term`() {
        val funcionario = entityManager.persistAndFlush(Funcionario(nome = "Luna", cpf = "12345678901"))
        val fornecedor = entityManager.persistAndFlush(Fornecedor(nome = "Flores do Campo"))
        val formaPagamento = entityManager.persistAndFlush(FormaPagamento(nome = "Pix"))

        entityManager.persistAndFlush(
            CompraFornecedor(
                funcionarioId = funcionario.id!!,
                fornecedorId = fornecedor.id!!,
                formaPagamentoId = formaPagamento.id!!,
                observacao = "Reposição semanal de vasos",
            ),
        )
        entityManager.persistAndFlush(
            CompraFornecedor(
                funcionarioId = funcionario.id!!,
                fornecedorId = fornecedor.id!!,
                formaPagamentoId = formaPagamento.id!!,
                observacao = "Compra de fertilizantes",
            ),
        )

        val resultados = compraFornecedorRepository.buscarPorObservacao("vasos")

        assertThat(resultados).extracting<String> { it.observacao }
            .containsExactly("Reposição semanal de vasos")
    }
}

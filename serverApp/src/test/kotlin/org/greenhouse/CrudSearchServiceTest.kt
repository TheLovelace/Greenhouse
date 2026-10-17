package org.greenhouse

import org.greenhouse.mapper.CompraFornecedorMapper
import org.greenhouse.mapper.FormaPagamentoMapper
import org.greenhouse.mapper.FornecedorMapper
import org.greenhouse.mapper.FuncionarioMapper
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.model.CompraFornecedor
import org.greenhouse.model.FormaPagamento
import org.greenhouse.model.Fornecedor
import org.greenhouse.model.Funcionario
import org.greenhouse.repository.CompraFornecedorRepository
import org.greenhouse.repository.CompraItemRepository
import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.repository.FornecedorRepository
import org.greenhouse.repository.FuncionarioRepository
import org.greenhouse.repository.ProdutoRepository
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.service.CompraFornecedorService
import org.greenhouse.service.EstoqueService
import org.greenhouse.service.FormaPagamentoService
import org.greenhouse.service.FornecedorService
import org.greenhouse.service.FuncionarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class CrudSearchServiceTest {
    @Test
    fun `funcionario listing uses unfiltered query when name is absent`() {
        val repository = Mockito.mock(FuncionarioRepository::class.java)
        Mockito.`when`(repository.listar()).thenReturn(
            listOf(Funcionario(id = 1, nome = "Luna", cpf = "12345678901")),
        )
        val service = FuncionarioService(
            repository,
            Mockito.mock(UsuarioRepository::class.java),
            FuncionarioMapper(),
            UsuarioMapper(),
        )

        val result = service.listar()

        assertEquals(listOf("Luna"), result.map { it.nome })
        Mockito.verify(repository).listar()
    }

    @Test
    fun `funcionario listing uses partial-name query when name is supplied`() {
        val repository = Mockito.mock(FuncionarioRepository::class.java)
        Mockito.`when`(repository.buscarPorNome("una")).thenReturn(
            listOf(Funcionario(id = 1, nome = "Luna", cpf = "12345678901")),
        )
        val service = FuncionarioService(
            repository,
            Mockito.mock(UsuarioRepository::class.java),
            FuncionarioMapper(),
            UsuarioMapper(),
        )

        val result = service.listar("una")

        assertEquals(listOf("Luna"), result.map { it.nome })
        Mockito.verify(repository).buscarPorNome("una")
    }

    @Test
    fun `forma de pagamento listing uses unfiltered query when name is absent`() {
        val repository = Mockito.mock(FormaPagamentoRepository::class.java)
        Mockito.`when`(repository.listar()).thenReturn(
            listOf(FormaPagamento(id = 1, nome = "Cartão")),
        )
        val service = FormaPagamentoService(repository, FormaPagamentoMapper())

        val result = service.listar()

        assertEquals(listOf("Cartão"), result.map { it.nome })
        Mockito.verify(repository).listar()
    }

    @Test
    fun `forma de pagamento listing uses partial-name query when name is supplied`() {
        val repository = Mockito.mock(FormaPagamentoRepository::class.java)
        Mockito.`when`(repository.buscarPorNome("art")).thenReturn(
            listOf(FormaPagamento(id = 1, nome = "Cartão")),
        )
        val service = FormaPagamentoService(repository, FormaPagamentoMapper())

        val result = service.listar("art")

        assertEquals(listOf("Cartão"), result.map { it.nome })
        Mockito.verify(repository).buscarPorNome("art")
    }

    @Test
    fun `fornecedor listing uses unfiltered query when name is absent`() {
        val repository = Mockito.mock(FornecedorRepository::class.java)
        Mockito.`when`(repository.listar()).thenReturn(
            listOf(Fornecedor(id = 1, nome = "Flores do Campo")),
        )
        val service = FornecedorService(repository, FornecedorMapper())

        val result = service.listar()

        assertEquals(listOf("Flores do Campo"), result.map { it.nome })
        Mockito.verify(repository).listar()
    }

    @Test
    fun `fornecedor listing uses partial-name query when name is supplied`() {
        val repository = Mockito.mock(FornecedorRepository::class.java)
        Mockito.`when`(repository.buscarPorNome("campo")).thenReturn(
            listOf(Fornecedor(id = 1, nome = "Flores do Campo")),
        )
        val service = FornecedorService(repository, FornecedorMapper())

        val result = service.listar("campo")

        assertEquals(listOf("Flores do Campo"), result.map { it.nome })
        Mockito.verify(repository).buscarPorNome("campo")
    }

    @Test
    fun `compra listing uses unfiltered query when observation is absent`() {
        val compraRepository = Mockito.mock(CompraFornecedorRepository::class.java)
        val compraItemRepository = Mockito.mock(CompraItemRepository::class.java)
        Mockito.`when`(compraRepository.listar()).thenReturn(
            listOf(CompraFornecedor(id = 1, observacao = "Reposição")),
        )
        val service = novaCompraService(compraRepository, compraItemRepository)

        val result = service.listar()

        assertEquals(listOf(1L), result.map { it.id })
        Mockito.verify(compraRepository).listar()
    }

    @Test
    fun `compra listing uses partial-observation query when observation is supplied`() {
        val compraRepository = Mockito.mock(CompraFornecedorRepository::class.java)
        val compraItemRepository = Mockito.mock(CompraItemRepository::class.java)
        Mockito.`when`(compraRepository.buscarPorObservacao("vasos")).thenReturn(
            listOf(CompraFornecedor(id = 1, observacao = "Reposição de vasos")),
        )
        val service = novaCompraService(compraRepository, compraItemRepository)

        val result = service.listar("vasos")

        assertEquals(listOf(1L), result.map { it.id })
        Mockito.verify(compraRepository).buscarPorObservacao("vasos")
    }

    private fun novaCompraService(
        compraRepository: CompraFornecedorRepository,
        compraItemRepository: CompraItemRepository,
    ) = CompraFornecedorService(
        compraRepository,
        compraItemRepository,
        Mockito.mock(FuncionarioRepository::class.java),
        Mockito.mock(FornecedorRepository::class.java),
        Mockito.mock(FormaPagamentoRepository::class.java),
        Mockito.mock(ProdutoRepository::class.java),
        Mockito.mock(EstoqueService::class.java),
        CompraFornecedorMapper(),
    )
}

package org.greenhouse

import org.greenhouse.mapper.FormaPagamentoMapper
import org.greenhouse.mapper.FuncionarioMapper
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.model.FormaPagamento
import org.greenhouse.model.Funcionario
import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.repository.FuncionarioRepository
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.service.FormaPagamentoService
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
}

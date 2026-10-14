package org.greenhouse.integration.security

import org.greenhouse.repository.CategoriaRepository
import org.greenhouse.repository.ClienteRepository
import org.greenhouse.repository.DescontoRepository
import org.greenhouse.repository.EstoqueRepository
import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.repository.FornecedorRepository
import org.greenhouse.repository.FuncionarioRepository
import org.greenhouse.repository.ProdutoRepository
import org.greenhouse.repository.UsuarioRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean

abstract class SpringBootWebTest {
    @MockitoBean
    private lateinit var categoriaRepository: CategoriaRepository

    @MockitoBean
    private lateinit var clienteRepository: ClienteRepository

    @MockitoBean
    private lateinit var descontoRepository: DescontoRepository

    @MockitoBean
    private lateinit var estoqueRepository: EstoqueRepository

    @MockitoBean
    private lateinit var formaPagamentoRepository: FormaPagamentoRepository

    @MockitoBean
    private lateinit var fornecedorRepository: FornecedorRepository

    @MockitoBean
    private lateinit var funcionarioRepository: FuncionarioRepository

    @MockitoBean
    private lateinit var produtoRepository: ProdutoRepository

    @MockitoBean
    private lateinit var usuarioRepository: UsuarioRepository

    @MockitoBean
    private lateinit var jdbcTemplate: JdbcTemplate
}

package org.greenhouse.unit.service

import org.greenhouse.adapter.AuthenticationException
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.ServerException
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.model.Usuario
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.security.JwtService
import org.greenhouse.service.AuthService
import org.greenhouse.service.LoginRequest
import org.greenhouse.service.PrimeiroAcessoRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Testes de unidade do service AuthService (login e primeiro acesso).
 *
 * Recursos de teste usados:
 * - MOCK [usuarioRepository]: substitui o banco.
 * - MOCK [authenticationManager]: substitui o Spring Security, simulando sucesso ou falha.
 * - MOCK [jwtService]: devolve um token fixo, sem assinar JWT de verdade.
 * - MOCK [passwordEncoder]: devolve um hash fixo, sem rodar o BCrypt.
 * - SPY [usuarioMapper]: mapper real, injetado pelo @InjectMocks.
 *
 * Os fluxos com o Spring Security e o BCrypt reais ficam em
 * shared/src/jvmTest/.../unit/service/AuthServiceTest.kt.
 */
@ExtendWith(MockitoExtension::class)
class AuthServiceTest {
    @Mock lateinit var usuarioRepository: UsuarioRepository

    @Mock lateinit var jwtService: JwtService

    @Spy var usuarioMapper = UsuarioMapper()

    @Mock lateinit var authenticationManager: AuthenticationManager

    @Mock lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks lateinit var service: AuthService

    private val login = "ana.lima"
    private val senha = "senha-segura"
    private val idDoUsuario = 7L

    private fun usuario(ativo: Boolean = true, primeiroAcessoFeito: Boolean = true) =
            Usuario(
                    id = idDoUsuario,
                    funcionarioId = 4L,
                    login = login,
                    senhaHash = "hash-atual",
                    perfil = "FUNCIONARIO",
                    ativo = ativo,
                    primeiroAcessoFeito = primeiroAcessoFeito,
            )

    private fun usuarioCadastrado(usuario: Usuario?) {
        `when`(usuarioRepository.buscarPorLogin(login)).thenReturn(usuario)
    }

    private fun usuarioDoPrimeiroAcesso(usuario: Usuario?) {
        `when`(usuarioRepository.buscarPorCpfEEmail("98765432100", "ana@greenhouse.com"))
                .thenReturn(usuario)
    }

    private fun primeiroAcesso(novaSenha: String = senha) =
            PrimeiroAcessoRequest(cpf = "98765432100", email = "ana@greenhouse.com", novaSenha = novaSenha)

    private fun autenticacaoFalhaCom(erro: RuntimeException) {
        `when`(authenticationManager.authenticate(any(Authentication::class.java))).thenThrow(erro)
    }

    // CT01 - Fluxo principal: login válido devolve token e perfil
    @Test
    fun `deve fazer login e devolver token com o perfil do usuario`() {
        usuarioCadastrado(usuario())
        `when`(jwtService.gerarToken(login, "FUNCIONARIO")).thenReturn("token-jwt")

        val resultado = service.login(LoginRequest(login, senha))

        assertAll(
                "resposta do login",
                { assertEquals("token-jwt", resultado.token, "devolve o token gerado") },
                { assertEquals("FUNCIONARIO", resultado.perfil, "devolve o perfil do usuário") },
        )
    }

    // CT02 - Fluxo de exceção: login inexistente
    @Test
    fun `nao deve fazer login com usuario inexistente`() {
        usuarioCadastrado(null)

        val erro = assertThrows(AuthenticationException::class.java) { service.login(LoginRequest(login, senha)) }

        assertEquals("Credenciais inválidas", erro.message)
        verifyNoInteractions(authenticationManager)
    }

    // CT03 - Fluxo de exceção: usuário inativo
    @Test
    fun `nao deve fazer login com usuario inativo`() {
        usuarioCadastrado(usuario(ativo = false))

        val erro = assertThrows(AuthenticationException::class.java) { service.login(LoginRequest(login, senha)) }

        assertEquals("Usuário inativo", erro.message)
        verifyNoInteractions(authenticationManager)
    }

    // CT04 - Fluxo de exceção: senha incorreta rejeitada pelo Spring Security
    @Test
    fun `nao deve fazer login com senha incorreta`() {
        usuarioCadastrado(usuario())
        autenticacaoFalhaCom(BadCredentialsException("senha errada"))

        val erro = assertThrows(AuthenticationException::class.java) { service.login(LoginRequest(login, senha)) }

        assertEquals("Credenciais inválidas", erro.message)
    }

    // CT05 - Fluxo de exceção: falha interna do Spring Security vira erro do servidor
    @Test
    fun `deve devolver erro do servidor quando a autenticacao falha internamente`() {
        usuarioCadastrado(usuario())
        autenticacaoFalhaCom(InternalAuthenticationServiceException("banco fora do ar"))

        val erro = assertThrows(ServerException::class.java) { service.login(LoginRequest(login, senha)) }

        assertEquals("Não foi possível autenticar o usuário", erro.message)
    }

    // CT06 - Fluxo principal: primeiro acesso grava a nova senha criptografada
    @Test
    fun `deve concluir o primeiro acesso com a nova senha criptografada`() {
        usuarioDoPrimeiroAcesso(usuario(primeiroAcessoFeito = false))
        `when`(passwordEncoder.encode(senha)).thenReturn("hash-novo")
        `when`(usuarioRepository.concluirPrimeiroAcesso(idDoUsuario, "hash-novo")).thenReturn(true)

        assertDoesNotThrow("primeiro acesso válido é concluído") { service.primeiroAcesso(primeiroAcesso()) }

        verify(usuarioRepository).concluirPrimeiroAcesso(idDoUsuario, "hash-novo")
    }

    // CT07 - Análise de valor limite: senha com 5 caracteres fica abaixo do mínimo de 6
    @Test
    fun `nao deve aceitar nova senha com menos de 6 caracteres`() {
        val erro =
                assertThrows(BadRequestException::class.java) {
                    service.primeiroAcesso(primeiroAcesso(novaSenha = "12345"))
                }

        assertEquals("A nova senha deve ter no mínimo 6 caracteres", erro.message)
        verifyNoInteractions(usuarioRepository)
    }

    // CT08 - Fluxo de exceção: CPF e e-mail não correspondem a nenhum usuário
    @Test
    fun `nao deve concluir primeiro acesso de usuario inexistente`() {
        usuarioDoPrimeiroAcesso(null)

        val erro = assertThrows(NotFoundException::class.java) { service.primeiroAcesso(primeiroAcesso()) }

        assertEquals("Dados de validação incorretos ou usuário não encontrado", erro.message)
    }

    // CT09 - Fluxo de exceção: usuário inativo não conclui o primeiro acesso
    @Test
    fun `nao deve concluir primeiro acesso de usuario inativo`() {
        usuarioDoPrimeiroAcesso(usuario(ativo = false, primeiroAcessoFeito = false))

        val erro = assertThrows(BadRequestException::class.java) { service.primeiroAcesso(primeiroAcesso()) }

        assertEquals("Este usuário está inativo", erro.message)
        verify(passwordEncoder, never()).encode(any())
    }

    // CT10 - Fluxo de exceção: o banco não conclui o primeiro acesso
    @Test
    fun `deve falhar quando o banco nao conclui o primeiro acesso`() {
        usuarioDoPrimeiroAcesso(usuario(primeiroAcessoFeito = false))
        `when`(passwordEncoder.encode(senha)).thenReturn("hash-novo")
        `when`(usuarioRepository.concluirPrimeiroAcesso(idDoUsuario, "hash-novo")).thenReturn(false)

        val erro = assertThrows(BadRequestException::class.java) { service.primeiroAcesso(primeiroAcesso()) }

        assertEquals("Não foi possível redefinir a senha", erro.message)
    }
}

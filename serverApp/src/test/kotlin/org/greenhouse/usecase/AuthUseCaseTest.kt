package org.greenhouse.usecase

import org.greenhouse.adapter.AuthenticationException
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.entity.Usuario
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.security.JwtService
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthUseCaseTest {

    private val usuarioRepository = Mockito.mock(UsuarioRepository::class.java)
    private val jwtService = JwtService("test-key-which-is-longer-than-thirty-two-characters", 60_000)
    private val useCase = AuthUseCase(usuarioRepository, jwtService)
    private val encoder = BCryptPasswordEncoder()

    @Test
    fun `login legado da conta existente e migra credencial depois do sucesso`() {
        val senhaLegada = "senha-admin-legada"
        Mockito.doReturn(usuario(senhaHash = senhaLegada))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")
        var storedHash: String? = null
        Mockito.doAnswer { invocation ->
            storedHash = invocation.getArgument(1)
            true
        }.`when`(usuarioRepository)
            .atualizarSenha(eq(7L), Mockito.anyString() ?: "")

        val result = useCase.login(LoginRequest("admin", senhaLegada))

        assertTrue(jwtService.validarToken(result.token))
        Mockito.verify(usuarioRepository).atualizarSenha(eq(7L), Mockito.anyString() ?: "")
        assertTrue(encoder.matches(senhaLegada, storedHash ?: ""))
    }

    @Test
    fun `credencial legada longa continua funcionando sem truncar nem atualizar`() {
        val senhaLegada = "a".repeat(73)
        Mockito.doReturn(usuario(senhaHash = senhaLegada))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")

        useCase.login(LoginRequest("admin", senhaLegada))

        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `login BCrypt valida credencial sem regravar hash`() {
        val senha = "senha-segura"
        Mockito.doReturn(usuario(senhaHash = encoder.encode(senha)))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")

        useCase.login(LoginRequest("admin", senha))

        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `credencial legada incorreta nao modifica armazenamento`() {
        Mockito.doReturn(usuario(senhaHash = "senha-correta"))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")

        assertThrows(AuthenticationException::class.java) {
            useCase.login(LoginRequest("admin", "senha-incorreta"))
        }
        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `login bloqueia conta pendente mesmo com placeholder correto`() {
        val pendente = usuario(senhaHash = "!FIRST_ACCESS_REQUIRED!", primeiroAcessoFeito = false)
        Mockito.doReturn(pendente)
            .`when`(usuarioRepository)
            .buscarPorLogin("novo")

        assertThrows(AuthenticationException::class.java) {
            useCase.login(LoginRequest("novo", "!FIRST_ACCESS_REQUIRED!"))
        }
        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `primeiro acesso armazena BCrypt e conclui flag atomicamente`() {
        Mockito.doReturn(usuario(primeiroAcessoFeito = false))
            .`when`(usuarioRepository)
            .buscarPorCpfEEmail("12345678901", "user@example.com")
        var storedHash: String? = null
        Mockito.doAnswer { invocation ->
            storedHash = invocation.getArgument(1)
            true
        }.`when`(usuarioRepository)
            .concluirPrimeiroAcesso(eq(7L), Mockito.anyString() ?: "")

        useCase.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "user@example.com", "nova-senha"))

        Mockito.verify(usuarioRepository).concluirPrimeiroAcesso(eq(7L), Mockito.anyString() ?: "")
        assertTrue(encoder.matches("nova-senha", storedHash ?: ""))
    }

    @Test
    fun `primeiro acesso rejeita senha acima de 72 bytes UTF-8`() {
        val senhaMultibyte = "á".repeat(37)

        assertThrows(BadRequestException::class.java) {
            useCase.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "user@example.com", senhaMultibyte))
        }
        Mockito.verifyNoInteractions(usuarioRepository)
    }

    @Test
    fun `primeiro acesso aceita senha de exatamente 72 bytes UTF-8`() {
        val senhaMultibyte = "á".repeat(36)
        Mockito.doReturn(usuario(primeiroAcessoFeito = false))
            .`when`(usuarioRepository)
            .buscarPorCpfEEmail("12345678901", "user@example.com")
        var storedHash: String? = null
        Mockito.doAnswer { invocation ->
            storedHash = invocation.getArgument(1)
            true
        }.`when`(usuarioRepository)
            .concluirPrimeiroAcesso(eq(7L), Mockito.anyString() ?: "")

        useCase.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "user@example.com", senhaMultibyte))

        Mockito.verify(usuarioRepository).concluirPrimeiroAcesso(eq(7L), Mockito.anyString() ?: "")
        assertTrue(encoder.matches(senhaMultibyte, storedHash ?: ""))
    }

    @Test
    fun `primeiro acesso rejeita reutilizacao apos conclusao`() {
        Mockito.doReturn(usuario(primeiroAcessoFeito = true))
            .`when`(usuarioRepository)
            .buscarPorCpfEEmail("12345678901", "user@example.com")

        assertThrows(BadRequestException::class.java) {
            useCase.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "user@example.com", "nova-senha"))
        }
        Mockito.verify(usuarioRepository, Mockito.never())
            .concluirPrimeiroAcesso(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `login admin legado mantem perfil ADMIN`() {
        Mockito.doReturn(usuario(senhaHash = "senha-admin"))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")
        Mockito.doAnswer { true }
            .`when`(usuarioRepository)
            .atualizarSenha(eq(7L), Mockito.anyString() ?: "")

        val token = useCase.login(LoginRequest("admin", "senha-admin")).token

        assertTrue(jwtService.validarToken(token))
        assertTrue(jwtService.extrairPerfil(token) == "ADMIN")
    }

    @Test
    fun `primeiro acesso retorna nao encontrado para dados sem conta`() {
        Mockito.doReturn(null)
            .`when`(usuarioRepository)
            .buscarPorCpfEEmail("12345678901", "missing@example.com")

        assertThrows(NotFoundException::class.java) {
            useCase.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "missing@example.com", "nova-senha"))
        }
    }

    private fun usuario(
        senhaHash: String = encoder.encode("senha-atual"),
        primeiroAcessoFeito: Boolean = true,
    ) = Usuario(
        id = 7,
        funcionarioId = 3,
        login = "admin",
        senhaHash = senhaHash,
        perfil = "ADMIN",
        primeiroAcessoFeito = primeiroAcessoFeito,
    )
}

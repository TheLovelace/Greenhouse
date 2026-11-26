package org.greenhouse.unit.service

import org.greenhouse.adapter.AuthenticationException
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.dto.UsuarioDTO as Usuario
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.model.RefreshToken
import org.greenhouse.repository.RefreshTokenRepository
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.security.JwtService
import org.greenhouse.security.LegacyPasswordAuthenticationProvider
import org.greenhouse.service.AuthService
import org.greenhouse.service.LoginRequest
import org.greenhouse.service.PrimeiroAcessoRequest
import org.greenhouse.service.RefreshTokenRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit

class AuthServiceTest {
    private val usuarioRepository = Mockito.mock(UsuarioRepository::class.java)
    private val refreshTokenRepository = Mockito.mock(RefreshTokenRepository::class.java)
    private val jwtService = JwtService("test-key-which-is-longer-than-thirty-two-characters", 60_000)
    private val usuarioMapper = UsuarioMapper()
    private val encoder = BCryptPasswordEncoder()
    private val userDetailsService =
        UserDetailsService { login ->
            val usuario = usuarioRepository.buscarPorLogin(login)
                ?: throw org.springframework.security.core.userdetails.UsernameNotFoundException("Credenciais inválidas")
            User.builder()
                .username(usuario.login)
                .password(usuario.senhaHash)
                .authorities(SimpleGrantedAuthority("ROLE_${usuario.perfil}"))
                .disabled(!usuario.ativo || !usuario.primeiroAcessoFeito)
                .build()
        }
    private val provider = DaoAuthenticationProvider(userDetailsService).apply {
        setPasswordEncoder(encoder)
    }
    private val legacyProvider = LegacyPasswordAuthenticationProvider(usuarioRepository, encoder)
    private val authenticationManager: AuthenticationManager = ProviderManager(listOf(legacyProvider, provider))
    private val service =
        AuthService(
            usuarioRepository,
            refreshTokenRepository,
            jwtService,
            usuarioMapper,
            authenticationManager,
            encoder,
        )

    @Test
    fun `login lembrado emite refresh token e persiste somente o hash`() {
        val password = "senha-atual"
        Mockito.doReturn(usuarioMapper.toModel(usuario(senhaHash = encoder.encode(password))))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")

        val result = service.login(LoginRequest("admin", password, lembrarDispositivo = true))
        val savedTokenCaptor = ArgumentCaptor.forClass(RefreshToken::class.java)
        Mockito.verify(refreshTokenRepository).save(savedTokenCaptor.capture())
        val savedToken = savedTokenCaptor.value

        assertNotNull(result.refreshToken)
        assertTrue(savedToken.tokenHash != result.refreshToken)
        assertTrue(savedToken.tokenHash.length == 64)
        assertTrue(savedToken.expiraEm.isAfter(Instant.now().plus(29, ChronoUnit.DAYS)))
    }

    @Test
    fun `refresh rotaciona token preservando o vencimento absoluto`() {
        val expiration = Instant.now().plus(20, ChronoUnit.DAYS)
        val persistedToken =
            RefreshToken(
                usuarioId = 7,
                tokenHash = "hash-antigo",
                expiraEm = expiration,
            )
        Mockito.doReturn(persistedToken)
            .`when`(refreshTokenRepository)
            .buscarPorHashParaAtualizacao(Mockito.anyString() ?: "")
        Mockito.doReturn(java.util.Optional.of(usuarioMapper.toModel(usuario())))
            .`when`(usuarioRepository)
            .findById(7L)

        val result = service.refresh(RefreshTokenRequest("refresh-antigo"))
        val savedTokenCaptor = ArgumentCaptor.forClass(RefreshToken::class.java)
        Mockito.verify(refreshTokenRepository, Mockito.times(2)).save(savedTokenCaptor.capture())
        val rotatedToken = savedTokenCaptor.allValues[1]

        assertNotNull(result.refreshToken)
        assertNotNull(persistedToken.revogadoEm)
        assertTrue(rotatedToken.tokenHash != result.refreshToken)
        assertTrue(rotatedToken.expiraEm == expiration)
    }

    @Test
    fun `login legado da conta existente e migra credencial depois do sucesso`() {
        val senhaLegada = "senha-admin-legada"
        Mockito.doReturn(usuarioMapper.toModel(usuario(senhaHash = senhaLegada)))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")
        var storedHash: String? = null
        Mockito.doAnswer { invocation ->
            storedHash = invocation.getArgument(1)
            true
        }.`when`(usuarioRepository)
            .atualizarSenha(eq(7L), Mockito.anyString() ?: "")

        val result = service.login(LoginRequest("admin", senhaLegada))

        assertTrue(jwtService.validarToken(result.token))
        Mockito.verify(usuarioRepository).atualizarSenha(eq(7L), Mockito.anyString() ?: "")
        assertTrue(encoder.matches(senhaLegada, storedHash ?: ""))
    }

    @Test
    fun `credencial legada longa continua funcionando sem truncar nem atualizar`() {
        val senhaLegada = "a".repeat(73)
        Mockito.doReturn(usuarioMapper.toModel(usuario(senhaHash = senhaLegada)))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")

        service.login(LoginRequest("admin", senhaLegada))

        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `login BCrypt valida credencial sem regravar hash`() {
        val senha = "senha-segura"
        Mockito.doReturn(usuarioMapper.toModel(usuario(senhaHash = encoder.encode(senha))))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")

        service.login(LoginRequest("admin", senha))

        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `login BCrypt rejeita senha incorreta pelo AuthenticationManager`() {
        Mockito.doReturn(usuarioMapper.toModel(usuario(senhaHash = encoder.encode("senha-correta"))))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")

        assertThrows(AuthenticationException::class.java) {
            service.login(LoginRequest("admin", "senha-incorreta"))
        }
        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `credencial legada incorreta nao modifica armazenamento`() {
        Mockito.doReturn(usuarioMapper.toModel(usuario(senhaHash = "senha-correta")))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")

        assertThrows(AuthenticationException::class.java) {
            service.login(LoginRequest("admin", "senha-incorreta"))
        }
        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `login bloqueia conta pendente mesmo com placeholder correto`() {
        val pendente = usuario(senhaHash = "!FIRST_ACCESS_REQUIRED!", primeiroAcessoFeito = false)
        Mockito.doReturn(usuarioMapper.toModel(pendente))
            .`when`(usuarioRepository)
            .buscarPorLogin("novo")

        assertThrows(AuthenticationException::class.java) {
            service.login(LoginRequest("novo", "!FIRST_ACCESS_REQUIRED!"))
        }
        Mockito.verify(usuarioRepository, Mockito.never()).atualizarSenha(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `primeiro acesso armazena BCrypt e conclui flag atomicamente`() {
        Mockito.doReturn(usuarioMapper.toModel(usuario(primeiroAcessoFeito = false)))
            .`when`(usuarioRepository)
            .buscarPorCpfEEmail("12345678901", "user@example.com")
        var storedHash: String? = null
        Mockito.doAnswer { invocation ->
            storedHash = invocation.getArgument(1)
            true
        }.`when`(usuarioRepository)
            .concluirPrimeiroAcesso(eq(7L), Mockito.anyString() ?: "")

        service.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "user@example.com", "nova-senha", "admin"))

        Mockito.verify(usuarioRepository).concluirPrimeiroAcesso(eq(7L), Mockito.anyString() ?: "")
        assertTrue(encoder.matches("nova-senha", storedHash ?: ""))
    }

    @Test
    fun `primeiro acesso rejeita senha acima de 72 bytes UTF-8`() {
        val senhaMultibyte = "á".repeat(37)

        assertThrows(BadRequestException::class.java) {
            service.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "user@example.com", senhaMultibyte, "admin"))
        }
        Mockito.verifyNoInteractions(usuarioRepository)
    }

    @Test
    fun `primeiro acesso aceita senha de exatamente 72 bytes UTF-8`() {
        val senhaMultibyte = "á".repeat(36)
        Mockito.doReturn(usuarioMapper.toModel(usuario(primeiroAcessoFeito = false)))
            .`when`(usuarioRepository)
            .buscarPorCpfEEmail("12345678901", "user@example.com")
        var storedHash: String? = null
        Mockito.doAnswer { invocation ->
            storedHash = invocation.getArgument(1)
            true
        }.`when`(usuarioRepository)
            .concluirPrimeiroAcesso(eq(7L), Mockito.anyString() ?: "")

        service.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "user@example.com", senhaMultibyte, "admin"))

        Mockito.verify(usuarioRepository).concluirPrimeiroAcesso(eq(7L), Mockito.anyString() ?: "")
        assertTrue(encoder.matches(senhaMultibyte, storedHash ?: ""))
    }

    @Test
    fun `primeiro acesso rejeita reutilizacao apos conclusao`() {
        Mockito.doReturn(usuarioMapper.toModel(usuario(primeiroAcessoFeito = true)))
            .`when`(usuarioRepository)
            .buscarPorCpfEEmail("12345678901", "user@example.com")

        assertThrows(BadRequestException::class.java) {
            service.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "user@example.com", "nova-senha", "admin"))
        }
        Mockito.verify(usuarioRepository, Mockito.never())
            .concluirPrimeiroAcesso(Mockito.anyLong(), Mockito.anyString() ?: "")
    }

    @Test
    fun `login admin legado mantem perfil ADMIN`() {
        Mockito.doReturn(usuarioMapper.toModel(usuario(senhaHash = "senha-admin")))
            .`when`(usuarioRepository)
            .buscarPorLogin("admin")
        Mockito.doAnswer { true }
            .`when`(usuarioRepository)
            .atualizarSenha(eq(7L), Mockito.anyString() ?: "")

        val token = service.login(LoginRequest("admin", "senha-admin")).token

        assertTrue(jwtService.validarToken(token))
        assertTrue(jwtService.extrairPerfil(token) == "ADMIN")
    }

    @Test
    fun `primeiro acesso retorna nao encontrado para dados sem conta`() {
        Mockito.doReturn(null)
            .`when`(usuarioRepository)
            .buscarPorCpfEEmail("12345678901", "missing@example.com")

        assertThrows(NotFoundException::class.java) {
            service.primeiroAcesso(PrimeiroAcessoRequest("12345678901", "missing@example.com", "nova-senha", "admin"))
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

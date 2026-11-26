package org.greenhouse.unit.service

import org.greenhouse.adapter.NotFoundException
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.model.Usuario
import org.greenhouse.repository.RefreshTokenRepository
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.security.JwtService
import org.greenhouse.service.AuthService
import org.greenhouse.service.PrimeiroAcessoRequest
import org.greenhouse.service.ValidarPrimeiroAcessoRequest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class AuthFirstAccessTest {
    @Mock lateinit var usuarioRepository: UsuarioRepository

    @Mock lateinit var refreshTokenRepository: RefreshTokenRepository

    @Mock lateinit var jwtService: JwtService

    @Spy var usuarioMapper = UsuarioMapper()

    @Mock lateinit var authenticationManager: AuthenticationManager

    @Mock lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks lateinit var service: AuthService

    private val login = "ana.lima"
    private val cpf = "98765432100"
    private val email = "ana@greenhouse.com"

    @Test
    fun `valida login normalizado de usuario ativo com acesso pendente`() {
        `when`(usuarioRepository.buscarPorLogin(login)).thenReturn(usuario())

        assertDoesNotThrow {
            service.validarPrimeiroAcesso(ValidarPrimeiroAcessoRequest(" ANA.LIMA@greenhouse.com "))
        }
    }

    @Test
    fun `rejeita login inexistente`() {
        `when`(usuarioRepository.buscarPorLogin(login)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            service.validarPrimeiroAcesso(ValidarPrimeiroAcessoRequest(login))
        }
    }

    @Test
    fun `rejeita usuario inativo`() {
        `when`(usuarioRepository.buscarPorLogin(login)).thenReturn(usuario(ativo = false))

        assertThrows(NotFoundException::class.java) {
            service.validarPrimeiroAcesso(ValidarPrimeiroAcessoRequest(login))
        }
    }

    @Test
    fun `rejeita usuario que ja concluiu o primeiro acesso`() {
        `when`(usuarioRepository.buscarPorLogin(login)).thenReturn(usuario(primeiroAcessoFeito = true))

        assertThrows(NotFoundException::class.java) {
            service.validarPrimeiroAcesso(ValidarPrimeiroAcessoRequest(login))
        }
    }

    @Test
    fun `nao define senha se o login nao corresponder ao cpf e email`() {
        `when`(usuarioRepository.buscarPorCpfEEmail(cpf, email)).thenReturn(usuario())

        assertThrows(NotFoundException::class.java) {
            service.primeiroAcesso(
                PrimeiroAcessoRequest(
                    cpf = cpf,
                    email = email,
                    novaSenha = "senha-nova",
                    login = "outro.login",
                ),
            )
        }
        verify(passwordEncoder, never()).encode(anyString() ?: "")
    }

    private fun usuario(
        ativo: Boolean = true,
        primeiroAcessoFeito: Boolean = false,
    ) = Usuario(
        id = 7L,
        funcionarioId = 4L,
        login = login,
        senhaHash = "!FIRST_ACCESS_REQUIRED!",
        perfil = "FUNCIONARIO",
        ativo = ativo,
        primeiroAcessoFeito = primeiroAcessoFeito,
    )
}

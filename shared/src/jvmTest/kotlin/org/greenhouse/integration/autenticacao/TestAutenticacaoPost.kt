package org.greenhouse.integration.autenticacao

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith
import org.greenhouse.adapter.AuthenticationException
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.integration.fixture.AutenticacaoFixture
import org.greenhouse.usecase.LoginRequest
import org.greenhouse.usecase.PrimeiroAcessoRequest

class TestAutenticacaoPost {

    // --- TESTES DA OPERAÇÃO POST: /login ---

    @Test
    fun `deve gerar token para login e senha validos`() {
        val fixture = AutenticacaoFixture()
        var loginUsado: String? = null

        try {
            val usuario = fixture.novoUsuarioAtivo()
            loginUsado = usuario.login

            val request = LoginRequest(login = usuario.login, senhaHash = usuario.senhaHash)
            val response = fixture.useCase.login(request)

            assertNotNull(response.token)
            assertEquals(usuario.perfil, response.perfil)
        } finally {
            fixture.limpar(login = loginUsado)
        }
    }

    @Test
    fun `deve rejeitar login com senha incorreta`() {
        val fixture = AutenticacaoFixture()
        var loginUsado: String? = null

        try {
            val usuario = fixture.novoUsuarioAtivo()
            loginUsado = usuario.login

            val request = LoginRequest(login = usuario.login, senhaHash = "senha-errada")

            assertFailsWith<AuthenticationException> {
                fixture.useCase.login(request)
            }
        } finally {
            fixture.limpar(login = loginUsado)
        }
    }

    @Test
    fun `deve rejeitar login de usuario inativo`() {
        val fixture = AutenticacaoFixture()
        var loginUsado: String? = null

        try {
            val usuario = fixture.novoUsuarioInativo()
            loginUsado = usuario.login

            val request = LoginRequest(login = usuario.login, senhaHash = usuario.senhaHash)

            assertFailsWith<AuthenticationException> {
                fixture.useCase.login(request)
            }
        } finally {
            fixture.limpar(login = loginUsado)
        }
    }

    // --- TESTES DA OPERAÇÃO POST: /primeiro-acesso ---

    @Test
    fun `deve redefinir senha com sucesso`() {
        val fixture = AutenticacaoFixture()
        var cpfUsado: String? = null
        var emailUsado: String? = null

        try {
            val usuario = fixture.novoUsuarioAtivo()
            // Simulação de resgate dos dados atrelados. Ajuste conforme a sua modelagem e banco
            cpfUsado = "12345678901"
            emailUsado = "teste@greenhouse.dev"

            val request = PrimeiroAcessoRequest(
                cpf = cpfUsado,
                email = emailUsado,
                novaSenha = "nova-senha-segura"
            )

            fixture.useCase.primeiroAcesso(request)
        } finally {
            fixture.limpar(cpf = cpfUsado, email = emailUsado)
        }
    }

    @Test
    fun `deve rejeitar nova senha menor que 6 caracteres`() {
        val fixture = AutenticacaoFixture()

        val request = PrimeiroAcessoRequest(
            cpf = "12345678901",
            email = "teste@greenhouse.dev",
            novaSenha = "12345"
        )

        assertFailsWith<BadRequestException> {
            fixture.useCase.primeiroAcesso(request)
        }
    }

    @Test
    fun `deve lancar not found para combinacao cpf e email incorreta`() {
        val fixture = AutenticacaoFixture()

        val request = PrimeiroAcessoRequest(
            cpf = "00000000000",
            email = "errado@greenhouse.dev",
            novaSenha = "nova-senha-segura"
        )

        assertFailsWith<NotFoundException> {
            fixture.useCase.primeiroAcesso(request)
        }
    }
}
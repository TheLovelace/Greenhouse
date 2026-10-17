package org.greenhouse.integration.security

import org.greenhouse.GreenhouseApplication
import org.greenhouse.security.LegacyPasswordAuthenticationProvider
import org.greenhouse.service.AuthService
import org.greenhouse.service.LoginRequest
import org.greenhouse.service.PrimeiroAcessoRequest
import org.greenhouse.service.TokenResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest(
    classes = [GreenhouseApplication::class],
    properties = [
        "jwt.secret=test-only-jwt-secret-for-auth-route-tests-000000000000000000",
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
            "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
class AuthRouteSecurityTest : SpringBootWebTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @MockitoBean
    private lateinit var authService: AuthService

    @Test
    fun `contexto Spring configura AuthenticationManager e PasswordEncoder`() {
        assertNotNull(authenticationManager)
        assertEquals(true, passwordEncoder.matches("senha", passwordEncoder.encode("senha")))
        val providerManager = assertInstanceOf(ProviderManager::class.java, authenticationManager)
        assertTrue(providerManager.providers.any { it is LegacyPasswordAuthenticationProvider })
    }

    @Test
    fun `signin e signup ficam acessiveis sem token e rotas antigas nao sao mais publicas`() {
        Mockito.doReturn(TokenResponse(token = "test-token", perfil = "ADMIN"))
            .`when`(authService)
            .login(Mockito.any(LoginRequest::class.java) ?: LoginRequest("admin", "senha"))
        Mockito.doNothing()
            .`when`(authService)
            .primeiroAcesso(Mockito.any(PrimeiroAcessoRequest::class.java) ?: PrimeiroAcessoRequest("", "", ""))

        val signinBody = """{"login":"admin","senhaHash":"senha"}"""
        val signupBody = """{"cpf":"12345678901","email":"admin@example.com","novaSenha":"senha-segura"}"""

        val signinStatus =
            mockMvc.perform(
                post("/api/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(signinBody),
            )
                .andExpect(content().json("""{"token":"test-token","perfil":"ADMIN"}"""))
                .andReturn().response.status
        val signupStatus =
            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(signupBody),
            ).andReturn().response.status
        val oldSigninStatus =
            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(signinBody),
            ).andReturn().response.status
        val oldSignupStatus =
            mockMvc.perform(
                post("/api/auth/primeiro-acesso")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(signupBody),
            ).andReturn().response.status

        assertEquals(200, signinStatus)
        assertEquals(204, signupStatus)
        assertEquals(401, oldSigninStatus)
        assertEquals(401, oldSignupStatus)
    }
}

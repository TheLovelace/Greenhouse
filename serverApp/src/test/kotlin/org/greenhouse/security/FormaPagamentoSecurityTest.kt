package org.greenhouse.security

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.config.WebConfig
import org.greenhouse.controller.FormaPagamentoController
import org.greenhouse.controller.OpenApiController
import org.greenhouse.entity.FormaPagamento
import org.greenhouse.usecase.FormaPagamentoUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [FormaPagamentoController::class, OpenApiController::class])
@Import(SecurityConfig::class, JwtAuthenticationFilter::class, JwtService::class, WebConfig::class)
class FormaPagamentoSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var formaPagamentoUseCase: FormaPagamentoUseCase

    @MockitoBean
    private lateinit var databaseConfig: DatabaseConfig

    private val formaPagamento = FormaPagamento(id = 1, nome = "PIX")

    @Test
    fun `todas as rotas do CRUD rejeitam requisicoes sem token`() {
        crudRequests().forEach { request ->
            mockMvc
                .perform(request)
                .andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `todas as rotas do CRUD rejeitam token invalido`() {
        crudRequests().forEach { request ->
            mockMvc
                .perform(request.header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `tokens validos de ADMIN e FUNCIONARIO autorizam todas as operacoes`() {
        Mockito
            .doReturn(formaPagamento)
            .`when`(formaPagamentoUseCase)
            .criar(Mockito.any(FormaPagamento::class.java) ?: formaPagamento)
        Mockito
            .doReturn(listOf(formaPagamento))
            .`when`(formaPagamentoUseCase)
            .listar()
        Mockito
            .doReturn(formaPagamento)
            .`when`(formaPagamentoUseCase)
            .buscarPorId(1)
        Mockito
            .doReturn(formaPagamento)
            .`when`(formaPagamentoUseCase)
            .atualizar(Mockito.anyLong(), Mockito.anyMap<String, Any?>() ?: emptyMap())

        val expectedStatuses = listOf(201, 200, 200, 200, 204)
        listOf("ADMIN", "FUNCIONARIO").forEach { perfil ->
            val token = jwtService.gerarToken(perfil.lowercase(), perfil)
            crudRequests().zip(expectedStatuses).forEach { (request, expectedStatus) ->
                val result =
                    mockMvc
                        .perform(request.header("Authorization", "Bearer $token"))
                        .andReturn()
                assertEquals(expectedStatus, result.response.status)
            }
        }
    }

    @Test
    fun `fragmento OpenAPI fica acessivel sem token`() {
        val fragment =
            mockMvc
                .perform(get("/api/openapi/forma-pagamento.yml"))
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

        assertEquals(true, fragment.contains("BearerAuth"))
    }

    private fun crudRequests(): List<MockHttpServletRequestBuilder> =
        listOf(
            post("/api/forma-pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"PIX"}"""),
            get("/api/forma-pagamento"),
            get("/api/forma-pagamento/1"),
            patch("/api/forma-pagamento/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Cartão"}"""),
            delete("/api/forma-pagamento/1"),
        )
}

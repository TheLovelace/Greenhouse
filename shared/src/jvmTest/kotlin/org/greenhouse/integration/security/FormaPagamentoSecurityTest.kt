package org.greenhouse.integration.security

import org.greenhouse.GreenhouseApplication
import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.dto.response.FormaPagamentoResponseDTO
import org.greenhouse.security.JwtService
import org.greenhouse.service.FormaPagamentoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(
    classes = [GreenhouseApplication::class],
    properties = [
        "jwt.secret=test-only-jwt-secret-for-payment-method-tests-0000000000000",
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
            "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
class FormaPagamentoSecurityTest : SpringBootWebTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var formaPagamentoService: FormaPagamentoService

    private val formaPagamento =
        FormaPagamentoResponseDTO(
            id = 1,
            nome = "PIX",
            ativo = true,
            descricao = null,
            limiteParcelas = 1,
            prazoEstornoDias = null,
        )

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
            .`when`(formaPagamentoService)
            .criar(Mockito.any(FormaPagamentoRequestDTO::class.java) ?: FormaPagamentoRequestDTO(nome = "PIX"))
        Mockito
            .doReturn(listOf(formaPagamento))
            .`when`(formaPagamentoService)
            .listar(Mockito.any())
        Mockito
            .doReturn(formaPagamento)
            .`when`(formaPagamentoService)
            .buscarPorId(1)
        Mockito
            .doReturn(formaPagamento)
            .`when`(formaPagamentoService)
            .atualizar(
                Mockito.anyLong(),
                Mockito.any(FormaPagamentoRequestDTO::class.java) ?: FormaPagamentoRequestDTO(nome = "Cartão"),
            )

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
    fun `POST e PUT rejeitam limites invalidos pelo Bean Validation`() {
        val token = jwtService.gerarToken("admin", "ADMIN")
        val invalidBody = """{"nome":"Cartão","limiteParcelas":0}"""

        listOf(
            post("/api/forma-pagamento"),
            put("/api/forma-pagamento/1"),
        ).forEach { request ->
            val result =
                mockMvc.perform(
                    request
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody),
                ).andReturn()
            assertEquals(400, result.response.status)
        }
        Mockito.verifyNoInteractions(formaPagamentoService)
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

    @Test
    fun `metodo PATCH nao esta mais mapeado`() {
        val token = jwtService.gerarToken("admin", "ADMIN")
        val result =
            mockMvc.perform(
                patch("/api/forma-pagamento/1")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"nome":"Cartão"}"""),
            ).andReturn()

        assertEquals(405, result.response.status)
    }

    private fun crudRequests(): List<MockHttpServletRequestBuilder> =
        listOf(
            post("/api/forma-pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"PIX"}"""),
            get("/api/forma-pagamento"),
            get("/api/forma-pagamento/1"),
            put("/api/forma-pagamento/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Cartão"}"""),
            delete("/api/forma-pagamento/1"),
        )
}

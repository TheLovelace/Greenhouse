package org.greenhouse.integration.security

import org.greenhouse.GreenhouseApplication
import org.greenhouse.dto.FuncionarioDTO as Funcionario
import org.greenhouse.security.JwtService
import org.greenhouse.service.FuncionarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
            "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
class FuncionarioSecurityTest : SpringBootWebTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var funcionarioService: FuncionarioService

    private val funcionario =
        Funcionario(
            id = 1,
            nome = "Funcionário de teste",
            cpf = "12345678901",
        )

    @Test
    fun `todas as rotas do CRUD rejeitam requisicoes sem token mesmo com header de perfil`() {
        crudRequests().forEach { request ->
            mockMvc.perform(request.header("X-User-Perfil", "ADMIN"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `todas as rotas do CRUD rejeitam funcionario mesmo com header de perfil ADMIN`() {
        val token = jwtService.gerarToken("funcionario", "FUNCIONARIO")

        crudRequests().forEach { request ->
            mockMvc.perform(request.bearer(token).header("X-User-Perfil", "ADMIN"))
                .andExpect(status().isForbidden)
        }
    }

    @Test
    fun `token ADMIN autoriza as cinco operacoes do CRUD`() {
        Mockito.doReturn(funcionario)
            .`when`(funcionarioService)
            .criar(Mockito.any(Funcionario::class.java) ?: funcionario)
        Mockito.doReturn(listOf(funcionario))
            .`when`(funcionarioService)
            .listar()
        Mockito.doReturn(funcionario)
            .`when`(funcionarioService)
            .buscarPorId(1)
        Mockito.doReturn(funcionario)
            .`when`(funcionarioService)
            .atualizar(Mockito.anyLong(), Mockito.any(Funcionario::class.java) ?: funcionario)
        val token = jwtService.gerarToken("admin", "ADMIN")
        val expectedStatuses = listOf(201, 200, 200, 200, 204)

        crudRequests().zip(expectedStatuses).forEach { (request, expectedStatus) ->
            val result = mockMvc.perform(request.bearer(token)).andReturn()
            assertEquals(expectedStatus, result.response.status)
        }
    }

    @Test
    fun `documentos OpenAPI permanecem acessiveis sem token`() {
        val specification = mockMvc.perform(get("/api/openapi.yaml"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
        val fragment = mockMvc.perform(get("/api/openapi/funcionario.yml"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        assertTrue(specification.contains("BearerAuth"))
        assertTrue(fragment.contains("BearerAuth"))
        assertTrue(!fragment.contains("X-User-Perfil"))
    }

    @Test
    fun `Swagger UI nao exige autenticacao`() {
        val result = mockMvc.perform(get("/swagger-ui/index.html")).andReturn()

        assert(result.response.status !in setOf(401, 403))
    }

    @Test
    fun `metodo PATCH nao esta mais mapeado`() {
        val token = jwtService.gerarToken("admin", "ADMIN")
        val result =
            mockMvc.perform(
                patch("/api/funcionario/1")
                    .bearer(token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"nome":"Nome atualizado","cpf":"12345678901"}"""),
            ).andReturn()

        assertEquals(405, result.response.status)
    }

    private fun crudRequests(): List<MockHttpServletRequestBuilder> =
        listOf(
            post("/api/funcionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Funcionário de teste","cpf":"12345678901"}"""),
            get("/api/funcionario"),
            get("/api/funcionario/1"),
            put("/api/funcionario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Nome atualizado","cpf":"12345678901"}"""),
            delete("/api/funcionario/1"),
        )

    private fun MockHttpServletRequestBuilder.bearer(token: String): MockHttpServletRequestBuilder =
        header("Authorization", "Bearer $token")
}

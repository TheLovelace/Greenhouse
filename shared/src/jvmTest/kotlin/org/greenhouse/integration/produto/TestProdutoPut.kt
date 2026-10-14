package org.greenhouse.integration.produto

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.ProdutoCreateRequestDTO
import org.greenhouse.dto.request.ProdutoUpdateRequestDTO
import org.greenhouse.integration.fixture.ProdutoFixture
import java.math.BigDecimal
import java.util.UUID

class TestProdutoPut {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `deve atualizar apenas os campos enviados e preservar os demais`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto(prazoPreparoMinutos = 15)
            id = criado.id

            val atualizado = fixture.service.atualizar(
                criado.id!!,
                produtoUpdateRequest { nome = "Rosa branca" },
            )

            assertEquals(criado.id, atualizado.id)
            assertEquals("Rosa branca", atualizado.nome)
            assertEquals(criado.codigo, atualizado.codigo)
            assertEquals(criado.descricao, atualizado.descricao)
            assertEquals(criado.preco, atualizado.preco)
            assertEquals(criado.ativo, atualizado.ativo)
            assertEquals(criado.prazoPreparoMinutos, atualizado.prazoPreparoMinutos)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve limpar codigo e descricao enviados como null e preservar prazo`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto(prazoPreparoMinutos = 15)
            id = criado.id

            val atualizado = fixture.service.atualizar(
                criado.id!!,
                produtoUpdateRequest {
                    codigo = null
                    descricao = null
                },
            )

            assertEquals(null, atualizado.codigo)
            assertEquals(null, atualizado.descricao)
            assertEquals(criado.prazoPreparoMinutos, atualizado.prazoPreparoMinutos)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve diferenciar campo omitido de campo opcional enviado como null`() {
        val objectMapper = ObjectMapper()
        val omitido = objectMapper.readValue("""{"nome":"Rosa"}""", ProdutoUpdateRequestDTO::class.java)
        val nulo = objectMapper.readValue("""{"codigo":null}""", ProdutoUpdateRequestDTO::class.java)

        assertTrue(!omitido.codigoInformado)
        assertTrue(omitido.nomeInformado)
        assertTrue(nulo.codigoInformado)
        assertTrue(nulo.codigo == null)
    }

    @Test
    fun `deve permitir manter codigo existente no proprio produto`() {
        val fixture = ProdutoFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoProduto()
            id = criado.id
            val atualizado = fixture.service.atualizar(
                criado.id!!,
                produtoUpdateRequest { codigo = criado.codigo },
            )

            assertEquals(criado.codigo, atualizado.codigo)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar codigo duplicado ao atualizar`() {
        val fixture = ProdutoFixture()
        val codigo = "SKU-${UUID.randomUUID()}"
        val ids = mutableListOf<Long>()

        try {
            val primeiro = fixture.novoProduto(codigo = codigo)
            val segundo = fixture.novoProduto()
            ids += primeiro.id!!
            ids += segundo.id!!

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(
                    segundo.id,
                    produtoUpdateRequest { this.codigo = codigo },
                )
            }
        } finally {
            ids.forEach(fixture::limpar)
        }
    }

    @Test
    fun `deve rejeitar id invalido e inexistente no put`() {
        val fixture = ProdutoFixture()
        val request = produtoUpdateRequest { nome = "Rosa branca" }

        assertFailsWith<BadRequestException> {
            fixture.service.atualizar(0, request)
        }
        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(Long.MAX_VALUE, request)
        }
    }

    @Test
    fun `requests create e update validam tamanhos preco e prazo`() {
        val create = ProdutoCreateRequestDTO(
            codigo = "c".repeat(51),
            nome = "n".repeat(151),
            descricao = "d".repeat(1001),
            preco = BigDecimal("-0.01"),
            prazoPreparoMinutos = -1,
        )
        val update = produtoUpdateRequest {
            codigo = "c".repeat(51)
            nome = "n".repeat(151)
            descricao = "d".repeat(1001)
            preco = BigDecimal("-0.01")
        }

        assertTrue(
            validator.validate(create).map { it.propertyPath.toString() }
                .containsAll(listOf("codigo", "nome", "descricao", "preco", "prazoPreparoMinutos")),
        )
        assertTrue(
            validator.validate(update).map { it.propertyPath.toString() }
                .containsAll(listOf("codigo", "nome", "descricao", "preco")),
        )
    }

    @Test
    fun `create e update declaram regras distintas de preco`() {
        val createRules = validator.getConstraintsForClass(ProdutoCreateRequestDTO::class.java)
            .getConstraintsForProperty("preco")!!.constraintDescriptors.map { it.annotation.annotationClass.java }
        val updateRules = validator.getConstraintsForClass(ProdutoUpdateRequestDTO::class.java)
            .getConstraintsForProperty("preco")!!.constraintDescriptors.map { it.annotation.annotationClass.java }

        assertTrue(createRules.contains(jakarta.validation.constraints.DecimalMin::class.java))
        assertTrue(createRules.contains(jakarta.validation.constraints.Digits::class.java))
        assertTrue(updateRules.contains(jakarta.validation.constraints.DecimalMin::class.java))
        assertTrue(updateRules.contains(jakarta.validation.constraints.Digits::class.java))
    }

    @Test
    fun `update vazio e nulo em campo obrigatorio sao invalidados`() {
        val vazio = ProdutoUpdateRequestDTO()
        val nomeNulo = produtoUpdateRequest { nome = null }

        assertTrue(validator.validate(vazio).any { it.propertyPath.toString() == "possuiCamposParaAtualizar" })
        assertTrue(validator.validate(nomeNulo).any { it.propertyPath.toString() == "valoresInformadosSaoValidos" })
    }

    private fun produtoUpdateRequest(
        configure: ProdutoUpdateRequestDTO.() -> Unit,
    ) = ProdutoUpdateRequestDTO().apply(configure)
}

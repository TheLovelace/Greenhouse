package org.greenhouse.integration.categoria

import jakarta.validation.Validation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.CategoriaCreateRequestDTO
import org.greenhouse.dto.request.CategoriaUpdateRequestDTO
import org.greenhouse.integration.fixture.CategoriaFixture

class TestCategoriaPut {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `deve atualizar apenas os campos enviados e preservar os demais`() {
        val fixture = CategoriaFixture()
        var id: Long? = null

        try {
            val criada = fixture.novaCategoria(observacao = "Observação anterior")
            id = criada.id

            val atualizada = fixture.service.atualizar(
                criada.id!!,
                CategoriaUpdateRequestDTO(
                    id = -1,
                    nome = "Folhagens",
                ),
            )

            assertEquals(criada.id, atualizada.id)
            assertEquals("Folhagens", atualizada.nome)
            assertEquals(criada.descricao, atualizada.descricao)
            assertEquals(criada.ativo, atualizada.ativo)
            assertEquals(criada.observacao, atualizada.observacao)
            assertEquals(criada.dataCadastro, atualizada.dataCadastro)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar nome duplicado ao atualizar`() {
        val fixture = CategoriaFixture()
        val ids = mutableListOf<Long>()

        try {
            val primeira = fixture.novaCategoria()
            val segunda = fixture.novaCategoria()
            ids += primeira.id!!
            ids += segunda.id!!

            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(
                    segunda.id,
                    CategoriaUpdateRequestDTO(
                        nome = primeira.nome,
                    ),
                )
            }
        } finally {
            ids.forEach(fixture::limpar)
        }
    }

    @Test
    fun `deve rejeitar id invalido e inexistente no put`() {
        val fixture = CategoriaFixture()
        val request = CategoriaUpdateRequestDTO(nome = "Folhagens")

        assertFailsWith<BadRequestException> {
            fixture.service.atualizar(0, request)
        }

        @Test
        fun `deve rejeitar put sem campos editaveis`() {
            val fixture = CategoriaFixture()
            val criada = fixture.novaCategoria()

            try {
                assertFailsWith<BadRequestException> {
                    fixture.service.atualizar(criada.id!!, CategoriaUpdateRequestDTO(id = -1))
                }
            } finally {
                fixture.limpar(criada.id)
            }
        }
        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(Long.MAX_VALUE, request)
        }
    }

    @Test
    fun `requests create e update validam nome descricao e observacao`() {
        val create = CategoriaCreateRequestDTO(
            nome = "n".repeat(101),
            descricao = "d".repeat(1001),
            observacao = "o".repeat(301),
        )
        val update = CategoriaUpdateRequestDTO(
            nome = "n".repeat(101),
            descricao = "d".repeat(1001),
        )

        assertTrue(validator.validate(create).map { it.propertyPath.toString() }.containsAll(
            listOf("nome", "descricao", "observacao"),
        ))
        assertTrue(validator.validate(update).map { it.propertyPath.toString() }.containsAll(
            listOf("nome", "descricao"),
        ))
    }

    @Test
    fun `create e update declaram regras de tamanho e update exige ao menos um campo`() {
        val createRules = validator.getConstraintsForClass(CategoriaCreateRequestDTO::class.java)
            .getConstraintsForProperty("nome")!!.constraintDescriptors.map { it.annotation.annotationClass.java }
        val updateRules = validator.getConstraintsForClass(CategoriaUpdateRequestDTO::class.java)
            .getConstraintsForProperty("nome")!!.constraintDescriptors.map { it.annotation.annotationClass.java }
        val updateEmpty = validator.validate(CategoriaUpdateRequestDTO())
            .map { it.propertyPath.toString() }

        assertTrue(createRules.contains(jakarta.validation.constraints.NotNull::class.java))
        assertTrue(createRules.contains(jakarta.validation.constraints.Size::class.java))
        assertTrue(updateRules.contains(jakarta.validation.constraints.Size::class.java))
        assertTrue(updateEmpty.contains("possuiCamposParaAtualizar"))
    }
}

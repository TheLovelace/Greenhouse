package org.greenhouse.integration.estoque

import jakarta.validation.Validation
import org.greenhouse.dto.request.EstoqueCreateRequestDTO
import org.greenhouse.dto.request.EstoqueUpdateRequestDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TestEstoqueRequestValidation {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `deve rejeitar campos invalidos no request de criacao`() {
        val request =
            EstoqueCreateRequestDTO(
                produtoId = 0,
                quantidadeFisica = -1,
                quantidadeMinima = -1,
                localizacao = "a".repeat(101),
            )

        val violations = validator.validate(request)

        assertEquals(4, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "produtoId" })
        assertTrue(violations.any { it.propertyPath.toString() == "quantidadeFisica" })
        assertTrue(violations.any { it.propertyPath.toString() == "quantidadeMinima" })
        assertTrue(violations.any { it.propertyPath.toString() == "localizacao" })
    }

    @Test
    fun `deve rejeitar campos invalidos no request de atualizacao`() {
        val request =
            EstoqueUpdateRequestDTO(
                quantidadeFisica = -1,
                quantidadeMinima = -1,
                localizacao = "a".repeat(101),
                bloqueado = false,
            )

        val violations = validator.validate(request)

        assertEquals(3, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "quantidadeFisica" })
        assertTrue(violations.any { it.propertyPath.toString() == "quantidadeMinima" })
        assertTrue(violations.any { it.propertyPath.toString() == "localizacao" })
    }

    @Test
    fun `deve aceitar requests validos`() {
        val createRequest = EstoqueCreateRequestDTO(produtoId = 1)
        val updateRequest =
            EstoqueUpdateRequestDTO(
                quantidadeFisica = 0,
                quantidadeMinima = 0,
                bloqueado = false,
            )

        assertTrue(validator.validate(createRequest).isEmpty())
        assertTrue(validator.validate(updateRequest).isEmpty())
    }
}

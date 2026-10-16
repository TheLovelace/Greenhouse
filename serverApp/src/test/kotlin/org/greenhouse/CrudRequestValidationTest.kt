package org.greenhouse

import jakarta.validation.Validation
import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.dto.request.FuncionarioRequestDTO
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrudRequestValidationTest {
    private val validatorFactory = Validation.buildDefaultValidatorFactory()
    private val validator = validatorFactory.validator

    @Test
    fun `funcionario request rejects blank name and malformed CPF`() {
        val errors = validator.validate(FuncionarioRequestDTO(nome = " ", cpf = "123"))
            .map { it.propertyPath.toString() }
            .toSet()

        assertEquals(setOf("nome", "cpf"), errors)
    }

    @Test
    fun `forma de pagamento request rejects blank name and invalid limits`() {
        val errors = validator.validate(
            FormaPagamentoRequestDTO(
                nome = "",
                limiteParcelas = 0,
                prazoEstornoDias = -1,
            ),
        ).map { it.propertyPath.toString() }
            .toSet()

        assertEquals(setOf("nome", "limiteParcelas", "prazoEstornoDias"), errors)
    }

    @Test
    fun `valid requests pass Bean Validation`() {
        assertTrue(
            validator.validate(
                FuncionarioRequestDTO(nome = "Luna", cpf = "12345678901"),
            ).isEmpty(),
        )
        assertTrue(
            validator.validate(
                FormaPagamentoRequestDTO(nome = "Cartão", limiteParcelas = 1),
            ).isEmpty(),
        )
    }

    @AfterAll
    fun closeValidatorFactory() {
        validatorFactory.close()
    }
}

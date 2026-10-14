package org.greenhouse.dto.request

import jakarta.validation.Validation
import org.greenhouse.enums.TipoDesconto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class DescontoRequestDTOTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `deve rejeitar titulo vazio valor nao positivo e quantidade minima invalida`() {
        val request =
            DescontoRequestDTO(
                titulo = " ",
                tipo = TipoDesconto.PERCENTUAL,
                valor = BigDecimal.ZERO,
                quantidadeMinimaItens = 0,
            )

        val violations = validator.validate(request)

        assertEquals(3, violations.size)
        assertTrue(violations.any { it.propertyPath.toString() == "titulo" })
        assertTrue(violations.any { it.propertyPath.toString() == "valor" })
        assertTrue(violations.any { it.propertyPath.toString() == "quantidadeMinimaItens" })
    }

    @Test
    fun `deve aceitar request com campos validos`() {
        val request =
            DescontoRequestDTO(
                titulo = "Oferta",
                tipo = TipoDesconto.PERCENTUAL,
                valor = BigDecimal("10.00"),
            )

        assertTrue(validator.validate(request).isEmpty())
    }
}

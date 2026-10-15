package org.greenhouse.ui.screens.desconto.validation

import org.greenhouse.ui.screens.desconto.model.DescontoCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DescontoCadastroValidatorTest {
    @Test
    fun dateInputIsFormattedAsBrazilianDate() {
        assertEquals("31/12/2026", formatarDataBrasileira("31122026"))
        assertEquals("31/12/2026", formatarDataBrasileira("31/12/2026"))
        assertEquals("31/12", formatarDataBrasileira("31/12/"))
        assertEquals("31", formatarDataBrasileira("31x"))
    }

    @Test
    fun discountValueInputKeepsOnlyOneDecimalSeparatorAndTwoFractionDigits() {
        assertEquals("12,50", formatarValorDesconto("12,50"))
        assertEquals("12.50", formatarValorDesconto("12.50"))
        assertEquals("12,50", formatarValorDesconto("a1b2,5x0,75"))
    }

    @Test
    fun validBrazilianDatesConvertToOffsetDateTime() {
        assertEquals("2026-12-31T00:00:00Z", "31/12/2026".toOffsetDateTimeOrNull())
        assertEquals("2024-02-29T00:00:00Z", "29/02/2024".toOffsetDateTimeOrNull())
    }

    @Test
    fun invalidCalendarDatesAreRejected() {
        assertEquals(null, "29/02/2025".toOffsetDateTimeOrNull())
        assertEquals(null, "31/04/2026".toOffsetDateTimeOrNull())
    }

    @Test
    fun validationRejectsEndDateBeforeStartDate() {
        val errors =
            validarDescontoCadastro(
                DescontoCadastroForm(
                    titulo = "Desconto",
                    valor = "10",
                    dataInicio = "20/12/2026",
                    dataFim = "19/12/2026",
                ),
            )

        assertEquals("A data final não pode ser anterior à data inicial", errors["dataFim"])
    }

    @Test
    fun validationRejectsNonNumericMinimumQuantity() {
        val errors =
            validarDescontoCadastro(
                DescontoCadastroForm(
                    titulo = "Desconto",
                    valor = "10",
                    quantidadeMinimaItens = "2a",
                ),
            )

        assertTrue(errors.containsKey("quantidadeMinimaItens"))
        assertFalse(errors.containsKey("titulo"))
    }
}

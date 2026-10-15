package org.greenhouse.ui.screens.estoque.validation

import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EstoqueCadastroValidatorTest {
    @Test
    fun quantityInputKeepsOnlyDigits() {
        assertEquals("120", formatarQuantidadeEstoque("1a2-0"))
        assertEquals("", formatarQuantidadeEstoque("abc"))
    }

    @Test
    fun validFormConvertsToCreateData() {
        val data =
            EstoqueCadastroForm(
                produtoId = "42",
                quantidadeFisica = "12",
                quantidadeMinima = "3",
                localizacao = " Estante A-01 ",
                bloqueado = true,
            ).toEstoqueCadastroDataOrNull()

        assertEquals(42L, data?.produtoId)
        assertEquals(12, data?.quantidadeFisica)
        assertEquals(3, data?.quantidadeMinima)
        assertEquals("Estante A-01", data?.localizacao)
        assertTrue(data?.bloqueado == true)
    }

    @Test
    fun blankOptionalValuesUseBackendDefaults() {
        val data =
            EstoqueCadastroForm(
                produtoId = "1",
                quantidadeFisica = "",
                quantidadeMinima = "",
                localizacao = "  ",
            ).toEstoqueCadastroDataOrNull()

        assertEquals(0, data?.quantidadeFisica)
        assertEquals(0, data?.quantidadeMinima)
        assertNull(data?.localizacao)
        assertFalse(data?.bloqueado ?: true)
    }

    @Test
    fun validationRejectsMissingOrInvalidProductId() {
        assertTrue(validarEstoqueCadastro(EstoqueCadastroForm()).containsKey("produtoId"))
        assertTrue(
            validarEstoqueCadastro(EstoqueCadastroForm(produtoId = "0"))
                .containsKey("produtoId"),
        )
        assertNull(
            EstoqueCadastroForm(produtoId = "999999999999999999999999")
                .toEstoqueCadastroDataOrNull(),
        )
    }

    @Test
    fun validationRejectsIntegerOverflowAndLongLocations() {
        val errors =
            validarEstoqueCadastro(
                EstoqueCadastroForm(
                    produtoId = "1",
                    quantidadeFisica = "2147483648",
                    quantidadeMinima = "2147483648",
                    localizacao = "x".repeat(101),
                ),
            )

        assertTrue(errors.containsKey("quantidadeFisica"))
        assertTrue(errors.containsKey("quantidadeMinima"))
        assertEquals("A localização deve ter no máximo 100 caracteres", errors["localizacao"])
    }

    @Test
    fun validationRejectsNegativeAndNonIntegerQuantities() {
        val errors =
            validarEstoqueCadastro(
                EstoqueCadastroForm(
                    produtoId = "1",
                    quantidadeFisica = "-1",
                    quantidadeMinima = "1.5",
                ),
            )

        assertTrue(errors.containsKey("quantidadeFisica"))
        assertTrue(errors.containsKey("quantidadeMinima"))
    }

    @Test
    fun validZeroQuantitiesAreAccepted() {
        val errors =
            validarEstoqueCadastro(
                EstoqueCadastroForm(
                    produtoId = "1",
                    quantidadeFisica = "0",
                    quantidadeMinima = "0",
                ),
            )

        assertFalse(errors.containsKey("quantidadeFisica"))
        assertFalse(errors.containsKey("quantidadeMinima"))
    }
}

package org.greenhouse.ui.screens.especie.validation

import org.greenhouse.ui.screens.especie.model.EspecieCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EspecieCadastroValidatorTest {
    private fun formValido() =
        EspecieCadastroForm(
            nomePopular = "Jiboia",
            nomeCientifico = "Epipremnum aureum",
            intervaloRegaDias = "5",
            temperaturaMin = "15",
            temperaturaMax = "30",
        )

    @Test
    fun integerInputKeepsOnlyDigits() {
        assertEquals("120", formatarInteiroEspecie("1a2-0"))
        assertEquals("", formatarInteiroEspecie("abc"))
    }

    @Test
    fun decimalInputKeepsOnlyOneSeparatorAndTwoFractionDigits() {
        assertEquals("12,50", formatarDecimalEspecie("12,50"))
        assertEquals("12.50", formatarDecimalEspecie("12.50"))
        assertEquals("12,50", formatarDecimalEspecie("a1b2,5x0,75"))
    }

    @Test
    fun validFormConvertsToBackendValues() {
        val data =
            formValido()
                .copy(
                    nomePopular = " Jiboia ",
                    descricao = "  ",
                    temperaturaMin = "15,5",
                    umidadeArMin = "40",
                    umidadeArMax = "70,25",
                ).toEspecieCadastroDataOrNull()

        assertEquals("Jiboia", data?.nomePopular)
        assertNull(data?.descricao)
        assertEquals(5, data?.intervaloRegaDias)
        assertEquals(15.5, data?.temperaturaMin)
        assertEquals(30.0, data?.temperaturaMax)
        assertEquals(40.0, data?.umidadeArMin)
        assertEquals(70.25, data?.umidadeArMax)
        assertNull(data?.umidadeSoloMin)
    }

    @Test
    fun requiredFieldsAreReported() {
        val errors = validarEspecieCadastro(EspecieCadastroForm())

        assertTrue(errors.containsKey("nomePopular"))
        assertTrue(errors.containsKey("nomeCientifico"))
        assertTrue(errors.containsKey("intervaloRegaDias"))
        assertTrue(errors.containsKey("temperaturaMin"))
        assertTrue(errors.containsKey("temperaturaMax"))
        assertFalse(errors.containsKey("umidadeArMin"))
        assertFalse(errors.containsKey("descricao"))
    }

    @Test
    fun wateringAndLengthLimitsAreEnforced() {
        val errors =
            validarEspecieCadastro(
                formValido().copy(
                    nomePopular = "x".repeat(151),
                    nomeCientifico = "x".repeat(151),
                    intervaloRegaDias = "0",
                    descricao = "x".repeat(2001),
                ),
            )

        assertEquals("O nome popular deve ter no máximo 150 caracteres", errors["nomePopular"])
        assertEquals("O nome científico deve ter no máximo 150 caracteres", errors["nomeCientifico"])
        assertTrue(errors.containsKey("intervaloRegaDias"))
        assertEquals("A descrição deve ter no máximo 2000 caracteres", errors["descricao"])
    }

    @Test
    fun temperatureRangeMustNotBeInverted() {
        val errors = validarEspecieCadastro(formValido().copy(temperaturaMin = "30", temperaturaMax = "10"))

        assertEquals("A temperatura máxima deve ser maior ou igual à mínima", errors["temperaturaMax"])
    }

    @Test
    fun temperatureWithTooManyDigitsOrDecimalsIsRejected() {
        val errors = validarEspecieCadastro(formValido().copy(temperaturaMax = "1000"))
        assertTrue(errors.containsKey("temperaturaMax"))

        val decimais = validarEspecieCadastro(formValido().copy(temperaturaMin = "10,123"))
        assertTrue(decimais.containsKey("temperaturaMin"))
    }

    @Test
    fun humidityMustBeBetweenZeroAndOneHundred() {
        val errors =
            validarEspecieCadastro(
                formValido().copy(umidadeArMin = "101", umidadeSoloMax = "100,5"),
            )

        assertEquals("A umidade deve estar entre 0 e 100", errors["umidadeArMin"])
        assertEquals("A umidade deve estar entre 0 e 100", errors["umidadeSoloMax"])
    }

    @Test
    fun humidityRangeMustNotBeInverted() {
        val errors =
            validarEspecieCadastro(
                formValido().copy(
                    umidadeArMin = "70",
                    umidadeArMax = "40",
                    umidadeSoloMin = "60",
                    umidadeSoloMax = "30",
                ),
            )

        assertTrue(errors.containsKey("umidadeArMax"))
        assertTrue(errors.containsKey("umidadeSoloMax"))
    }

    @Test
    fun invalidFormDoesNotConvertToData() {
        assertNull(EspecieCadastroForm().toEspecieCadastroDataOrNull())
    }
}

package org.greenhouse.ui.screens.alerta.validation

import org.greenhouse.ui.screens.alerta.model.AlertaCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AlertaCadastroValidatorTest {
    private fun formValido() =
        AlertaCadastroForm(
            dispositivoIotId = "3",
            especieId = "7",
            titulo = "Temperatura fora do ideal",
        )

    @Test
    fun idAndPriorityInputKeepOnlyDigits() {
        assertEquals("120", formatarIdAlerta("1a2-0"))
        assertEquals("2", formatarPrioridadeAlerta("2a3"))
        assertEquals("", formatarPrioridadeAlerta("x"))
    }

    @Test
    fun validFormConvertsToBackendValues() {
        val data =
            formValido()
                .copy(titulo = " Alerta ", mensagem = "  ", prioridade = "1", monitorarUmidadeSolo = true)
                .toAlertaCadastroDataOrNull()

        assertEquals(3L, data?.dispositivoIotId)
        assertEquals(7L, data?.especieId)
        assertEquals("Alerta", data?.titulo)
        assertNull(data?.mensagem)
        assertEquals(1, data?.prioridade)
        assertTrue(data?.monitorarTemperatura == true)
        assertTrue(data?.monitorarUmidadeSolo == true)
        assertFalse(data?.monitorarUmidadeAr ?: true)
    }

    @Test
    fun requiredFieldsAreReported() {
        val errors = validarAlertaCadastro(AlertaCadastroForm())

        assertEquals("O ID do dispositivo IoT é obrigatório", errors["dispositivoIotId"])
        assertEquals("O ID da espécie é obrigatório", errors["especieId"])
        assertEquals("O título do alerta é obrigatório", errors["titulo"])
        assertFalse(errors.containsKey("prioridade"))
        assertFalse(errors.containsKey("condicoes"))
    }

    @Test
    fun idsMustBeGreaterThanZero() {
        val errors = validarAlertaCadastro(formValido().copy(dispositivoIotId = "0", especieId = "0"))

        assertTrue(errors.containsKey("dispositivoIotId"))
        assertTrue(errors.containsKey("especieId"))
    }

    @Test
    fun priorityMustBeOneTwoOrThree() {
        assertTrue(validarAlertaCadastro(formValido().copy(prioridade = "0")).containsKey("prioridade"))
        assertTrue(validarAlertaCadastro(formValido().copy(prioridade = "4")).containsKey("prioridade"))
        assertTrue(validarAlertaCadastro(formValido().copy(prioridade = "")).containsKey("prioridade"))
        assertFalse(validarAlertaCadastro(formValido().copy(prioridade = "3")).containsKey("prioridade"))
    }

    @Test
    fun atLeastOneConditionMustBeMonitored() {
        val errors = validarAlertaCadastro(formValido().copy(monitorarTemperatura = false))

        assertEquals("Selecione ao menos uma condição monitorada", errors["condicoes"])
    }

    @Test
    fun lengthLimitsAreEnforced() {
        val errors =
            validarAlertaCadastro(formValido().copy(titulo = "x".repeat(151), mensagem = "x".repeat(1001)))

        assertEquals("O título deve ter no máximo 150 caracteres", errors["titulo"])
        assertEquals("A mensagem deve ter no máximo 1000 caracteres", errors["mensagem"])
    }

    @Test
    fun invalidFormDoesNotConvertToData() {
        assertNull(AlertaCadastroForm().toAlertaCadastroDataOrNull())
    }
}

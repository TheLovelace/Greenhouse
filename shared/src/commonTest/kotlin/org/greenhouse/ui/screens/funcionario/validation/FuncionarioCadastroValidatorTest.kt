package org.greenhouse.ui.screens.funcionario.validation

import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FuncionarioCadastroValidatorTest {
    @Test
    fun validFormConvertsOptionalValuesToNull() {
        val data = FuncionarioCadastroForm(
            nome = " Luna ",
            cpf = "12345678901",
            email = "luna@example.com",
        ).toFuncionarioCadastroDataOrNull()

        assertEquals("Luna", data?.nome)
        assertEquals(null, data?.telefone)
        assertEquals(null, data?.cargo)
        assertEquals(null, data?.dataDeNascimento)
        assertTrue(data?.ativo == true)
    }

    @Test
    fun validationRejectsBlankNameInvalidCpfAndMissingEmail() {
        val errors = validarFuncionarioCadastro(FuncionarioCadastroForm())

        assertTrue(errors.containsKey("nome"))
        assertTrue(errors.containsKey("cpf"))
        assertTrue(errors.containsKey("email"))
        assertNull(FuncionarioCadastroForm().toFuncionarioCadastroDataOrNull())
    }

    @Test
    fun validationChecksFieldSizesAndOptionalBirthDate() {
        val errors = validarFuncionarioCadastro(
            FuncionarioCadastroForm(
                nome = "N".repeat(151),
                cpf = "12345678901",
                email = "luna@example.com",
                telefone = "1".repeat(21),
                cargo = "C".repeat(101),
                dataDeNascimento = "2025-02-29",
            ),
        )

        assertEquals(setOf("nome", "telefone", "cargo", "dataDeNascimento"), errors.keys)
    }

    @Test
    fun validationAcceptsLeapDayAndRejectsMalformedEmail() {
        assertTrue(
            validarFuncionarioCadastro(
                FuncionarioCadastroForm(
                    nome = "Luna",
                    cpf = "12345678901",
                    email = "luna@example.com",
                    dataDeNascimento = "2024-02-29",
                ),
            ).isEmpty(),
        )
        assertTrue(
            validarFuncionarioCadastro(
                FuncionarioCadastroForm(
                    nome = "Luna",
                    cpf = "12345678901",
                    email = "invalido",
                ),
            ).containsKey("email"),
        )
    }
}

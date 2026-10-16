package org.greenhouse.ui.screens.fornecedor.validation

import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FornecedorCadastroValidatorTest {
    @Test
    fun cnpjInputKeepsOnlyFourteenDigits() {
        assertEquals("12345678901234", formatarCnpj("12.345.678/9012-34"))
        assertEquals("12345678901234", formatarCnpj("123456789012345"))
        assertEquals("", formatarCnpj("abc"))
    }

    @Test
    fun digitInputKeepsOnlyDigits() {
        assertEquals("120", formatarDigitosFornecedor("1a2-0"))
        assertEquals("", formatarDigitosFornecedor("abc"))
    }

    @Test
    fun validFormConvertsToCreateData() {
        val data =
            requireNotNull(
                FornecedorCadastroForm(
                    nome = " Flores do Campo ",
                    cnpj = "12345678901234",
                    telefone = "11999999999",
                    email = "flores@example.com",
                    contato = "Maria",
                    ativo = false,
                    prazoMedioEntregaDias = "10",
                ).toFornecedorCadastroDataOrNull(),
            )

        assertEquals("Flores do Campo", data.nome)
        assertEquals("12345678901234", data.cnpj)
        assertEquals("11999999999", data.telefone)
        assertEquals("flores@example.com", data.email)
        assertEquals("Maria", data.contato)
        assertFalse(data.ativo)
        assertEquals(10, data.prazoMedioEntregaDias)
    }

    @Test
    fun blankOptionalValuesConvertToNull() {
        val data =
            FornecedorCadastroForm(nome = "Fornecedor")
                .toFornecedorCadastroDataOrNull()

        assertNull(data?.cnpj)
        assertNull(data?.telefone)
        assertNull(data?.email)
        assertNull(data?.contato)
        assertNull(data?.prazoMedioEntregaDias)
        assertTrue(data?.ativo == true)
    }

    @Test
    fun validationRejectsMissingOrLongName() {
        assertTrue(validarFornecedorCadastro(FornecedorCadastroForm()).containsKey("nome"))
        assertTrue(
            validarFornecedorCadastro(
                FornecedorCadastroForm(nome = "a".repeat(151)),
            ).containsKey("nome"),
        )
        assertNull(
            FornecedorCadastroForm(nome = " ")
                .toFornecedorCadastroDataOrNull(),
        )
    }

    @Test
    fun validationRejectsInvalidCnpj() {
        val errors =
            validarFornecedorCadastro(
                FornecedorCadastroForm(nome = "Fornecedor", cnpj = "123"),
            )

        assertEquals("O CNPJ deve conter exatamente 14 dígitos numéricos", errors["cnpj"])
    }

    @Test
    fun validationRejectsInvalidEmailAndLongFields() {
        val errors =
            validarFornecedorCadastro(
                FornecedorCadastroForm(
                    nome = "Fornecedor",
                    email = "nao-e-email",
                    telefone = "1".repeat(21),
                    contato = "c".repeat(151),
                ),
            )

        assertTrue(errors.containsKey("email"))
        assertTrue(errors.containsKey("telefone"))
        assertTrue(errors.containsKey("contato"))
    }

    @Test
    fun validationRejectsInvalidLeadTime() {
        val errors =
            validarFornecedorCadastro(
                FornecedorCadastroForm(
                    nome = "Fornecedor",
                    prazoMedioEntregaDias = "99999999999999999999",
                ),
            )

        assertTrue(errors.containsKey("prazoMedioEntregaDias"))
    }
}

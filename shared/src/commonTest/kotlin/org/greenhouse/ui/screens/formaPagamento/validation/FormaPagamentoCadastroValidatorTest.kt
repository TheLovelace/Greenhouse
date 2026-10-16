package org.greenhouse.ui.screens.formaPagamento.validation

import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormaPagamentoCadastroValidatorTest {
    @Test
    fun validFormConvertsNumbersAndOptionalDescription() {
        val data = FormaPagamentoCadastroForm(
            nome = " Cartão ",
            limiteParcelas = "6",
            prazoEstornoDias = "15",
        ).toFormaPagamentoCadastroDataOrNull()

        assertEquals("Cartão", data?.nome)
        assertEquals(6, data?.limiteParcelas)
        assertEquals(15, data?.prazoEstornoDias)
        assertEquals(null, data?.descricao)
    }

    @Test
    fun validationRejectsBlankNameAndInvalidParcelLimit() {
        val form = FormaPagamentoCadastroForm(nome = "", limiteParcelas = "0")
        val errors = validarFormaPagamentoCadastro(form)

        assertTrue(errors.containsKey("nome"))
        assertTrue(errors.containsKey("limiteParcelas"))
        assertNull(form.toFormaPagamentoCadastroDataOrNull())
    }

    @Test
    fun validationChecksMaximumLengthsAndOptionalRefundDays() {
        val errors = validarFormaPagamentoCadastro(
            FormaPagamentoCadastroForm(
                nome = "N".repeat(51),
                descricao = "D".repeat(251),
                prazoEstornoDias = "-1",
            ),
        )

        assertEquals(setOf("nome", "descricao", "prazoEstornoDias"), errors.keys)
    }

    @Test
    fun blankOptionalRefundPeriodMapsToNullAndNumberInputKeepsDigits() {
        val data = FormaPagamentoCadastroForm(
            nome = "Pix",
            limiteParcelas = "1",
            prazoEstornoDias = "",
        ).toFormaPagamentoCadastroDataOrNull()

        assertEquals(null, data?.prazoEstornoDias)
        assertEquals("123", formatarNumeroFormaPagamento("1a-2 3"))
    }
}

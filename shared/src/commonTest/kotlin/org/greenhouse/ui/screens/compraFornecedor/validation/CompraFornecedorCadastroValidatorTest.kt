package org.greenhouse.ui.screens.compraFornecedor.validation

import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroForm
import org.greenhouse.ui.screens.compraFornecedor.model.CompraItemForm
import org.greenhouse.ui.screens.compraFornecedor.model.StatusCompraCadastro
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CompraFornecedorCadastroValidatorTest {
    @Test
    fun digitInputKeepsOnlyDigits() {
        assertEquals("120", formatarDigitosCompra("1a2-0"))
        assertEquals("", formatarDigitosCompra("abc"))
    }

    @Test
    fun dateInputFormatsAsBrazilianDate() {
        assertEquals("16/10/2026", formatarDataCompra("16102026"))
        assertEquals("16/1", formatarDataCompra("161"))
    }

    @Test
    fun moneyInputKeepsDigitsAndTwoDecimalPlaces() {
        assertEquals("10,50", formatarValorCompra("10,50"))
        assertEquals("10.50", formatarValorCompra("10.50"))
        assertEquals("10,50", formatarValorCompra("10,509"))
        assertEquals("10,", formatarValorCompra("10,"))
        assertEquals("1234", formatarValorCompra("1a2b3c4"))
    }

    @Test
    fun validFormConvertsToCreateDataWithCalculatedTotal() {
        val data =
            formularioValido()
                .copy(
                    valorFrete = "5,00",
                    observacao = "Reposição semanal",
                ).toCompraFornecedorCadastroDataOrNull()

        assertEquals(1L, data?.funcionarioId)
        assertEquals(2L, data?.fornecedorId)
        assertEquals(3L, data?.formaPagamentoId)
        assertEquals(StatusCompraCadastro.PEDIDA, data?.status)
        assertEquals("35.00", data?.valorTotal)
        assertEquals("5.00", data?.valorFrete)
        assertEquals("Reposição semanal", data?.observacao)
        assertEquals(1, data?.itens?.size)
        assertEquals("10.00", data?.itens?.first()?.valorUnitario)
    }

    @Test
    fun freightIsNotAddedToTotalWhenIncluded() {
        val data =
            formularioValido()
                .copy(valorFrete = "5,00", freteIncluso = true)
                .toCompraFornecedorCadastroDataOrNull()

        assertEquals("30.00", data?.valorTotal)
    }

    @Test
    fun datesConvertToIsoFormat() {
        val data =
            formularioValido()
                .copy(dataCompra = "16/10/2026", dataEntregaPrevista = "20/10/2026")
                .toCompraFornecedorCadastroDataOrNull()

        assertEquals("2026-10-16", data?.dataCompra)
        assertEquals("2026-10-20", data?.dataEntregaPrevista)
    }

    @Test
    fun validationRejectsMissingRequiredIds() {
        val errors = validarCompraFornecedorCadastro(CompraFornecedorCadastroForm())

        assertTrue(errors.containsKey("funcionarioId"))
        assertTrue(errors.containsKey("fornecedorId"))
        assertTrue(errors.containsKey("formaPagamentoId"))
    }

    @Test
    fun validationRejectsInvalidDates() {
        val errors =
            validarCompraFornecedorCadastro(
                formularioValido().copy(
                    dataCompra = "32/13/2026",
                    dataEntregaPrevista = "29/02/2025",
                ),
            )

        assertTrue(errors.containsKey("dataCompra"))
        assertTrue(errors.containsKey("dataEntregaPrevista"))
    }

    @Test
    fun validationRejectsInvalidItemFields() {
        val errors =
            validarCompraFornecedorCadastro(
                formularioValido().copy(
                    itens = listOf(CompraItemForm(produtoId = "", quantidade = "0", valorUnitario = "")),
                ),
            )

        assertTrue(errors.containsKey("itens[0].produtoId"))
        assertTrue(errors.containsKey("itens[0].quantidade"))
        assertTrue(errors.containsKey("itens[0].valorUnitario"))
    }

    @Test
    fun validationRejectsDuplicatedProductInItems() {
        val errors =
            validarCompraFornecedorCadastro(
                formularioValido().copy(
                    itens =
                        listOf(
                            CompraItemForm(produtoId = "7", quantidade = "1", valorUnitario = "10,00"),
                            CompraItemForm(produtoId = "7", quantidade = "2", valorUnitario = "10,00"),
                        ),
                ),
            )

        assertEquals("O produto já foi adicionado à compra", errors["itens[1].produtoId"])
    }

    @Test
    fun validationRejectsLongObservation() {
        val errors =
            validarCompraFornecedorCadastro(
                formularioValido().copy(observacao = "a".repeat(1001)),
            )

        assertTrue(errors.containsKey("observacao"))
    }

    @Test
    fun calculatedTotalIsNullWhenItemsAreInvalid() {
        assertNull(
            calcularValorTotalCompra(
                formularioValido().copy(
                    itens = listOf(CompraItemForm(produtoId = "7", quantidade = "", valorUnitario = "10,00")),
                ),
            ),
        )
    }

    @Test
    fun initialFormHasSingleEmptyItemAndPedidaStatus() {
        val form = CompraFornecedorCadastroForm()

        assertEquals(1, form.itens.size)
        assertEquals(StatusCompraCadastro.PEDIDA, form.status)
        assertNull(form.toCompraFornecedorCadastroDataOrNull())
    }

    private fun formularioValido() =
        CompraFornecedorCadastroForm(
            funcionarioId = "1",
            fornecedorId = "2",
            formaPagamentoId = "3",
            itens =
                listOf(
                    CompraItemForm(produtoId = "7", quantidade = "3", valorUnitario = "10,00"),
                ),
        )
}

package org.greenhouse.unit.repository

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.json.Json
import org.greenhouse.app.model.CompraFornecedor
import org.greenhouse.app.model.CompraItem
import org.greenhouse.app.viewmodel.toCreateRequest
import org.greenhouse.app.viewmodel.toUpdateRequest
import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraFornecedorUpdateRequestDTO
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.compraFornecedor.model.CompraItemCadastroData
import org.greenhouse.ui.screens.compraFornecedor.model.StatusCompraCadastro
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CompraFornecedorContratoTest {
    private val jackson = ObjectMapper().findAndRegisterModules()
    private val dados =
        CompraFornecedorCadastroData(
            funcionarioId = 1,
            fornecedorId = 2,
            formaPagamentoId = 3,
            dataCompra = "2026-11-26",
            status = StatusCompraCadastro.PEDIDA,
            valorTotal = "9999999999.99",
            valorFrete = "10.50",
            freteIncluso = true,
            dataEntregaPrevista = "2026-11-30",
            observacao = null,
            itens = listOf(CompraItemCadastroData(produtoId = 4, quantidade = 1, valorUnitario = "9999999999.99")),
        )

    @Test
    fun `payload de cadastro preserva datas e centavos no DTO do backend`() {
        val payload = Json.encodeToString(dados.toCreateRequest())
        val request = jackson.readValue(payload, CompraFornecedorCreateRequestDTO::class.java)

        assertEquals(OffsetDateTime.parse("2026-11-26T00:00:00Z"), request.dataCompra)
        assertEquals(LocalDate.parse("2026-11-30"), request.dataEntregaPrevista)
        assertEquals(0, BigDecimal("10.50").compareTo(request.valorFrete))
        assertEquals(0, BigDecimal("9999999999.99").compareTo(request.itens.single().valorUnitario))
    }

    @Test
    fun `edicao preserva horario e referencias quando apenas a observacao muda`() {
        val atual =
            CompraFornecedor(
                id = 1,
                funcionarioId = 1,
                fornecedorId = 2,
                formaPagamentoId = 3,
                dataCompra = "2026-11-26T14:35:12Z",
                status = StatusCompraCadastro.PEDIDA,
                valorTotal = 9999999999.99,
                valorFrete = 10.5,
                freteIncluso = true,
                dataEntregaPrevista = "2026-11-30",
                observacao = "Observação antiga",
                itens = listOf(CompraItem(1, 4, 1, 9999999999.99)),
            )
        val payload = Json.encodeToString(dados.toUpdateRequest(atual))
        val request = jackson.readValue(payload, CompraFornecedorUpdateRequestDTO::class.java)

        assertEquals("", request.observacao)
        assertNull(request.dataCompra)
        assertNull(request.dataEntregaPrevista)
        assertNull(request.funcionarioId)
        assertNull(request.fornecedorId)
        assertNull(request.formaPagamentoId)
        assertNull(request.itens)
        assertNull(request.valorFrete)
    }
}

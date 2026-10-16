package org.greenhouse.ui.screens.pedido.validation

import org.greenhouse.ui.screens.pedido.model.ModalidadePedidoCadastro
import org.greenhouse.ui.screens.pedido.model.PedidoCadastroForm
import org.greenhouse.ui.screens.pedido.model.PedidoItemForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

class PedidoCadastroValidatorTest {
    @Test
    fun `formatadores removem nao digitos e aplicam mascara de data`() {
        assertEquals("123", formatarDigitos("1a2-3"))
        assertEquals("16/10/2026", formatarDataBrasileira("16102026"))
        assertEquals("16/10", formatarDataBrasileira("1610"))
    }

    @Test
    fun `exige funcionario, forma de pagamento e ao menos um item`() {
        val errors = validarPedidoCadastro(PedidoCadastroForm())

        assertTrue(errors.containsKey("funcionarioId"))
        assertTrue(errors.containsKey("formaPagamentoId"))
        assertTrue(errors.containsKey("itens[0].produtoId"))
        assertTrue(errors.containsKey("itens[0].quantidade"))
    }

    @Test
    fun `aceita pedido imediato sem data e normaliza campos para Data`() {
        val data =
            PedidoCadastroForm(
                funcionarioId = "3",
                formaPagamentoId = "2",
                itens = listOf(PedidoItemForm(produtoId = "10", quantidade = "2")),
            ).toPedidoCadastroDataOrNull()

        assertEquals(3L, data?.funcionarioId)
        assertEquals(2L, data?.formaPagamentoId)
        assertNull(data?.clienteId)
        assertNull(data?.descontoId)
        assertNull(data?.dataRetiradaPrevista)
        assertEquals(ModalidadePedidoCadastro.IMEDIATA, data?.modalidade)
        assertEquals(1, data?.itens?.size)
        assertEquals(10L, data?.itens?.first()?.produtoId)
        assertEquals(2, data?.itens?.first()?.quantidade)
    }

    @Test
    fun `converte data brasileira para offset date time`() {
        val data =
            PedidoCadastroForm(
                funcionarioId = "3",
                formaPagamentoId = "2",
                modalidade = ModalidadePedidoCadastro.AGENDADA,
                dataRetiradaPrevista = "01/01/2099",
                itens = listOf(PedidoItemForm(produtoId = "10", quantidade = "1")),
            ).toPedidoCadastroDataOrNull()

        assertEquals("2099-01-01T00:00:00Z", data?.dataRetiradaPrevista)
    }

    @Test
    fun `exige data valida e nao anterior a hoje quando agendado`() {
        val base =
            PedidoCadastroForm(
                funcionarioId = "3",
                formaPagamentoId = "2",
                modalidade = ModalidadePedidoCadastro.AGENDADA,
                itens = listOf(PedidoItemForm(produtoId = "10", quantidade = "1")),
            )

        assertTrue(validarPedidoCadastro(base).containsKey("dataRetiradaPrevista"))

        val malformada = base.copy(dataRetiradaPrevista = "31/02/2026")
        assertTrue(validarPedidoCadastro(malformada).containsKey("dataRetiradaPrevista"))

        val passada = base.copy(dataRetiradaPrevista = "01/01/2000")
        assertTrue(validarPedidoCadastro(passada).containsKey("dataRetiradaPrevista"))

        val hoje = Clock.System.now().toString().take(10)
        val dataHoje = "${hoje.substring(8, 10)}/${hoje.substring(5, 7)}/${hoje.substring(0, 4)}"
        assertTrue(validarPedidoCadastro(base.copy(dataRetiradaPrevista = dataHoje)).isEmpty())

        assertTrue(validarPedidoCadastro(base.copy(dataRetiradaPrevista = "01/01/2099")).isEmpty())
    }

    @Test
    fun `rejeita ids invalidos, itens duplicados, volumes e observacao fora dos limites`() {
        val errors =
            validarPedidoCadastro(
                PedidoCadastroForm(
                    clienteId = "abc",
                    funcionarioId = "0",
                    formaPagamentoId = "-1",
                    descontoId = "x",
                    observacao = "a".repeat(1001),
                    numeroVolumes = "0",
                    itens =
                        listOf(
                            PedidoItemForm(produtoId = "10", quantidade = "1"),
                            PedidoItemForm(produtoId = "10", quantidade = "2"),
                        ),
                ),
            )

        assertTrue(errors.containsKey("clienteId"))
        assertTrue(errors.containsKey("funcionarioId"))
        assertTrue(errors.containsKey("formaPagamentoId"))
        assertTrue(errors.containsKey("descontoId"))
        assertTrue(errors.containsKey("observacao"))
        assertTrue(errors.containsKey("numeroVolumes"))
        assertTrue(errors.containsKey("itens[1].produtoId"))
    }

    @Test
    fun `nao gera Data quando ha erros`() {
        assertNull(PedidoCadastroForm().toPedidoCadastroDataOrNull())
    }
}

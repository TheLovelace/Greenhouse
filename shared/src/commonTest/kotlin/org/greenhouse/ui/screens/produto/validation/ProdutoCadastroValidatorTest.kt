package org.greenhouse.ui.screens.produto.validation

import org.greenhouse.ui.screens.produto.model.ProdutoCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProdutoCadastroValidatorTest {
    @Test
    fun `formatadores mantem digitos validos de preco e prazo de preparo`() {
        assertEquals("12,50", formatarPrecoProduto("a1b2,5x0,75"))
        assertEquals("120", formatarPrazoPreparoProduto("1a2-0"))
    }

    @Test
    fun `remove espacos dos campos e converte preco brasileiro`() {
        val data =
            ProdutoCadastroForm(
                codigo = "  FL-01  ",
                nome = "  Rosa  ",
                descricao = "  Planta ornamental  ",
                preco = "25,50",
                prazoPreparoMinutos = "15",
                ativo = false,
            ).toProdutoCadastroDataOrNull()

        assertEquals("FL-01", data?.codigo)
        assertEquals("Rosa", data?.nome)
        assertEquals("Planta ornamental", data?.descricao)
        assertEquals("25.50", data?.preco)
        assertEquals(15, data?.prazoPreparoMinutos)
        assertEquals(false, data?.ativo)
    }

    @Test
    fun `converte campos opcionais vazios para nulo`() {
        val data =
            ProdutoCadastroForm(
                nome = "Vaso",
                preco = "0",
            ).toProdutoCadastroDataOrNull()

        assertNull(data?.codigo)
        assertNull(data?.descricao)
        assertNull(data?.prazoPreparoMinutos)
    }

    @Test
    fun `exige nome e preco`() {
        val errors = validarProdutoCadastro(ProdutoCadastroForm())

        assertTrue(errors.containsKey("nome"))
        assertTrue(errors.containsKey("preco"))
    }

    @Test
    fun `rejeita preco invalido e campos fora dos limites`() {
        val errors =
            validarProdutoCadastro(
                ProdutoCadastroForm(
                    codigo = "c".repeat(51),
                    nome = "n".repeat(151),
                    descricao = "d".repeat(1001),
                    preco = "10000000000",
                    prazoPreparoMinutos = "2147483648",
                ),
            )

        assertTrue(errors.containsKey("codigo"))
        assertTrue(errors.containsKey("nome"))
        assertTrue(errors.containsKey("descricao"))
        assertTrue(errors.containsKey("preco"))
        assertTrue(errors.containsKey("prazoPreparoMinutos"))
    }
}

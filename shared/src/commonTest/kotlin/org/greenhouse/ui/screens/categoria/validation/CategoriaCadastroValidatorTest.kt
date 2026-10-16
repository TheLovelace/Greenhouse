package org.greenhouse.ui.screens.categoria.validation

import org.greenhouse.ui.screens.categoria.model.CategoriaCadastroForm
import org.greenhouse.ui.screens.desconto.validation.formatarDataBrasileira
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CategoriaCadastroValidatorTest {
    @Test
    fun `remove espacos dos campos e converte observacao vazia para nulo`() {
        val data =
            CategoriaCadastroForm(
                nome = "  Flores  ",
                descricao = "  Plantas floridas  ",
                observacao = "   ",
                ativo = false,
                dataCadastro = "29/02/2024",
            ).toCategoriaCadastroDataOrNull()

        assertEquals("Flores", data?.nome)
        assertEquals("Plantas floridas", data?.descricao)
        assertNull(data?.observacao)
        assertEquals(false, data?.ativo)
        assertEquals("2024-02-29", data?.dataCadastro)
    }

    @Test
    fun `exige nome e descricao`() {
        val errors = validarCategoriaCadastro(CategoriaCadastroForm())

        assertTrue(errors.containsKey("nome"))
        assertTrue(errors.containsKey("descricao"))
    }

    @Test
    fun `rejeita valores acima dos limites do backend`() {
        val errors =
            validarCategoriaCadastro(
                CategoriaCadastroForm(
                    nome = "n".repeat(101),
                    descricao = "d".repeat(1001),
                    observacao = "o".repeat(301),
                ),
            )

        assertTrue(errors.containsKey("nome"))
        assertTrue(errors.containsKey("descricao"))
        assertTrue(errors.containsKey("observacao"))
    }

    @Test
    fun `valida data de cadastro no formato e calendario ISO`() {
        val dataInvalida =
            validarCategoriaCadastro(
                CategoriaCadastroForm(
                    nome = "Flores",
                    descricao = "Plantas floridas",
                    dataCadastro = "29/02/2023",
                ),
            )
        val dataValida =
            validarCategoriaCadastro(
                CategoriaCadastroForm(
                    nome = "Flores",
                    descricao = "Plantas floridas",
                    dataCadastro = "29/02/2024",
                ),
            )

        assertTrue(dataInvalida.containsKey("dataCadastro"))
        assertTrue(dataValida.isEmpty())
    }

    @Test
    fun `mascara data de cadastro como data brasileira`() {
        assertEquals("31/12/2026", formatarDataBrasileira("31122026"))
        assertEquals("31/12/2026", formatarDataBrasileira("31/12/2026"))
    }
}

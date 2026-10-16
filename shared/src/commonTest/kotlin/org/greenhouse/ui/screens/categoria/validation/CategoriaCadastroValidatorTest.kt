package org.greenhouse.ui.screens.categoria.validation

import org.greenhouse.ui.screens.categoria.model.CategoriaCadastroForm
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
            ).toCategoriaCadastroDataOrNull()

        assertEquals("Flores", data?.nome)
        assertEquals("Plantas floridas", data?.descricao)
        assertNull(data?.observacao)
        assertEquals(false, data?.ativo)
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
}

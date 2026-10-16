package org.greenhouse.ui.screens.cliente.validation

import org.greenhouse.ui.screens.cliente.model.ClienteCadastroForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClienteCadastroValidatorTest {
    @Test
    fun `formatador de CPF mantem apenas digitos e limita a 11`() {
        assertEquals("12345678901", formatarCpf("123.456.789-01"))
        assertEquals("12345678901", formatarCpf("123456789012345"))
    }

    @Test
    fun `remove espacos dos campos e converte opcionais vazios para nulo`() {
        val data =
            ClienteCadastroForm(
                nome = "  Maria Silva  ",
                telefone = "  (11) 98765-4321  ",
                whatsapp = true,
                cpf = "",
                email = "",
                ativo = true,
            ).toClienteCadastroDataOrNull()

        assertEquals("Maria Silva", data?.nome)
        assertEquals("(11) 98765-4321", data?.telefone)
        assertEquals(true, data?.whatsapp)
        assertNull(data?.cpf)
        assertNull(data?.email)
        assertEquals(true, data?.ativo)
    }

    @Test
    fun `exige nome e telefone`() {
        val errors = validarClienteCadastro(ClienteCadastroForm())

        assertTrue(errors.containsKey("nome"))
        assertTrue(errors.containsKey("telefone"))
    }

    @Test
    fun `rejeita campos fora dos limites`() {
        val errors =
            validarClienteCadastro(
                ClienteCadastroForm(
                    nome = "a".repeat(151),
                    telefone = "1".repeat(21),
                    email = "${"a".repeat(145)}@exemplo.com",
                ),
            )

        assertTrue(errors.containsKey("nome"))
        assertTrue(errors.containsKey("telefone"))
        assertTrue(errors.containsKey("email"))
    }

    @Test
    fun `aceita CPF e email validos e rejeita invalidos`() {
        val validos =
            validarClienteCadastro(
                ClienteCadastroForm(
                    nome = "Maria",
                    telefone = "(11) 98765-4321",
                    cpf = "12345678901",
                    email = "maria@exemplo.com",
                ),
            )
        assertTrue(validos.isEmpty())

        val cpfInvalido =
            validarClienteCadastro(
                ClienteCadastroForm(nome = "Maria", telefone = "123", cpf = "1234567890"),
            )
        assertTrue(cpfInvalido.containsKey("cpf"))

        val emailInvalido =
            validarClienteCadastro(
                ClienteCadastroForm(nome = "Maria", telefone = "123", email = "maria-sem-arroba"),
            )
        assertTrue(emailInvalido.containsKey("email"))
    }

    @Test
    fun `nao gera Data quando ha erros`() {
        assertNull(ClienteCadastroForm(nome = "Maria").toClienteCadastroDataOrNull())
    }
}

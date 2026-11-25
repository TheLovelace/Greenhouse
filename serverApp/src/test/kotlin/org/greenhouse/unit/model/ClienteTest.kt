package org.greenhouse.unit.model

import java.time.LocalDate
import org.greenhouse.model.Cliente
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE Cliente.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class ClienteTest {
    @Test
    fun `deve guardar os dados alterados do cliente`() {
        val cliente = Cliente()
        val idNovo = 1L
        val nomeNovo = "Maria Souza"
        val telefoneNovo = "11987654321"
        val whatsappNovo = true
        val cpfNovo = "12345678901"
        val emailNovo = "maria@email.com"
        val ativoNovo = false
        val dataNascimentoNovo = LocalDate.of(1990, 5, 20)

        cliente.id = idNovo
        cliente.nome = nomeNovo
        cliente.telefone = telefoneNovo
        cliente.whatsapp = whatsappNovo
        cliente.cpf = cpfNovo
        cliente.email = emailNovo
        cliente.ativo = ativoNovo
        cliente.dataNascimento = dataNascimentoNovo

        assertAll(
                "campos do cliente",
                { assertEquals(idNovo, cliente.id, "id") },
                { assertEquals(nomeNovo, cliente.nome, "nome") },
                { assertEquals(telefoneNovo, cliente.telefone, "telefone") },
                { assertEquals(whatsappNovo, cliente.whatsapp, "indicação de WhatsApp") },
                { assertEquals(cpfNovo, cliente.cpf, "CPF") },
                { assertEquals(emailNovo, cliente.email, "e-mail") },
                { assertEquals(ativoNovo, cliente.ativo, "status ativo") },
                { assertEquals(dataNascimentoNovo, cliente.dataNascimento, "data de nascimento") },
        )
    }
}

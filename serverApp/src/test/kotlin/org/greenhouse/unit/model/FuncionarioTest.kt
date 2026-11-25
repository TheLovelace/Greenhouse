package org.greenhouse.unit.model

import java.time.LocalDate
import org.greenhouse.model.Funcionario
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE Funcionario.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class FuncionarioTest {
    @Test
    fun `deve guardar os dados alterados do funcionario`() {
        val funcionario = Funcionario()
        val idNovo = 4L
        val nomeNovo = "Ana Lima"
        val cpfNovo = "98765432100"
        val telefoneNovo = "11912345678"
        val emailNovo = "ana@greenhouse.com"
        val cargoNovo = "Florista"
        val ativoNovo = false
        val dataDeNascimentoNovo = LocalDate.of(1995, 3, 8)

        funcionario.id = idNovo
        funcionario.nome = nomeNovo
        funcionario.cpf = cpfNovo
        funcionario.telefone = telefoneNovo
        funcionario.email = emailNovo
        funcionario.cargo = cargoNovo
        funcionario.ativo = ativoNovo
        funcionario.dataDeNascimento = dataDeNascimentoNovo

        assertAll(
                "campos do funcionário",
                { assertEquals(idNovo, funcionario.id, "id") },
                { assertEquals(nomeNovo, funcionario.nome, "nome") },
                { assertEquals(cpfNovo, funcionario.cpf, "CPF") },
                { assertEquals(telefoneNovo, funcionario.telefone, "telefone") },
                { assertEquals(emailNovo, funcionario.email, "e-mail") },
                { assertEquals(cargoNovo, funcionario.cargo, "cargo") },
                { assertEquals(ativoNovo, funcionario.ativo, "status ativo") },
                { assertEquals(dataDeNascimentoNovo, funcionario.dataDeNascimento, "data de nascimento") },
        )
    }
}

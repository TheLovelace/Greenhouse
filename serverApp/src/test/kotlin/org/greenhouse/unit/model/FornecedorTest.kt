package org.greenhouse.unit.model

import org.greenhouse.model.Fornecedor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE Fornecedor.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class FornecedorTest {
    @Test
    fun `deve guardar os dados alterados do fornecedor`() {
        val fornecedor = Fornecedor()
        val idNovo = 5L
        val nomeNovo = "Flora Distribuidora"
        val cnpjNovo = "12345678000199"
        val telefoneNovo = "1133334444"
        val emailNovo = "vendas@flora.com"
        val contatoNovo = "Carlos"
        val ativoNovo = false
        val prazoMedioEntregaDiasNovo = 7

        fornecedor.id = idNovo
        fornecedor.nome = nomeNovo
        fornecedor.cnpj = cnpjNovo
        fornecedor.telefone = telefoneNovo
        fornecedor.email = emailNovo
        fornecedor.contato = contatoNovo
        fornecedor.ativo = ativoNovo
        fornecedor.prazoMedioEntregaDias = prazoMedioEntregaDiasNovo

        assertAll(
                "campos do fornecedor",
                { assertEquals(idNovo, fornecedor.id, "id") },
                { assertEquals(nomeNovo, fornecedor.nome, "nome") },
                { assertEquals(cnpjNovo, fornecedor.cnpj, "CNPJ") },
                { assertEquals(telefoneNovo, fornecedor.telefone, "telefone") },
                { assertEquals(emailNovo, fornecedor.email, "e-mail") },
                { assertEquals(contatoNovo, fornecedor.contato, "contato") },
                { assertEquals(ativoNovo, fornecedor.ativo, "status ativo") },
                { assertEquals(prazoMedioEntregaDiasNovo, fornecedor.prazoMedioEntregaDias, "prazo médio de entrega") },
        )
    }
}

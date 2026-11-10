package org.greenhouse.unit.model

import java.time.LocalDate
import org.greenhouse.model.Categoria
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE Categoria.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class CategoriaTest {
    @Test
    fun `deve guardar os dados alterados da categoria`() {
        val categoria = Categoria()
        val idNovo = 9L
        val nomeNovo = "Suculentas"
        val descricaoNovo = "Plantas de pouca rega"
        val ativoNovo = false
        val observacaoNovo = "Destaque na vitrine"
        val dataCadastroNovo = LocalDate.of(2026, 1, 15)

        categoria.id = idNovo
        categoria.nome = nomeNovo
        categoria.descricao = descricaoNovo
        categoria.ativo = ativoNovo
        categoria.observacao = observacaoNovo
        categoria.dataCadastro = dataCadastroNovo

        assertAll(
                "campos da categoria",
                { assertEquals(idNovo, categoria.id, "id") },
                { assertEquals(nomeNovo, categoria.nome, "nome") },
                { assertEquals(descricaoNovo, categoria.descricao, "descrição") },
                { assertEquals(ativoNovo, categoria.ativo, "status ativo") },
                { assertEquals(observacaoNovo, categoria.observacao, "observação") },
                { assertEquals(dataCadastroNovo, categoria.dataCadastro, "data de cadastro") },
        )
    }
}

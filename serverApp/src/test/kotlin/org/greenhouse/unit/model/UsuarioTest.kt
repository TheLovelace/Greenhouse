package org.greenhouse.unit.model

import org.greenhouse.model.Usuario
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Testes de unidade da ENTIDADE Usuario.
 *
 * Os campos da entidade são alterados pelo JPA e pelos services ao atualizar o registro. O teste
 * garante que cada campo mapeado guarda o valor atribuído. Não utiliza mocks.
 */
class UsuarioTest {
    @Test
    fun `deve guardar os dados alterados do usuario`() {
        val usuario = Usuario()
        val idNovo = 10L
        val funcionarioIdNovo = 4L
        val loginNovo = "maria"
        val senhaHashNovo = "\$2a\$10\$hash"
        val perfilNovo = "ADMIN"
        val ativoNovo = false
        val primeiroAcessoFeitoNovo = true

        usuario.id = idNovo
        usuario.funcionarioId = funcionarioIdNovo
        usuario.login = loginNovo
        usuario.senhaHash = senhaHashNovo
        usuario.perfil = perfilNovo
        usuario.ativo = ativoNovo
        usuario.primeiroAcessoFeito = primeiroAcessoFeitoNovo

        assertAll(
                "campos do usuário",
                { assertEquals(idNovo, usuario.id, "id") },
                { assertEquals(funcionarioIdNovo, usuario.funcionarioId, "funcionário") },
                { assertEquals(loginNovo, usuario.login, "login") },
                { assertEquals(senhaHashNovo, usuario.senhaHash, "hash da senha") },
                { assertEquals(perfilNovo, usuario.perfil, "perfil") },
                { assertEquals(ativoNovo, usuario.ativo, "status ativo") },
                { assertEquals(primeiroAcessoFeitoNovo, usuario.primeiroAcessoFeito, "primeiro acesso concluído") },
        )
    }
}

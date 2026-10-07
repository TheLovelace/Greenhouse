package org.greenhouse.usecase

import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Funcionario
import org.greenhouse.repository.FuncionarioRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class FuncionarioProvisioningTest {

    private val repository = Mockito.mock(FuncionarioRepository::class.java)
    private val useCase = FuncionarioUseCase(repository)

    @Test
    fun `cria funcionario e provisiona login normalizado`() {
        val funcionario =
            Funcionario(
                nome = "Funcionário",
                cpf = "12345678901",
                email = "New.User@example.com",
            )
        Mockito.doReturn(funcionario.copy(id = 42))
            .`when`(repository)
            .criar(
                Mockito.any(Funcionario::class.java) ?: funcionario,
                Mockito.anyString() ?: "",
                Mockito.anyString() ?: "",
            )

        val result = useCase.criar(funcionario)

        assertEquals(42L, result.id)
        Mockito.verify(repository).criar(
            funcionario.copy(id = null),
            "new.user",
            "!FIRST_ACCESS_REQUIRED!",
        )
    }

    @Test
    fun `nao cria funcionario sem email valido`() {
        val semEmail = Funcionario(nome = "Funcionário", cpf = "12345678901")
        val emailInvalido = semEmail.copy(email = "invalido")

        assertThrows(UnprocessableEntityException::class.java) { useCase.criar(semEmail) }
        assertThrows(UnprocessableEntityException::class.java) { useCase.criar(emailInvalido) }
        Mockito.verifyNoInteractions(repository)
    }
}

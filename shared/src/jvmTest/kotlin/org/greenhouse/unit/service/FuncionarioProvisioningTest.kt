package org.greenhouse.unit.service

import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.FuncionarioDTO as Funcionario
import org.greenhouse.model.Funcionario as FuncionarioModel
import org.greenhouse.model.Usuario as UsuarioModel
import org.greenhouse.mapper.FuncionarioMapper
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.repository.FuncionarioRepository
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.service.FuncionarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDate

class FuncionarioProvisioningTest {

    private val repository = Mockito.mock(FuncionarioRepository::class.java)
    private val usuarioRepository = Mockito.mock(UsuarioRepository::class.java)
    private val service =
        FuncionarioService(
            repository,
            usuarioRepository,
            FuncionarioMapper(),
            UsuarioMapper(),
        )

    @Test
    fun `cria funcionario e provisiona login normalizado`() {
        val funcionario =
            Funcionario(
                nome = "Funcionário",
                cpf = "12345678901",
                email = "New.User@example.com",
            )
        var funcionarioPersistido: FuncionarioModel? = null
        val funcionarioSalvo =
            FuncionarioModel(
                id = 42,
                nome = funcionario.nome,
                cpf = funcionario.cpf,
                email = funcionario.email,
            )
        Mockito.doAnswer { invocation ->
            funcionarioPersistido = invocation.getArgument(0)
            funcionarioSalvo
        }
            .`when`(repository)
            .criar(Mockito.any(FuncionarioModel::class.java) ?: FuncionarioModel())
        var usuarioPersistido: UsuarioModel? = null
        Mockito.doReturn(null)
            .`when`(usuarioRepository)
            .buscarPorLogin("new.user")
        Mockito.doAnswer { invocation ->
            val saved = invocation.getArgument<UsuarioModel>(0)
            usuarioPersistido = saved
            saved
        }
            .`when`(usuarioRepository)
            .save(Mockito.any(UsuarioModel::class.java) ?: UsuarioModel())

        val result = service.criar(funcionario)

        assertEquals(42L, result.id)
        assertEquals(funcionario.nome, funcionarioPersistido?.nome)
        assertEquals(null, funcionarioPersistido?.id)
        assertEquals("new.user", usuarioPersistido?.login)
        assertEquals("!FIRST_ACCESS_REQUIRED!", usuarioPersistido?.senhaHash)
        assertEquals("FUNCIONARIO", usuarioPersistido?.perfil)
        assertEquals(42L, usuarioPersistido?.funcionarioId)
    }

    @Test
    fun `nao cria funcionario sem email valido`() {
        val semEmail = Funcionario(nome = "Funcionário", cpf = "12345678901")
        val emailInvalido = semEmail.copy(email = "invalido")

        assertThrows(UnprocessableEntityException::class.java) { service.criar(semEmail) }
        assertThrows(UnprocessableEntityException::class.java) { service.criar(emailInvalido) }
        Mockito.verifyNoInteractions(repository, usuarioRepository)
    }

    @Test
    fun `substitui os dados do funcionario incluindo data de nascimento`() {
        val existente =
            FuncionarioModel(
                id = 42,
                nome = "Funcionário",
                cpf = "12345678901",
            )
        Mockito.doReturn(existente).`when`(repository).buscarPorId(42)
        Mockito.doAnswer { invocation -> invocation.getArgument<FuncionarioModel>(1) }
            .`when`(repository)
            .atualizar(Mockito.eq(42L), Mockito.any(FuncionarioModel::class.java) ?: FuncionarioModel())

        val resultado =
            service.atualizar(
                42,
                Funcionario(
                    nome = "Funcionário atualizado",
                    cpf = "12345678901",
                    telefone = "11999999999",
                    email = "funcionario@example.com",
                    cargo = "Gerente",
                    ativo = false,
                    dataDeNascimento = LocalDate.parse("2000-02-29"),
                ),
            )

        assertEquals("Funcionário atualizado", resultado.nome)
        assertEquals(false, resultado.ativo)
        assertEquals(LocalDate.parse("2000-02-29"), resultado.dataDeNascimento)
    }

    @Test
    fun `substituicao permite limpar campos opcionais`() {
        val existente =
            FuncionarioModel(
                id = 42,
                nome = "Funcionário",
                cpf = "12345678901",
                dataDeNascimento = LocalDate.parse("2000-02-29"),
            )
        Mockito.doReturn(existente).`when`(repository).buscarPorId(42)
        Mockito.doReturn(existente.copy(dataDeNascimento = null))
            .`when`(repository)
            .atualizar(Mockito.eq(42L), Mockito.any(FuncionarioModel::class.java) ?: FuncionarioModel())

        val atualizado =
            service.atualizar(
                42,
                Funcionario(
                    nome = "Funcionário",
                    cpf = "12345678901",
                    telefone = null,
                    dataDeNascimento = null,
                ),
            )
        assertEquals(null, atualizado.dataDeNascimento)
        assertEquals(null, atualizado.telefone)
    }
}

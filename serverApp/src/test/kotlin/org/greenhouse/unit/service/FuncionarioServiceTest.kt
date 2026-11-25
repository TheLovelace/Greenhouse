package org.greenhouse.unit.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.FuncionarioRequestDTO
import org.greenhouse.mapper.FuncionarioMapper
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.model.Funcionario
import org.greenhouse.model.Usuario
import org.greenhouse.repository.FuncionarioRepository
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.service.FuncionarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension

/**
 * Testes de unidade do service FuncionarioService.
 *
 * Recursos de teste usados:
 * - MOCKS [funcionarioRepository], [usuarioRepository]: substituem o banco.
 * - SPIES [funcionarioMapper], [usuarioMapper]: mappers reais, injetados pelo @InjectMocks.
 * - DRIVER: cada método de teste chama o service diretamente.
 */
@ExtendWith(MockitoExtension::class)
class FuncionarioServiceTest {
    @Mock lateinit var funcionarioRepository: FuncionarioRepository

    @Mock lateinit var usuarioRepository: UsuarioRepository

    @Spy var funcionarioMapper = FuncionarioMapper()

    @Spy var usuarioMapper = UsuarioMapper()

    @InjectMocks lateinit var service: FuncionarioService

    private val idDoFuncionario = 4L

    private fun funcionarioRequest(
            nome: String = "Ana Lima",
            cpf: String = "98765432100",
            email: String? = "Ana.Lima@greenhouse.com",
            ativo: Boolean = true,
    ) = FuncionarioRequestDTO(nome = nome, cpf = cpf, email = email, cargo = "Florista", ativo = ativo)

    private fun funcionarioSalvo(ativo: Boolean = true) =
            Funcionario(
                    id = idDoFuncionario,
                    nome = "Ana Lima",
                    cpf = "98765432100",
                    email = "Ana.Lima@greenhouse.com",
                    cargo = "Florista",
                    ativo = ativo,
            )

    // Kotlin + Mockito: o matcher devolve null, e o "?:" entrega um valor não nulo ao Kotlin.
    private fun qualquerFuncionario(): Funcionario = any(Funcionario::class.java) ?: Funcionario()

    private fun funcionarioCadastrado(funcionario: Funcionario? = funcionarioSalvo()) {
        `when`(funcionarioRepository.buscarPorId(idDoFuncionario)).thenReturn(funcionario)
    }

    private fun nenhumFuncionarioFoiCriado() = verify(funcionarioRepository, never()).criar(qualquerFuncionario())

    // CT01 - Fluxo principal: cria o funcionário e o usuário com login derivado do e-mail
    @Test
    fun `deve criar funcionario e provisionar usuario com login do email`() {
        `when`(usuarioRepository.buscarPorLogin("ana.lima")).thenReturn(null)
        `when`(funcionarioRepository.criar(qualquerFuncionario())).thenReturn(funcionarioSalvo())
        val usuarioEnviadoAoBanco = ArgumentCaptor.forClass(Usuario::class.java)

        val resultado = service.criar(funcionarioRequest())

        verify(usuarioRepository).save(usuarioEnviadoAoBanco.capture())
        val usuario = usuarioEnviadoAoBanco.value
        assertAll(
                "funcionário criado com usuário de acesso",
                { assertEquals(idDoFuncionario, resultado.id, "devolve o funcionário salvo") },
                { assertEquals("ana.lima", usuario.login, "login é o e-mail antes do @, em minúsculas") },
                { assertEquals(idDoFuncionario, usuario.funcionarioId, "usuário ligado ao funcionário") },
                { assertEquals("FUNCIONARIO", usuario.perfil, "perfil padrão de funcionário") },
                { assertFalse(usuario.primeiroAcessoFeito, "o primeiro acesso ainda está pendente") },
        )
    }

    // CT02 - Fluxo de exceção: não cria funcionário sem e-mail
    @Test
    fun `nao deve criar funcionario sem email`() {
        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.criar(funcionarioRequest(email = null))
                }

        assertEquals("E-mail válido é obrigatório para criar um funcionário", erro.message)
        nenhumFuncionarioFoiCriado()
    }

    // CT03 - Fluxo de exceção: não cria funcionário com e-mail em formato inválido
    @Test
    fun `nao deve criar funcionario com email invalido`() {
        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.criar(funcionarioRequest(email = "ana.lima-sem-arroba"))
                }

        assertEquals("E-mail inválido", erro.message)
        nenhumFuncionarioFoiCriado()
    }

    // CT04 - Análise de valor limite: parte do e-mail antes do @ com 101 caracteres excede o login
    @Test
    fun `nao deve criar funcionario quando o login derivado passa de 100 caracteres`() {
        val emailComLoginLongo = "a".repeat(101) + "@greenhouse.com"

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.criar(funcionarioRequest(email = emailComLoginLongo))
                }

        assertEquals("A parte do e-mail antes de @ excede o limite do login", erro.message)
        nenhumFuncionarioFoiCriado()
    }

    // CT05 - Fluxo de exceção: login derivado do e-mail já está em uso
    @Test
    fun `nao deve criar funcionario quando o login ja existe`() {
        `when`(usuarioRepository.buscarPorLogin("ana.lima")).thenReturn(Usuario(id = 1L, login = "ana.lima"))

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.criar(funcionarioRequest())
                }

        assertEquals("Já existe um usuário com o login derivado deste e-mail", erro.message)
        nenhumFuncionarioFoiCriado()
    }

    // CT06 - Fluxo de exceção: CPF precisa ter exatamente 11 dígitos
    @Test
    fun `nao deve criar funcionario com cpf invalido`() {
        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.criar(funcionarioRequest(cpf = "123.456.789-00"))
                }

        assertEquals("CPF inválido. Deve conter exatamente 11 dígitos numéricos.", erro.message)
        nenhumFuncionarioFoiCriado()
    }

    // CT07 - Fluxo de exceção: nome em branco
    @Test
    fun `nao deve criar funcionario com nome em branco`() {
        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.criar(funcionarioRequest(nome = "   "))
                }

        assertEquals("O nome do funcionário é obrigatório", erro.message)
    }

    // CT08 - Análise de valor limite: nome com 151 caracteres passa do máximo de 150
    @Test
    fun `nao deve criar funcionario com nome acima de 150 caracteres`() {
        val nomeLongoDemais = "A".repeat(151)

        val erro =
                assertThrows(UnprocessableEntityException::class.java) {
                    service.criar(funcionarioRequest(nome = nomeLongoDemais))
                }

        assertEquals("O nome do funcionário deve ter no máximo 150 caracteres", erro.message)
    }

    // CT09 - Fluxo principal: consulta funcionário pelo id
    @Test
    fun `deve buscar funcionario por id`() {
        funcionarioCadastrado()

        val resultado = service.buscarPorId(idDoFuncionario)

        assertEquals("Ana Lima", resultado.nome, "devolve o funcionário do id pedido")
    }

    // CT10 - Fluxo de exceção: id inválido e funcionário inexistente
    @Test
    fun `deve rejeitar id invalido e funcionario inexistente na busca`() {
        funcionarioCadastrado(null)

        val erroIdInvalido = assertThrows(BadRequestException::class.java) { service.buscarPorId(0L) }
        val erroInexistente =
                assertThrows(NotFoundException::class.java) { service.buscarPorId(idDoFuncionario) }

        assertAll(
                "consulta de funcionário inválida",
                {
                    assertEquals(
                            "O id do funcionário deve ser maior que zero",
                            erroIdInvalido.message,
                            "id 0 é rejeitado"
                    )
                },
                {
                    assertEquals(
                            "Funcionário não encontrado",
                            erroInexistente.message,
                            "id sem funcionário é rejeitado"
                    )
                },
        )
    }

    // CT11 - Fluxo principal: atualiza o funcionário e sincroniza o status do usuário
    @Test
    fun `deve atualizar funcionario e sincronizar status do usuario`() {
        funcionarioCadastrado()
        `when`(funcionarioRepository.atualizar(anyLong(), qualquerFuncionario()))
                .thenReturn(funcionarioSalvo(ativo = false))

        val resultado = service.atualizar(idDoFuncionario, funcionarioRequest(ativo = false))

        assertFalse(resultado.ativo, "devolve o funcionário atualizado")
        verify(usuarioRepository).atualizarAtivoPorFuncionario(idDoFuncionario, false)
    }

    // CT12 - Fluxo de exceção: não atualiza funcionário inexistente
    @Test
    fun `nao deve atualizar funcionario inexistente`() {
        funcionarioCadastrado(null)

        val erro =
                assertThrows(NotFoundException::class.java) {
                    service.atualizar(idDoFuncionario, funcionarioRequest())
                }

        assertEquals("Funcionário não encontrado", erro.message)
        verify(funcionarioRepository, never()).atualizar(anyLong(), qualquerFuncionario())
    }

    // CT13 - Fluxo de exceção: o registro some entre a consulta e a atualização
    @Test
    fun `deve falhar quando o banco nao atualiza o funcionario`() {
        funcionarioCadastrado()
        `when`(funcionarioRepository.atualizar(anyLong(), qualquerFuncionario())).thenReturn(null)

        val erro =
                assertThrows(NotFoundException::class.java) {
                    service.atualizar(idDoFuncionario, funcionarioRequest())
                }

        assertEquals("Funcionário não encontrado", erro.message)
        verify(usuarioRepository, never()).atualizarAtivoPorFuncionario(anyLong(), anyBoolean())
    }

    // CT14 - Fluxo principal: desativar inativa o funcionário e o usuário dele
    @Test
    fun `deve desativar funcionario e o usuario vinculado`() {
        funcionarioCadastrado(funcionarioSalvo(ativo = true))
        val funcionarioEnviadoAoBanco = ArgumentCaptor.forClass(Funcionario::class.java)
        `when`(funcionarioRepository.atualizar(anyLong(), qualquerFuncionario()))
                .thenReturn(funcionarioSalvo(ativo = false))

        service.desativar(idDoFuncionario)

        verify(funcionarioRepository).atualizar(anyLong(), funcionarioEnviadoAoBanco.capture() ?: Funcionario())
        assertFalse(funcionarioEnviadoAoBanco.value.ativo, "o funcionário é salvo como inativo")
        verify(usuarioRepository).atualizarAtivoPorFuncionario(idDoFuncionario, false)
    }

    // CT15 - Fluxo de exceção: não desativa funcionário inexistente
    @Test
    fun `nao deve desativar funcionario inexistente`() {
        funcionarioCadastrado(null)

        val erro = assertThrows(NotFoundException::class.java) { service.desativar(idDoFuncionario) }

        assertEquals("Funcionário não encontrado", erro.message)
        verify(usuarioRepository, never()).atualizarAtivoPorFuncionario(anyLong(), anyBoolean())
    }
}

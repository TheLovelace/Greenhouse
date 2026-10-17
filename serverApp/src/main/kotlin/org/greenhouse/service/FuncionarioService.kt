package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.FuncionarioRequestDTO
import org.greenhouse.dto.response.FuncionarioResponseDTO
import org.greenhouse.dto.UsuarioDTO as Usuario
import org.greenhouse.mapper.FuncionarioMapper
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.model.Funcionario
import org.greenhouse.repository.FuncionarioRepository
import org.greenhouse.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale

@Service
class FuncionarioService(
    private val funcionarioRepository: FuncionarioRepository,
    private val usuarioRepository: UsuarioRepository,
    private val funcionarioMapper: FuncionarioMapper,
    private val usuarioMapper: UsuarioMapper,
) {

    private val cpfRegex = Regex("^[0-9]{11}$")
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    @Transactional
    fun criar(funcionario: FuncionarioRequestDTO): FuncionarioResponseDTO {
        validar(funcionario)
        val email = funcionario.email
            ?: throw UnprocessableEntityException("E-mail válido é obrigatório para criar um funcionário")
        if (email.length > 150 || !email.matches(emailRegex)) {
            throw UnprocessableEntityException("E-mail inválido")
        }

        val login = email.substringBefore('@').lowercase(Locale.ROOT)
        if (login.length > 100) {
            throw UnprocessableEntityException("A parte do e-mail antes de @ excede o limite do login")
        }

        if (usuarioRepository.buscarPorLogin(login) != null) {
            throw UnprocessableEntityException("Já existe um usuário com o login derivado deste e-mail")
        }

        val funcionarioSalvo = funcionarioRepository.criar(funcionarioMapper.toModel(funcionario))
        usuarioRepository.save(
            usuarioMapper.toModel(
                Usuario(
                    funcionarioId = requireNotNull(funcionarioSalvo.id),
                    login = login,
                    senhaHash = SENHA_PLACEHOLDER,
                    perfil = "FUNCIONARIO",
                    ativo = funcionarioSalvo.ativo,
                    primeiroAcessoFeito = false,
                ),
            ),
        )
        return funcionarioMapper.toResponseDto(funcionarioSalvo)
    }

    fun listar(nome: String? = null): List<FuncionarioResponseDTO> {
        val funcionarios = if (nome.isNullOrBlank()) {
            funcionarioRepository.listar()
        } else {
            funcionarioRepository.buscarPorNome(nome)
        }
        return funcionarios.map(funcionarioMapper::toResponseDto)
    }

    fun buscarPorId(id: Long): FuncionarioResponseDTO =
        funcionarioRepository.buscarPorId(validarId(id))
            ?.let(funcionarioMapper::toResponseDto) ?: throw NotFoundException("Funcionário não encontrado")

    @Transactional
    fun atualizar(id: Long, funcionario: FuncionarioRequestDTO): FuncionarioResponseDTO {
        val idValidado = validarId(id)
        if (funcionarioRepository.buscarPorId(idValidado) == null) {
            throw NotFoundException("Funcionário não encontrado")
        }
        validar(funcionario)

        return atualizarFuncionario(idValidado, funcionarioMapper.toModel(funcionario, idValidado))
    }

    @Transactional
    fun desativar(id: Long) {
        val idValidado = validarId(id)
        val funcionarioAtual = funcionarioRepository.buscarPorId(idValidado)
            ?: throw NotFoundException("Funcionário não encontrado")

        val funcionarioInativado = funcionarioAtual.copy(ativo = false)
        atualizarFuncionario(idValidado, funcionarioInativado)
    }

    private fun atualizarFuncionario(id: Long, funcionario: Funcionario): FuncionarioResponseDTO {
        val atualizado = funcionarioRepository.atualizar(id, funcionario)
            ?: throw NotFoundException("Funcionário não encontrado")
        usuarioRepository.atualizarAtivoPorFuncionario(id, funcionario.ativo)
        return funcionarioMapper.toResponseDto(atualizado)
    }

    private fun validar(funcionario: FuncionarioRequestDTO) {
        if (!funcionario.cpf.matches(cpfRegex)) {
            throw UnprocessableEntityException("CPF inválido. Deve conter exatamente 11 dígitos numéricos.")
        }
        if (funcionario.nome.isBlank()) {
            throw UnprocessableEntityException("O nome do funcionário é obrigatório")
        }
        if (funcionario.nome.length > 150) {
            throw UnprocessableEntityException("O nome do funcionário deve ter no máximo 150 caracteres")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id do funcionário deve ser maior que zero")
        }
        return id
    }

    private companion object {
        const val SENHA_PLACEHOLDER = "!FIRST_ACCESS_REQUIRED!"
    }
}
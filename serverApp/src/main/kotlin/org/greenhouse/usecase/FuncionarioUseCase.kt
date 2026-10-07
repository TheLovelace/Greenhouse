package org.greenhouse.usecase

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Funcionario
import org.greenhouse.repository.FuncionarioRepository
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class FuncionarioUseCase(
    private val funcionarioRepository: FuncionarioRepository,
) {

    private val cpfRegex = Regex("^[0-9]{11}$")
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun criar(funcionario: Funcionario): Funcionario {
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

        return funcionarioRepository.criar(
            funcionario.copy(id = null),
            login,
            senhaPlaceholder = SENHA_PLACEHOLDER,
        )
    }

    fun listar(): List<Funcionario> = funcionarioRepository.listar()

    fun buscarPorId(id: Long): Funcionario =
        funcionarioRepository.buscarPorId(validarId(id))
            ?: throw NotFoundException("Funcionário não encontrado")

    fun atualizar(id: Long, campos: Map<String, Any?>): Funcionario {
        val idValidado = validarId(id)
        val funcionarioAtual = funcionarioRepository.buscarPorId(idValidado)
            ?: throw NotFoundException("Funcionário não encontrado")

        val camposPermitidos = setOf("id", "nome", "cpf", "telefone", "email", "cargo", "ativo")
        val camposDesconhecidos = campos.keys - camposPermitidos

        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val funcionarioAtualizado =
            funcionarioAtual.copy(
                nome = campos.texto("nome", funcionarioAtual.nome),
                cpf = campos.texto("cpf", funcionarioAtual.cpf),
                telefone = campos.textoOpcional("telefone", funcionarioAtual.telefone),
                email = campos.textoOpcional("email", funcionarioAtual.email),
                cargo = campos.textoOpcional("cargo", funcionarioAtual.cargo),
                ativo = campos.booleano("ativo", funcionarioAtual.ativo),
            )
        validar(funcionarioAtualizado)

        return funcionarioRepository.atualizar(idValidado, funcionarioAtualizado)
            ?: throw NotFoundException("Funcionário não encontrado")
    }

    fun desativar(id: Long) {
        val idValidado = validarId(id)
        val funcionarioAtual = funcionarioRepository.buscarPorId(idValidado)
            ?: throw NotFoundException("Funcionário não encontrado")

        val funcionarioInativado = funcionarioAtual.copy(ativo = false)
        funcionarioRepository.atualizar(idValidado, funcionarioInativado)
            ?: throw NotFoundException("Funcionário não encontrado")
    }

    private fun validar(funcionario: Funcionario) {
        if (!funcionario.cpf.matches(cpfRegex)) {
            throw UnprocessableEntityException("CPF inválido. Deve conter exatamente 11 dígitos numéricos.")
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

    private fun Map<String, Any?>.texto(
        campo: String,
        valorAtual: String,
    ): String {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto")
    }

    private fun Map<String, Any?>.textoOpcional(
        campo: String,
        valorAtual: String?,
    ): String? {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? String?
    }

    private fun Map<String, Any?>.booleano(
        campo: String,
        valorAtual: Boolean,
    ): Boolean {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? Boolean
            ?: throw BadRequestException("O campo '$campo' deve ser booleano")
    }

    private companion object {
        const val SENHA_PLACEHOLDER = "!FIRST_ACCESS_REQUIRED!"
    }
}
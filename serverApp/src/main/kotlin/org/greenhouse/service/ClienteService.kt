package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.ClienteDTO as Cliente
import org.greenhouse.mapper.ClienteMapper
import org.greenhouse.repository.ClienteRepository
import org.springframework.stereotype.Service

@Service
class ClienteService(
    private val clienteRepository: ClienteRepository,
    private val clienteMapper: ClienteMapper,
) {

    fun criar(cliente: Cliente): Cliente {
        val clienteNormalizado =
            cliente.copy(
                id = null,
                ativo = true,
                cpf = cliente.cpf?.takeIf { it.isNotBlank() },
                email = cliente.email?.takeIf { it.isNotBlank() },
            )
        validar(clienteNormalizado)
        validarCpfUnico(clienteNormalizado.cpf)
        val model = clienteMapper.toModel(clienteNormalizado)
        return clienteMapper.toDto(clienteRepository.criar(model))
    }

    fun listar(
        nome: String? = null,
        telefone: String? = null,
        ativo: Boolean? = null,
    ): List<Cliente> =
        clienteRepository.listar(
            nome?.takeIf { it.isNotBlank() },
            telefone?.takeIf { it.isNotBlank() },
            ativo,
        ).map(clienteMapper::toDto)

    fun buscarPorId(id: Long): Cliente =
        clienteRepository.buscarPorId(validarId(id))
            ?.let(clienteMapper::toDto) ?: throw NotFoundException("Cliente não encontrado")

    fun atualizar(id: Long, campos: Map<String, Any?>): Cliente {
        val idValidado = validarId(id)
        val clienteAtual = clienteRepository.buscarPorId(idValidado)
            ?.let(clienteMapper::toDto) ?: throw NotFoundException("Cliente não encontrado")

        val camposPermitidos = setOf("id", "nome", "telefone", "whatsapp", "cpf", "email", "ativo")
        val camposDesconhecidos = campos.keys - camposPermitidos
        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val clienteAtualizado =
            clienteAtual.copy(
                nome = campos.texto("nome", clienteAtual.nome),
                telefone = campos.texto("telefone", clienteAtual.telefone),
                whatsapp = campos.booleano("whatsapp", clienteAtual.whatsapp),
                cpf = campos.textoOpcional("cpf", clienteAtual.cpf),
                email = campos.textoOpcional("email", clienteAtual.email),
                ativo = campos.booleano("ativo", clienteAtual.ativo),
            )
        validar(clienteAtualizado)
        if (clienteAtualizado.cpf != clienteAtual.cpf) {
            validarCpfUnico(clienteAtualizado.cpf, ignorarId = idValidado)
        }

        return clienteRepository.atualizar(idValidado, clienteMapper.toModel(clienteAtualizado))
            ?.let(clienteMapper::toDto) ?: throw NotFoundException("Cliente não encontrado")
    }

    private fun validar(cliente: Cliente) {
        if (cliente.nome.isBlank()) {
            throw UnprocessableEntityException("O nome do cliente é obrigatório")
        }
        if (cliente.nome.length > 150) {
            throw UnprocessableEntityException("O nome do cliente deve ter no máximo 150 caracteres")
        }
        if (cliente.telefone.isBlank()) {
            throw UnprocessableEntityException("O telefone do cliente é obrigatório")
        }
        if (cliente.telefone.length > 20) {
            throw UnprocessableEntityException("O telefone do cliente deve ter no máximo 20 caracteres")
        }
        cliente.cpf?.let {
            if (!it.matches(Regex("^[0-9]{11}$"))) {
                throw UnprocessableEntityException("O CPF do cliente deve conter exatamente 11 dígitos numéricos")
            }
        }
        cliente.email?.let {
            if (it.length > 150) {
                throw UnprocessableEntityException("O e-mail do cliente deve ter no máximo 150 caracteres")
            }
            if (!it.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
                throw UnprocessableEntityException("O e-mail do cliente é inválido")
            }
        }
    }

    private fun validarCpfUnico(
        cpf: String?,
        ignorarId: Long? = null,
    ) {
        cpf ?: return
        val existente = clienteRepository.buscarPorCpf(cpf)?.let(clienteMapper::toDto)
        if (existente != null && existente.id != ignorarId) {
            throw BadRequestException("CPF já cadastrado para outro cliente")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id do cliente deve ser maior que zero")
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
        val valor = this[campo] ?: return null
        val texto = valor as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto")
        return texto.takeIf { it.isNotBlank() }
    }

    private fun Map<String, Any?>.booleano(
        campo: String,
        valorAtual: Boolean,
    ): Boolean {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? Boolean
            ?: throw BadRequestException("O campo '$campo' deve ser booleano")
    }
}

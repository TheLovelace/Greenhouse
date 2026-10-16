package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.ClienteCreateRequestDTO
import org.greenhouse.dto.request.ClienteUpdateRequestDTO
import org.greenhouse.dto.response.ClienteResponseDTO
import org.greenhouse.mapper.ClienteMapper
import org.greenhouse.model.Cliente
import org.greenhouse.repository.ClienteRepository
import org.springframework.stereotype.Service

@Service
class ClienteService(
    private val clienteRepository: ClienteRepository,
    private val clienteMapper: ClienteMapper,
) {

    fun criar(request: ClienteCreateRequestDTO): ClienteResponseDTO {
        val clienteNormalizado =
            clienteMapper.toModel(request).copy(
                id = null,
                ativo = true,
                cpf = request.cpf?.takeIf { it.isNotBlank() },
                email = request.email?.takeIf { it.isNotBlank() },
            )
        validar(clienteNormalizado)
        validarCpfUnico(clienteNormalizado.cpf)
        return clienteMapper.toResponse(clienteRepository.criar(clienteNormalizado))
    }

    fun listar(
        nome: String? = null,
        telefone: String? = null,
        ativo: Boolean? = null,
    ): List<ClienteResponseDTO> =
        clienteRepository.listar(
            nome?.takeIf { it.isNotBlank() },
            telefone?.takeIf { it.isNotBlank() },
            ativo,
        ).map(clienteMapper::toResponse)

    fun buscarPorId(id: Long): ClienteResponseDTO =
        clienteRepository.buscarPorId(validarId(id))
            ?.let(clienteMapper::toResponse) ?: throw NotFoundException("Cliente não encontrado")

    fun atualizar(id: Long, request: ClienteUpdateRequestDTO): ClienteResponseDTO {
        val idValidado = validarId(id)
        val clienteAtual =
            clienteRepository.buscarPorId(idValidado)
                ?: throw NotFoundException("Cliente não encontrado")
        if (!request.possuiCamposParaAtualizar) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val clienteAtualizado = clienteMapper.toModel(request, clienteAtual)
        validar(clienteAtualizado)
        if (clienteAtualizado.cpf != clienteAtual.cpf) {
            validarCpfUnico(clienteAtualizado.cpf, ignorarId = idValidado)
        }

        return clienteRepository.atualizar(idValidado, clienteAtualizado)
            ?.let(clienteMapper::toResponse) ?: throw NotFoundException("Cliente não encontrado")
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
        val existente = clienteRepository.buscarPorCpf(cpf)
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
}

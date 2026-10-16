package org.greenhouse.mapper

import org.greenhouse.dto.request.ClienteCreateRequestDTO
import org.greenhouse.dto.request.ClienteUpdateRequestDTO
import org.greenhouse.dto.response.ClienteResponseDTO
import org.greenhouse.model.Cliente
import org.springframework.stereotype.Component

@Component
class ClienteMapper {
    fun toResponse(model: Cliente) =
        ClienteResponseDTO(
            id = requireNotNull(model.id),
            nome = model.nome,
            telefone = model.telefone,
            whatsapp = model.whatsapp,
            cpf = model.cpf,
            email = model.email,
            ativo = model.ativo,
        )

    fun toModel(dto: ClienteCreateRequestDTO) =
        Cliente(
            nome = dto.nome,
            telefone = dto.telefone,
            whatsapp = dto.whatsapp,
            cpf = dto.cpf,
            email = dto.email,
            ativo = dto.ativo,
        )

    fun toModel(
        dto: ClienteUpdateRequestDTO,
        current: Cliente,
    ) = current.copy(
        nome = dto.nome ?: current.nome,
        telefone = dto.telefone ?: current.telefone,
        whatsapp = dto.whatsapp ?: current.whatsapp,
        // Empty string clears the field; null keeps the current value.
        cpf = if (dto.cpf != null) dto.cpf.takeIf { it.isNotBlank() } else current.cpf,
        email = if (dto.email != null) dto.email.takeIf { it.isNotBlank() } else current.email,
        ativo = dto.ativo ?: current.ativo,
    )
}

package org.greenhouse.mapper

import org.greenhouse.dto.ClienteDTO
import org.greenhouse.model.Cliente
import org.springframework.stereotype.Component

@Component
class ClienteMapper {
    fun toDto(model: Cliente) =
        ClienteDTO(model.id, model.nome, model.telefone, model.whatsapp, model.cpf, model.email, model.ativo)

    fun toModel(dto: ClienteDTO) =
        Cliente(dto.id, dto.nome, dto.telefone, dto.whatsapp, dto.cpf, dto.email, dto.ativo)
}

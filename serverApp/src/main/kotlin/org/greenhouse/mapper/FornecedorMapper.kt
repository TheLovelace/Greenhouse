package org.greenhouse.mapper

import org.greenhouse.dto.request.FornecedorCreateRequestDTO
import org.greenhouse.dto.request.FornecedorUpdateRequestDTO
import org.greenhouse.dto.response.FornecedorResponseDTO
import org.greenhouse.model.Fornecedor
import org.springframework.stereotype.Component

@Component
class FornecedorMapper {
    fun toResponseDto(model: Fornecedor) =
        FornecedorResponseDTO(
            id = requireNotNull(model.id),
            nome = model.nome,
            cnpj = model.cnpj,
            telefone = model.telefone,
            email = model.email,
            contato = model.contato,
            ativo = model.ativo,
            prazoMedioEntregaDias = model.prazoMedioEntregaDias,
        )

    fun toModel(dto: FornecedorCreateRequestDTO) =
        Fornecedor(
            nome = dto.nome,
            cnpj = dto.cnpj,
            telefone = dto.telefone,
            email = dto.email,
            contato = dto.contato,
            ativo = dto.ativo,
            prazoMedioEntregaDias = dto.prazoMedioEntregaDias,
        )

    fun toModel(dto: FornecedorUpdateRequestDTO) =
        Fornecedor(
            nome = dto.nome,
            cnpj = dto.cnpj,
            telefone = dto.telefone,
            email = dto.email,
            contato = dto.contato,
            ativo = dto.ativo,
            prazoMedioEntregaDias = dto.prazoMedioEntregaDias,
        )
}

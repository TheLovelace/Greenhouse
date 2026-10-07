package org.greenhouse.mapper

import org.greenhouse.dto.FornecedorDTO
import org.greenhouse.model.Fornecedor
import org.springframework.stereotype.Component

@Component
class FornecedorMapper {
    fun toDto(model: Fornecedor) =
        FornecedorDTO(
            model.id,
            model.nome,
            model.cnpj,
            model.telefone,
            model.email,
            model.contato,
            model.ativo,
        )

    fun toModel(dto: FornecedorDTO) =
        Fornecedor(
            dto.id,
            dto.nome,
            dto.cnpj,
            dto.telefone,
            dto.email,
            dto.contato,
            dto.ativo,
        )
}

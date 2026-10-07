package org.greenhouse.mapper

import org.greenhouse.dto.FuncionarioDTO
import org.greenhouse.model.Funcionario
import org.springframework.stereotype.Component

@Component
class FuncionarioMapper {
    fun toDto(model: Funcionario) =
        FuncionarioDTO(model.id, model.nome, model.cpf, model.telefone, model.email, model.cargo, model.ativo)

    fun toModel(dto: FuncionarioDTO) =
        Funcionario(dto.id, dto.nome, dto.cpf, dto.telefone, dto.email, dto.cargo, dto.ativo)
}

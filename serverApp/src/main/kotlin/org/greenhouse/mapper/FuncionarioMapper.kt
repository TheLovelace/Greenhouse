package org.greenhouse.mapper

import org.greenhouse.dto.FuncionarioDTO
import org.greenhouse.model.Funcionario
import org.springframework.stereotype.Component

@Component
class FuncionarioMapper {
    fun toDto(model: Funcionario) =
        FuncionarioDTO(
            id = model.id,
            nome = model.nome,
            cpf = model.cpf,
            telefone = model.telefone,
            email = model.email,
            cargo = model.cargo,
            ativo = model.ativo,
            dataDeNascimento = model.dataDeNascimento,
        )

    fun toModel(dto: FuncionarioDTO) =
        Funcionario(
            id = dto.id,
            nome = dto.nome,
            cpf = dto.cpf,
            telefone = dto.telefone,
            email = dto.email,
            cargo = dto.cargo,
            ativo = dto.ativo,
            dataDeNascimento = dto.dataDeNascimento,
        )
}

package org.greenhouse.mapper

import org.greenhouse.dto.request.FuncionarioRequestDTO
import org.greenhouse.dto.response.FuncionarioResponseDTO
import org.greenhouse.model.Funcionario
import org.springframework.stereotype.Component

@Component
class FuncionarioMapper {
    fun toResponseDto(model: Funcionario) =
        FuncionarioResponseDTO(
            id = requireNotNull(model.id),
            nome = model.nome,
            cpf = model.cpf,
            telefone = model.telefone,
            email = model.email,
            cargo = model.cargo,
            ativo = model.ativo,
            dataDeNascimento = model.dataDeNascimento,
        )

    fun toModel(dto: FuncionarioRequestDTO, id: Long? = null) =
        Funcionario(
            id = id,
            nome = dto.nome,
            cpf = dto.cpf,
            telefone = dto.telefone,
            email = dto.email,
            cargo = dto.cargo,
            ativo = dto.ativo,
            dataDeNascimento = dto.dataDeNascimento,
        )
}

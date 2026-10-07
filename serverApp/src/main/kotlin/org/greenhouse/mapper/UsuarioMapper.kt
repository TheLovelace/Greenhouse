package org.greenhouse.mapper

import org.greenhouse.dto.UsuarioDTO
import org.greenhouse.model.Usuario
import org.springframework.stereotype.Component

@Component
class UsuarioMapper {
    fun toDto(model: Usuario) =
        UsuarioDTO(
            model.id,
            model.funcionarioId,
            model.login,
            model.senhaHash,
            model.perfil,
            model.ativo,
            model.primeiroAcessoFeito,
        )

    fun toModel(dto: UsuarioDTO) =
        Usuario(
            dto.id,
            dto.funcionarioId,
            dto.login,
            dto.senhaHash,
            dto.perfil,
            dto.ativo,
            dto.primeiroAcessoFeito,
        )
}

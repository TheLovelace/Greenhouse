package org.greenhouse.mapper

import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.dto.response.FormaPagamentoResponseDTO
import org.greenhouse.model.FormaPagamento
import org.springframework.stereotype.Component

@Component
class FormaPagamentoMapper {
    fun toResponseDto(model: FormaPagamento) =
        FormaPagamentoResponseDTO(
            id = requireNotNull(model.id),
            nome = model.nome,
            ativo = model.ativo,
            descricao = model.descricao,
            limiteParcelas = model.limiteParcelas,
            prazoEstornoDias = model.prazoEstornoDias,
        )

    fun toModel(dto: FormaPagamentoRequestDTO, id: Long? = null) =
        FormaPagamento(
            id = id,
            nome = dto.nome,
            ativo = dto.ativo,
            descricao = dto.descricao,
            limiteParcelas = dto.limiteParcelas,
            prazoEstornoDias = dto.prazoEstornoDias,
        )
}

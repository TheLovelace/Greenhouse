package org.greenhouse.mapper

import org.greenhouse.dto.FormaPagamentoDTO
import org.greenhouse.model.FormaPagamento
import org.springframework.stereotype.Component

@Component
class FormaPagamentoMapper {
    fun toDto(model: FormaPagamento) =
        FormaPagamentoDTO(
            id = model.id,
            nome = model.nome,
            ativo = model.ativo,
            descricao = model.descricao,
            limiteParcelas = model.limiteParcelas,
            prazoEstornoDias = model.prazoEstornoDias,
        )

    fun toModel(dto: FormaPagamentoDTO) =
        FormaPagamento(
            id = dto.id,
            nome = dto.nome,
            ativo = dto.ativo,
            descricao = dto.descricao,
            limiteParcelas = dto.limiteParcelas,
            prazoEstornoDias = dto.prazoEstornoDias,
        )
}

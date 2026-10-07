package org.greenhouse.mapper

import org.greenhouse.dto.FormaPagamentoDTO
import org.greenhouse.model.FormaPagamento
import org.springframework.stereotype.Component

@Component
class FormaPagamentoMapper {
    fun toDto(model: FormaPagamento) =
        FormaPagamentoDTO(model.id, model.nome, model.ativo)

    fun toModel(dto: FormaPagamentoDTO) =
        FormaPagamento(dto.id, dto.nome, dto.ativo)
}

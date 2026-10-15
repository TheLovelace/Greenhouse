package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.FormaPagamentoDTO as FormaPagamento
import org.greenhouse.mapper.FormaPagamentoMapper
import org.greenhouse.repository.FormaPagamentoRepository
import org.springframework.stereotype.Service

@Service
class FormaPagamentoService(
    private val formaPagamentoRepository: FormaPagamentoRepository,
    private val formaPagamentoMapper: FormaPagamentoMapper,
) {
    fun criar(formaPagamento: FormaPagamento): FormaPagamento {
        validar(formaPagamento)
        val model = formaPagamentoMapper.toModel(formaPagamento.copy(id = null))
        return formaPagamentoMapper.toDto(formaPagamentoRepository.criar(model))
    }

    fun listar(): List<FormaPagamento> = formaPagamentoRepository.listar().map(formaPagamentoMapper::toDto)

    fun buscarPorId(id: Long): FormaPagamento =
        formaPagamentoRepository.buscarPorId(validarId(id))
            ?.let(formaPagamentoMapper::toDto) ?: throw NotFoundException("Forma de pagamento não encontrada")

    fun atualizar(
        id: Long,
        formaPagamento: FormaPagamento,
    ): FormaPagamento {
        val idValidado = validarId(id)
        if (formaPagamentoRepository.buscarPorId(idValidado) == null) {
            throw NotFoundException("Forma de pagamento não encontrada")
        }
        val atualizada = formaPagamento.copy(id = idValidado)
        validar(atualizada)

        return formaPagamentoRepository.atualizar(idValidado, formaPagamentoMapper.toModel(atualizada))
            ?.let(formaPagamentoMapper::toDto) ?: throw NotFoundException("Forma de pagamento não encontrada")
    }

    fun excluir(id: Long) {
        if (!formaPagamentoRepository.inativar(validarId(id))) {
            throw NotFoundException("Forma de pagamento não encontrada")
        }
    }

    private fun validar(formaPagamento: FormaPagamento) {
        if (formaPagamento.nome.isBlank()) {
            throw UnprocessableEntityException("O nome da forma de pagamento é obrigatório")
        }
        if (formaPagamento.nome.length > 50) {
            throw UnprocessableEntityException("O nome da forma de pagamento deve ter no máximo 50 caracteres")
        }
        if (formaPagamento.descricao != null && formaPagamento.descricao.length > 250) {
            throw UnprocessableEntityException("A descrição deve ter no máximo 250 caracteres")
        }
        if (formaPagamento.limiteParcelas < 1) {
            throw UnprocessableEntityException("O limite de parcelas deve ser maior ou igual a 1")
        }
        if (formaPagamento.prazoEstornoDias != null && formaPagamento.prazoEstornoDias < 0) {
            throw UnprocessableEntityException("O prazo de estorno não pode ser negativo")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id da forma de pagamento deve ser maior que zero")
        }
        return id
    }

}

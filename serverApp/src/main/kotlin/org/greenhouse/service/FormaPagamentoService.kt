package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.dto.response.FormaPagamentoResponseDTO
import org.greenhouse.mapper.FormaPagamentoMapper
import org.greenhouse.repository.FormaPagamentoRepository
import org.springframework.stereotype.Service

@Service
class FormaPagamentoService(
    private val formaPagamentoRepository: FormaPagamentoRepository,
    private val formaPagamentoMapper: FormaPagamentoMapper,
) {
    fun criar(formaPagamento: FormaPagamentoRequestDTO): FormaPagamentoResponseDTO {
        validar(formaPagamento)
        val model = formaPagamentoMapper.toModel(formaPagamento)
        return formaPagamentoMapper.toResponseDto(formaPagamentoRepository.criar(model))
    }

    fun listar(nome: String? = null): List<FormaPagamentoResponseDTO> {
        val formasPagamento = if (nome.isNullOrBlank()) {
            formaPagamentoRepository.listar()
        } else {
            formaPagamentoRepository.buscarPorNome(nome)
        }
        return formasPagamento.map(formaPagamentoMapper::toResponseDto)
    }

    fun buscarPorId(id: Long): FormaPagamentoResponseDTO =
        formaPagamentoRepository.buscarPorId(validarId(id))
            ?.let(formaPagamentoMapper::toResponseDto) ?: throw NotFoundException("Forma de pagamento não encontrada")

    fun atualizar(
        id: Long,
        formaPagamento: FormaPagamentoRequestDTO,
    ): FormaPagamentoResponseDTO {
        val idValidado = validarId(id)
        if (formaPagamentoRepository.buscarPorId(idValidado) == null) {
            throw NotFoundException("Forma de pagamento não encontrada")
        }
        validar(formaPagamento)

        return formaPagamentoRepository.atualizar(idValidado, formaPagamentoMapper.toModel(formaPagamento, idValidado))
            ?.let(formaPagamentoMapper::toResponseDto) ?: throw NotFoundException("Forma de pagamento não encontrada")
    }

    fun excluir(id: Long) {
        if (!formaPagamentoRepository.inativar(validarId(id))) {
            throw NotFoundException("Forma de pagamento não encontrada")
        }
    }

    private fun validar(formaPagamento: FormaPagamentoRequestDTO) {
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

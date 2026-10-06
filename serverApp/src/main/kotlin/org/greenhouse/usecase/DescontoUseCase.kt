package org.greenhouse.usecase

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Desconto
import org.greenhouse.entity.TipoDesconto
import org.greenhouse.repository.DescontoRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

private val VALOR_MAXIMO_DESCONTO = BigDecimal("9999999999.99")

@Service
class DescontoUseCase(
    private val descontoRepository: DescontoRepository,
) {
    fun criar(desconto: Desconto): Desconto {
        validar(desconto)
        return descontoRepository.criar(desconto.copy(id = null))
    }

    fun listar(): List<Desconto> = descontoRepository.listar()

    fun buscarPorId(id: Long): Desconto =
        descontoRepository.buscarPorId(validarId(id))
            ?: throw NotFoundException("Desconto não encontrado")

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id do desconto deve ser maior que zero")
        }
        return id
    }

    private fun validar(desconto: Desconto) {
        if (desconto.titulo.isBlank()) {
            throw UnprocessableEntityException("O título do desconto é obrigatório")
        }
        if (desconto.titulo.length > 150) {
            throw UnprocessableEntityException("O título do desconto deve ter no máximo 150 caracteres")
        }
        if (desconto.valor <= BigDecimal.ZERO) {
            throw UnprocessableEntityException("O valor do desconto deve ser maior que zero")
        }
        if (desconto.tipo == TipoDesconto.PERCENTUAL && desconto.valor > BigDecimal("100")) {
            throw UnprocessableEntityException("O desconto percentual não pode ser maior que 100")
        }
        if (desconto.valor > VALOR_MAXIMO_DESCONTO ||
            desconto.valor.stripTrailingZeros().scale() > 2
        ) {
            throw UnprocessableEntityException("O valor deve respeitar o formato NUMERIC(12,2)")
        }
        if (desconto.dataInicio != null &&
            desconto.dataFim != null &&
            desconto.dataFim.isBefore(desconto.dataInicio)
        ) {
            throw UnprocessableEntityException("A data final não pode ser anterior à data inicial")
        }
    }
}

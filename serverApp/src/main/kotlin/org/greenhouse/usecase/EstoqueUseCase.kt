package org.greenhouse.usecase

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.entity.EstoquePosicao
import org.greenhouse.repository.EstoqueRepository
import org.springframework.stereotype.Service

@Service
class EstoqueUseCase(
    private val estoqueRepository: EstoqueRepository,
) {
    fun buscarPosicaoPorProdutoId(produtoId: Long): EstoquePosicao {
        if (produtoId <= 0) {
            throw BadRequestException("O id do produto deve ser maior que zero")
        }

        return estoqueRepository.buscarPosicaoPorProdutoId(produtoId)
            ?: throw NotFoundException("Produto não encontrado")
    }
}

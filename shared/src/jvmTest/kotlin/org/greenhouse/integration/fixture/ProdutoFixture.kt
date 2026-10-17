package org.greenhouse.integration.fixture

import org.greenhouse.dto.request.ProdutoCreateRequestDTO
import org.greenhouse.dto.response.ProdutoResponseDTO
import org.greenhouse.model.Estoque
import org.greenhouse.repository.EstoqueRepository
import org.greenhouse.repository.ProdutoRepository
import org.greenhouse.service.ProdutoService
import java.math.BigDecimal
import java.util.UUID

class ProdutoFixture {
    private val repository = SpringTestContext.bean<ProdutoRepository>()
    private val estoqueRepository = SpringTestContext.bean<EstoqueRepository>()
    val service = SpringTestContext.bean<ProdutoService>()

    fun novoProduto(
        codigo: String = "SKU-${UUID.randomUUID()}",
        nome: String = "Produto de teste ${UUID.randomUUID()}",
        descricao: String? = "Descrição de teste",
        preco: BigDecimal = BigDecimal("12.50"),
        prazoPreparoMinutos: Int? = null,
    ): ProdutoResponseDTO =
        service.criar(
            ProdutoCreateRequestDTO(
                codigo = codigo,
                nome = nome,
                descricao = descricao,
                preco = preco,
                prazoPreparoMinutos = prazoPreparoMinutos,
            ),
        )

    fun adicionarEstoque(produtoId: Long) {
        estoqueRepository.save(Estoque(produtoId = produtoId))
    }

    fun limpar(id: Long?) {
        id?.let { produtoId ->
            estoqueRepository.deleteById(produtoId)
            repository.excluir(produtoId)
        }
    }
}

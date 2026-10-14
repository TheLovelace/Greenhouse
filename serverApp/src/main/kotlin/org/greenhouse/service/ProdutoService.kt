package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.ProdutoCreateRequestDTO
import org.greenhouse.dto.request.ProdutoUpdateRequestDTO
import org.greenhouse.dto.response.ProdutoResponseDTO
import org.greenhouse.mapper.ProdutoMapper
import org.greenhouse.repository.ProdutoRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProdutoService(
    private val produtoRepository: ProdutoRepository,
    private val produtoMapper: ProdutoMapper,
) {

    fun criar(produto: ProdutoCreateRequestDTO): ProdutoResponseDTO {
        validar(produto.codigo, produto.nome, produto.descricao, produto.preco, produto.prazoPreparoMinutos)
        validarCodigoDisponivel(produto.codigo)
        val model = produtoMapper.toModel(produto)
        return produtoMapper.toResponse(produtoRepository.criar(model))
    }

    fun listar(nome: String? = null): List<ProdutoResponseDTO> =
        (if (nome == null) produtoRepository.listar() else produtoRepository.buscarPorNome(nome))
            .map(produtoMapper::toResponse)

    fun buscarPorId(id: Long): ProdutoResponseDTO =
        produtoRepository.buscarPorId(validarId(id))
            ?.let(produtoMapper::toResponse) ?: throw NotFoundException("Produto não encontrado")

    fun atualizar(id: Long, produto: ProdutoUpdateRequestDTO): ProdutoResponseDTO {
        val idValidado = validarId(id)
        val atual = produtoRepository.buscarPorId(idValidado)
            ?: throw NotFoundException("Produto não encontrado")
        if (!produto.possuiCamposParaAtualizar) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }
        if (!produto.valoresInformadosSaoValidos) {
            throw BadRequestException("Nome, preço e ativo não podem ser nulos quando informados")
        }
        val atualizado = produtoMapper.toModel(produto, atual)
        validar(
            atualizado.codigo,
            atualizado.nome,
            atualizado.descricao,
            atualizado.preco,
            atualizado.prazoPreparoMinutos,
        )
        validarCodigoDisponivel(atualizado.codigo, ignorarId = idValidado)

        return produtoRepository.atualizar(idValidado, atualizado)
            ?.let(produtoMapper::toResponse) ?: throw NotFoundException("Produto não encontrado")
    }

    fun excluir(id: Long) {
        val idValidado = validarId(id)
        if (produtoRepository.buscarPorId(idValidado) == null) {
            throw NotFoundException("Produto não encontrado")
        }
        if (produtoRepository.possuiDependencias(idValidado)) {
            throw UnprocessableEntityException(
                "Produto não pode ser excluído porque possui registros vinculados (pedidos, compras ou estoque)",
            )
        }
        if (!produtoRepository.excluir(idValidado)) {
            throw NotFoundException("Produto não encontrado")
        }
    }

    private fun validar(
        codigo: String?,
        nome: String,
        descricao: String?,
        preco: BigDecimal,
        prazoPreparoMinutos: Int?,
    ) {
        if (codigo != null && codigo.length > 50) {
            throw UnprocessableEntityException("O código do produto deve ter no máximo 50 caracteres")
        }
        if (nome.length > 150) {
            throw UnprocessableEntityException("O nome do produto deve ter no máximo 150 caracteres")
        }
        if (descricao != null && descricao.length > 1000) {
            throw UnprocessableEntityException("A descrição do produto deve ter no máximo 1000 caracteres")
        }
        if (preco < BigDecimal.ZERO) {
            throw UnprocessableEntityException("O preço do produto não pode ser negativo")
        }
        if (preco > BigDecimal("9999999999.99") ||
            preco.stripTrailingZeros().scale() > 2
        ) {
            throw UnprocessableEntityException("O preço deve respeitar o limite de 10 dígitos inteiros e 2 decimais")
        }
        if (prazoPreparoMinutos != null && prazoPreparoMinutos < 0) {
            throw UnprocessableEntityException("O prazo de preparo não pode ser negativo")
        }
    }

    private fun validarCodigoDisponivel(codigo: String?, ignorarId: Long? = null) {
        if (codigo != null && produtoRepository.codigoEmUso(codigo, ignorarId)) {
            throw UnprocessableEntityException("Já existe um produto com o código informado")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id do produto deve ser maior que zero")
        }
        return id
    }

}

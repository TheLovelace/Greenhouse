package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.ProdutoDTO as Produto
import org.greenhouse.mapper.ProdutoMapper
import org.greenhouse.repository.ProdutoRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProdutoService(
    private val produtoRepository: ProdutoRepository,
    private val produtoMapper: ProdutoMapper,
) {

    fun criar(produto: Produto): Produto {
        validar(produto)
        validarCodigoDisponivel(produto.codigo)
        val model = produtoMapper.toModel(produto.copy(id = null))
        return produtoMapper.toDto(produtoRepository.criar(model))
    }

    fun listar(): List<Produto> = produtoRepository.listar().map(produtoMapper::toDto)

    fun buscarPorId(id: Long): Produto =
        produtoRepository.buscarPorId(validarId(id))
            ?.let(produtoMapper::toDto) ?: throw NotFoundException("Produto não encontrado")

    fun atualizar(id: Long, campos: Map<String, Any?>): Produto {
        val idValidado = validarId(id)
        val produtoAtual =
            produtoRepository.buscarPorId(idValidado)
                ?.let(produtoMapper::toDto) ?: throw NotFoundException("Produto não encontrado")

        val camposPermitidos = setOf("id", "codigo", "nome", "descricao", "preco", "ativo")
        val camposDesconhecidos = campos.keys - camposPermitidos
        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val produtoAtualizado =
            produtoAtual.copy(
                codigo = campos.textoOpcional("codigo", produtoAtual.codigo),
                nome = campos.texto("nome", produtoAtual.nome),
                descricao = campos.textoOpcional("descricao", produtoAtual.descricao),
                preco = campos.decimal("preco", produtoAtual.preco),
                ativo = campos.booleano("ativo", produtoAtual.ativo),
            )
        validar(produtoAtualizado)
        validarCodigoDisponivel(produtoAtualizado.codigo, ignorarId = idValidado)

        return produtoRepository.atualizar(idValidado, produtoMapper.toModel(produtoAtualizado))
            ?.let(produtoMapper::toDto) ?: throw NotFoundException("Produto não encontrado")
    }

    fun excluir(id: Long) {
        val idValidado = validarId(id)
        if (produtoRepository.buscarPorId(idValidado) == null) {
            throw NotFoundException("Produto não encontrado")
        }
        if (produtoRepository.possuiDependencias(idValidado)) {
            throw UnprocessableEntityException(
                "Produto não pode ser excluído porque possui registros vinculados (pedidos, compras, estoque ou movimentações)",
            )
        }
        if (!produtoRepository.excluir(idValidado)) {
            throw NotFoundException("Produto não encontrado")
        }
    }

    private fun validar(produto: Produto) {
        val codigo = produto.codigo
        if (codigo != null && codigo.length > 50) {
            throw UnprocessableEntityException("O código do produto deve ter no máximo 50 caracteres")
        }
        if (produto.nome.length > 150) {
            throw UnprocessableEntityException("O nome do produto deve ter no máximo 150 caracteres")
        }
        if (produto.preco < BigDecimal.ZERO) {
            throw UnprocessableEntityException("O preço do produto não pode ser negativo")
        }
        if (produto.preco > BigDecimal("9999999999.99") ||
            produto.preco.stripTrailingZeros().scale() > 2
        ) {
            throw UnprocessableEntityException("O preço deve respeitar o limite de 10 dígitos inteiros e 2 decimais")
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

    private fun Map<String, Any?>.texto(campo: String, valorAtual: String): String {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto")
    }

    private fun Map<String, Any?>.textoOpcional(campo: String, valorAtual: String?): String? {
        if (!containsKey(campo)) return valorAtual
        val valor = this[campo] ?: return null
        return valor as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto ou nulo")
    }

    private fun Map<String, Any?>.decimal(campo: String, valorAtual: BigDecimal): BigDecimal {
        if (!containsKey(campo)) return valorAtual
        val valor = this[campo] as? Number
            ?: throw BadRequestException("O campo '$campo' deve ser numérico")
        return valor.toString().toBigDecimalOrNull()
            ?: throw BadRequestException("O campo '$campo' deve ser numérico")
    }

    private fun Map<String, Any?>.booleano(campo: String, valorAtual: Boolean): Boolean {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? Boolean
            ?: throw BadRequestException("O campo '$campo' deve ser booleano")
    }
}

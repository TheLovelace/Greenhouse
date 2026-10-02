package org.greenhouse.usecase

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Categoria
import org.greenhouse.repository.CategoriaRepository
import org.springframework.stereotype.Service

@Service
class CategoriaUseCase(
    private val categoriaRepository: CategoriaRepository,
) {

    fun criar(categoria: Categoria): Categoria {
        validar(categoria)
        return categoriaRepository.criar(categoria.copy(id = null))
    }

    fun listar(): List<Categoria> = categoriaRepository.listar()

    fun buscarPorId(id: Long): Categoria =
        categoriaRepository.buscarPorId(validarId(id))
            ?: throw NotFoundException("Categoria não encontrada")

    fun atualizar(id: Long, campos: Map<String, Any?>): Categoria {
        val idValidado = validarId(id)
        val categoriaAtual = categoriaRepository.buscarPorId(idValidado)
            ?: throw NotFoundException("Categoria não encontrada")

        val camposPermitidos = setOf("id", "nome", "descricao", "ativo")
        val camposDesconhecidos = campos.keys - camposPermitidos
        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val categoriaAtualizada =
            categoriaAtual.copy(
                nome = campos.texto("nome", categoriaAtual.nome),
                descricao = campos.texto("descricao", categoriaAtual.descricao),
                ativo = campos.booleano("ativo", categoriaAtual.ativo),
            )
        validar(categoriaAtualizada)

        return categoriaRepository.atualizar(idValidado, categoriaAtualizada)
            ?: throw NotFoundException("Categoria não encontrada")
    }

    fun excluir(id: Long) {
        if (!categoriaRepository.excluir(validarId(id))) {
            throw NotFoundException("Categoria não encontrada")
        }
    }

    private fun validar(categoria: Categoria) {
        if (categoria.nome.length > 100) {
            throw UnprocessableEntityException("O nome da categoria deve ter no máximo 100 caracteres")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id da categoria deve ser maior que zero")
        }
        return id
    }

    private fun Map<String, Any?>.texto(
        campo: String,
        valorAtual: String,
    ): String {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto")
    }

    private fun Map<String, Any?>.booleano(
        campo: String,
        valorAtual: Boolean,
    ): Boolean {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? Boolean
            ?: throw BadRequestException("O campo '$campo' deve ser booleano")
    }
}
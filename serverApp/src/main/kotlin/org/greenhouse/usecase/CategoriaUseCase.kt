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

    fun atualizar(id: Long, categoria: Categoria): Categoria {
        validar(categoria)
        val idValidado = validarId(id)
        return categoriaRepository.atualizar(idValidado, categoria.copy(id = idValidado))
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
}
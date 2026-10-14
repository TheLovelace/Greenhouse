package org.greenhouse.service

import jakarta.persistence.EntityManager
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.CategoriaCreateRequestDTO
import org.greenhouse.dto.request.CategoriaUpdateRequestDTO
import org.greenhouse.dto.response.CategoriaResponseDTO
import org.greenhouse.mapper.CategoriaMapper
import org.greenhouse.repository.CategoriaRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoriaService(
    private val categoriaRepository: CategoriaRepository,
    private val categoriaMapper: CategoriaMapper,
    private val entityManagerProvider: ObjectProvider<EntityManager>,
) {
    @Transactional
    fun criar(categoria: CategoriaCreateRequestDTO): CategoriaResponseDTO {
        validar(categoria.nome, categoria.descricao, categoria.observacao)
        validarNomeDisponivel(categoria.nome)
        val model = categoriaMapper.toModel(categoria)
        val criada = categoriaRepository.criar(model)
        if (categoria.dataCadastro == null) {
            entityManagerProvider.getObject().refresh(criada)
        }
        return categoriaMapper.toResponse(criada)
    }

    fun listar(nome: String? = null): List<CategoriaResponseDTO> =
        (if (nome == null) categoriaRepository.listar() else categoriaRepository.buscarPorNome(nome))
            .map(categoriaMapper::toResponse)

    fun buscarPorId(id: Long): CategoriaResponseDTO =
        categoriaRepository.buscarPorId(validarId(id))
            ?.let(categoriaMapper::toResponse) ?: throw NotFoundException("Categoria não encontrada")

    fun atualizar(id: Long, categoria: CategoriaUpdateRequestDTO): CategoriaResponseDTO {
        val idValidado = validarId(id)
        val atual = categoriaRepository.buscarPorId(idValidado)
            ?: throw NotFoundException("Categoria não encontrada")
        if (!categoria.possuiCamposParaAtualizar) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val atualizada = categoriaMapper.toModel(categoria, atual)
        validar(atualizada.nome, atualizada.descricao, atualizada.observacao)
        validarNomeDisponivel(atualizada.nome, ignorarId = idValidado)

        return categoriaRepository.atualizar(idValidado, atualizada)
            ?.let(categoriaMapper::toResponse) ?: throw NotFoundException("Categoria não encontrada")
    }

    fun excluir(id: Long) {
        if (!categoriaRepository.excluir(validarId(id))) {
            throw NotFoundException("Categoria não encontrada")
        }
    }

    private fun validar(nome: String, descricao: String, observacao: String?) {
        if (nome.length > 100) {
            throw UnprocessableEntityException("O nome da categoria deve ter no máximo 100 caracteres")
        }
        if (descricao.length > 1000) {
            throw UnprocessableEntityException("A descrição da categoria deve ter no máximo 1000 caracteres")
        }
        if (observacao != null && observacao.length > 300) {
            throw UnprocessableEntityException("A observação da categoria deve ter no máximo 300 caracteres")
        }
    }

    private fun validarNomeDisponivel(nome: String, ignorarId: Long? = null) {
        if (categoriaRepository.nomeEmUso(nome, ignorarId)) {
            throw UnprocessableEntityException("Já existe uma categoria com o nome informado")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id da categoria deve ser maior que zero")
        }
        return id
    }

}
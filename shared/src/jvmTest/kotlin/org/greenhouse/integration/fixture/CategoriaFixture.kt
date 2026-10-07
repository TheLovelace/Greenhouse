package org.greenhouse.integration.fixture

import org.greenhouse.dto.CategoriaDTO as Categoria
import org.greenhouse.repository.CategoriaRepository
import org.greenhouse.service.CategoriaService
import java.util.UUID

class CategoriaFixture {
    private val repository = SpringTestContext.bean<CategoriaRepository>()
    val service = SpringTestContext.bean<CategoriaService>()

    fun novaCategoria(
        nome: String = "Categoria ${UUID.randomUUID()}",
        descricao: String = "Descrição de teste",
    ): Categoria = service.criar(Categoria(nome = nome, descricao = descricao))

    fun limpar(id: Long?) {
        id?.let(repository::excluir)
    }
}
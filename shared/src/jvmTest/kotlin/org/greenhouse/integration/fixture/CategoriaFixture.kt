package org.greenhouse.integration.fixture

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Categoria
import org.greenhouse.repository.CategoriaRepository
import org.greenhouse.usecase.CategoriaUseCase
import java.util.UUID

class CategoriaFixture {
    private val repository = CategoriaRepository(DatabaseConfig(System.getenv("DATABASE_URL")))
    val useCase = CategoriaUseCase(repository)

    fun novaCategoria(
        nome: String = "Categoria ${UUID.randomUUID()}",
        descricao: String = "Descrição de teste",
    ): Categoria = useCase.criar(Categoria(nome = nome, descricao = descricao))

    fun limpar(id: Long?) {
        id?.let(repository::excluir)
    }
}
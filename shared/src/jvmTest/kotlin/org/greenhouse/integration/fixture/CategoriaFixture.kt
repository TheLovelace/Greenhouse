package org.greenhouse.integration.fixture

import org.greenhouse.dto.request.CategoriaCreateRequestDTO
import org.greenhouse.dto.response.CategoriaResponseDTO
import org.greenhouse.repository.CategoriaRepository
import org.greenhouse.service.CategoriaService
import java.time.LocalDate
import java.util.UUID

class CategoriaFixture {
    private val repository = SpringTestContext.bean<CategoriaRepository>()
    val service = SpringTestContext.bean<CategoriaService>()

    fun novaCategoria(
        nome: String = "Categoria ${UUID.randomUUID()}",
        descricao: String = "Descrição de teste",
        observacao: String? = null,
        dataCadastro: LocalDate? = null,
    ): CategoriaResponseDTO =
        service.criar(
            CategoriaCreateRequestDTO(
                nome = nome,
                descricao = descricao,
                observacao = observacao,
                dataCadastro = dataCadastro,
            ),
        )

    fun limpar(id: Long?) {
        id?.let(repository::excluir)
    }
}
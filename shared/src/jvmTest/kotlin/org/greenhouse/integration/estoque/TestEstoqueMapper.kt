package org.greenhouse.integration.estoque

import org.greenhouse.dto.request.EstoqueCreateRequestDTO
import org.greenhouse.dto.request.EstoqueUpdateRequestDTO
import org.greenhouse.mapper.EstoqueMapper
import org.greenhouse.model.Estoque
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class TestEstoqueMapper {
    private val mapper = EstoqueMapper()

    @Test
    fun `deve converter request de criacao para entidade`() {
        val request =
            EstoqueCreateRequestDTO(
                produtoId = 7,
                quantidadeFisica = 20,
                quantidadeMinima = 5,
                localizacao = "Prateleira A",
                bloqueado = true,
            )

        val model = mapper.toModel(request)

        assertEquals(7, model.produtoId)
        assertEquals(20, model.quantidadeFisica)
        assertEquals(0, model.quantidadeReservada)
        assertEquals(5, model.quantidadeMinima)
        assertEquals("Prateleira A", model.localizacao)
        assertEquals(true, model.bloqueado)
    }

    @Test
    fun `deve aplicar request de atualizacao preservando id e quantidade reservada`() {
        val model =
            Estoque(
                produtoId = 7,
                quantidadeFisica = 20,
                quantidadeReservada = 4,
                quantidadeMinima = 5,
                localizacao = "Prateleira A",
                bloqueado = false,
            )
        val request =
            EstoqueUpdateRequestDTO(
                quantidadeFisica = 25,
                quantidadeMinima = 6,
                localizacao = "Prateleira B",
                bloqueado = true,
            )

        val atualizado = mapper.updateModel(request, model)

        assertSame(model, atualizado)
        assertEquals(7, atualizado.produtoId)
        assertEquals(25, atualizado.quantidadeFisica)
        assertEquals(4, atualizado.quantidadeReservada)
        assertEquals(6, atualizado.quantidadeMinima)
        assertEquals("Prateleira B", atualizado.localizacao)
        assertEquals(true, atualizado.bloqueado)
    }
}

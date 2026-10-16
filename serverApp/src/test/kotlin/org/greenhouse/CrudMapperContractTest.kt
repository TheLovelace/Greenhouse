package org.greenhouse

import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.dto.request.FuncionarioRequestDTO
import org.greenhouse.mapper.FormaPagamentoMapper
import org.greenhouse.mapper.FuncionarioMapper
import org.greenhouse.model.FormaPagamento
import org.greenhouse.model.Funcionario
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CrudMapperContractTest {
    @Test
    fun `funcionario response preserves existing fields`() {
        val response = FuncionarioMapper().toResponseDto(
            Funcionario(
                id = 7,
                nome = "Luna",
                cpf = "12345678901",
                telefone = "11999999999",
                email = "luna@example.com",
                cargo = "Florista",
                ativo = false,
                dataDeNascimento = LocalDate.of(2000, 1, 2),
            ),
        )

        assertEquals(7, response.id)
        assertEquals("Luna", response.nome)
        assertEquals("12345678901", response.cpf)
        assertEquals("11999999999", response.telefone)
        assertEquals("luna@example.com", response.email)
        assertEquals("Florista", response.cargo)
        assertEquals(false, response.ativo)
        assertEquals(LocalDate.of(2000, 1, 2), response.dataDeNascimento)
    }

    @Test
    fun `forma de pagamento response preserves existing fields`() {
        val response = FormaPagamentoMapper().toResponseDto(
            FormaPagamento(
                id = 3,
                nome = "Cartão",
                ativo = false,
                descricao = "Crédito",
                limiteParcelas = 6,
                prazoEstornoDias = 10,
            ),
        )

        assertEquals(3, response.id)
        assertEquals("Cartão", response.nome)
        assertEquals(false, response.ativo)
        assertEquals("Crédito", response.descricao)
        assertEquals(6, response.limiteParcelas)
        assertEquals(10, response.prazoEstornoDias)
    }

    @Test
    fun `request DTOs map fields without accepting response identifiers`() {
        val funcionario = FuncionarioMapper().toModel(
            FuncionarioRequestDTO(
                nome = "Luna",
                cpf = "12345678901",
                email = "luna@example.com",
            ),
        )
        val formaPagamento = FormaPagamentoMapper().toModel(
            FormaPagamentoRequestDTO(nome = "Cartão", limiteParcelas = 6),
        )

        assertEquals(null, funcionario.id)
        assertEquals("Luna", funcionario.nome)
        assertEquals(null, formaPagamento.id)
        assertEquals("Cartão", formaPagamento.nome)
    }
}

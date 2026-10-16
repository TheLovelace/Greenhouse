package org.greenhouse

import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraItemRequestDTO
import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.dto.request.FornecedorCreateRequestDTO
import org.greenhouse.dto.request.FuncionarioRequestDTO
import org.greenhouse.mapper.CompraFornecedorMapper
import org.greenhouse.mapper.FormaPagamentoMapper
import org.greenhouse.mapper.FornecedorMapper
import org.greenhouse.mapper.FuncionarioMapper
import org.greenhouse.model.CompraFornecedor
import org.greenhouse.model.CompraItem
import org.greenhouse.model.FormaPagamento
import org.greenhouse.model.Fornecedor
import org.greenhouse.model.Funcionario
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
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

    @Test
    fun `fornecedor response preserves existing fields`() {
        val response = FornecedorMapper().toResponseDto(
            Fornecedor(
                id = 5,
                nome = "Flores do Campo",
                cnpj = "12345678901234",
                telefone = "11999999999",
                email = "flores@example.com",
                contato = "Maria",
                ativo = false,
                prazoMedioEntregaDias = 10,
            ),
        )

        assertEquals(5, response.id)
        assertEquals("Flores do Campo", response.nome)
        assertEquals("12345678901234", response.cnpj)
        assertEquals("11999999999", response.telefone)
        assertEquals("flores@example.com", response.email)
        assertEquals("Maria", response.contato)
        assertEquals(false, response.ativo)
        assertEquals(10, response.prazoMedioEntregaDias)
    }

    @Test
    fun `fornecedor request maps fields without accepting response identifiers`() {
        val fornecedor = FornecedorMapper().toModel(
            FornecedorCreateRequestDTO(
                nome = "Flores do Campo",
                cnpj = "12345678901234",
                prazoMedioEntregaDias = 10,
            ),
        )

        assertEquals(null, fornecedor.id)
        assertEquals("Flores do Campo", fornecedor.nome)
        assertEquals(10, fornecedor.prazoMedioEntregaDias)
    }

    @Test
    fun `compra fornecedor response preserves compra and item fields`() {
        val mapper = CompraFornecedorMapper()
        val response = mapper.toResponse(
            CompraFornecedor(
                id = 9,
                funcionarioId = 1,
                fornecedorId = 2,
                formaPagamentoId = 3,
                valorTotal = BigDecimal("25.00"),
                valorFrete = BigDecimal("5.00"),
                freteIncluso = false,
                observacao = "Reposição",
            ),
            listOf(
                CompraItem(
                    id = 4,
                    compraId = 9,
                    produtoId = 7,
                    quantidade = 2,
                    valorUnitario = BigDecimal("10.00"),
                ),
            ),
        )

        assertEquals(9, response.id)
        assertEquals(2, response.fornecedorId)
        assertEquals(BigDecimal("25.00"), response.valorTotal)
        assertEquals(BigDecimal("5.00"), response.valorFrete)
        assertEquals(1, response.itens.size)
        assertEquals(7, response.itens.first().produtoId)
        assertEquals(2, response.itens.first().quantidade)
        assertEquals(BigDecimal("10.00"), response.itens.first().valorUnitario)
    }

    @Test
    fun `compra request maps fields without accepting response identifiers`() {
        val compra = CompraFornecedorMapper().toModel(
            CompraFornecedorCreateRequestDTO(
                funcionarioId = 1,
                fornecedorId = 2,
                formaPagamentoId = 3,
                observacao = "Reposição",
                itens =
                    listOf(
                        CompraItemRequestDTO(
                            produtoId = 7,
                            quantidade = 2,
                            valorUnitario = BigDecimal("10.00"),
                        ),
                    ),
            ),
        )

        assertEquals(null, compra.id)
        assertEquals(1, compra.funcionarioId)
        assertEquals(2, compra.fornecedorId)
        assertEquals("Reposição", compra.observacao)
    }
}

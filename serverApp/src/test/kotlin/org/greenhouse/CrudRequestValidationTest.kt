package org.greenhouse

import jakarta.validation.Validation
import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraItemRequestDTO
import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.dto.request.FornecedorCreateRequestDTO
import org.greenhouse.dto.request.FuncionarioRequestDTO
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrudRequestValidationTest {
    private val validatorFactory = Validation.buildDefaultValidatorFactory()
    private val validator = validatorFactory.validator

    @Test
    fun `funcionario request rejects blank name and malformed CPF`() {
        val errors = validator.validate(FuncionarioRequestDTO(nome = " ", cpf = "123"))
            .map { it.propertyPath.toString() }
            .toSet()

        assertEquals(setOf("nome", "cpf"), errors)
    }

    @Test
    fun `forma de pagamento request rejects blank name and invalid limits`() {
        val errors = validator.validate(
            FormaPagamentoRequestDTO(
                nome = "",
                limiteParcelas = 0,
                prazoEstornoDias = -1,
            ),
        ).map { it.propertyPath.toString() }
            .toSet()

        assertEquals(setOf("nome", "limiteParcelas", "prazoEstornoDias"), errors)
    }

    @Test
    fun `valid requests pass Bean Validation`() {
        assertTrue(
            validator.validate(
                FuncionarioRequestDTO(nome = "Luna", cpf = "12345678901"),
            ).isEmpty(),
        )
        assertTrue(
            validator.validate(
                FormaPagamentoRequestDTO(nome = "Cartão", limiteParcelas = 1),
            ).isEmpty(),
        )
        assertTrue(
            validator.validate(
                FornecedorCreateRequestDTO(nome = "Flores do Campo", cnpj = "12345678901234"),
            ).isEmpty(),
        )
        assertTrue(
            validator.validate(
                CompraFornecedorCreateRequestDTO(
                    funcionarioId = 1,
                    fornecedorId = 1,
                    formaPagamentoId = 1,
                    itens =
                        listOf(
                            CompraItemRequestDTO(
                                produtoId = 1,
                                quantidade = 1,
                                valorUnitario = BigDecimal("10.00"),
                            ),
                        ),
                ),
            ).isEmpty(),
        )
    }

    @Test
    fun `fornecedor request rejects blank name invalid cnpj and negative lead time`() {
        val errors = validator.validate(
            FornecedorCreateRequestDTO(
                nome = " ",
                cnpj = "123",
                prazoMedioEntregaDias = -1,
            ),
        ).map { it.propertyPath.toString() }
            .toSet()

        assertEquals(setOf("nome", "cnpj", "prazoMedioEntregaDias"), errors)
    }

    @Test
    fun `fornecedor request rejects malformed email`() {
        val errors = validator.validate(
            FornecedorCreateRequestDTO(nome = "Fornecedor", email = "nao-e-email"),
        ).map { it.propertyPath.toString() }
            .toSet()

        assertEquals(setOf("email"), errors)
    }

    @Test
    fun `compra request rejects empty items invalid ids and negative freight`() {
        val errors = validator.validate(
            CompraFornecedorCreateRequestDTO(
                funcionarioId = 0,
                fornecedorId = -1,
                formaPagamentoId = 0,
                valorFrete = BigDecimal("-1.00"),
                itens = emptyList(),
            ),
        ).map { it.propertyPath.toString() }
            .toSet()

        assertEquals(setOf("funcionarioId", "fornecedorId", "formaPagamentoId", "valorFrete", "itens"), errors)
    }

    @Test
    fun `compra item request rejects invalid quantity and negative price`() {
        val errors = validator.validate(
            CompraFornecedorCreateRequestDTO(
                funcionarioId = 1,
                fornecedorId = 1,
                formaPagamentoId = 1,
                itens =
                    listOf(
                        CompraItemRequestDTO(
                            produtoId = 0,
                            quantidade = 0,
                            valorUnitario = BigDecimal("-2.00"),
                        ),
                    ),
            ),
        ).map { it.propertyPath.toString() }
            .toSet()

        assertEquals(
            setOf(
                "itens[0].produtoId",
                "itens[0].quantidade",
                "itens[0].valorUnitario",
            ),
            errors,
        )
    }

    @AfterAll
    fun closeValidatorFactory() {
        validatorFactory.close()
    }
}

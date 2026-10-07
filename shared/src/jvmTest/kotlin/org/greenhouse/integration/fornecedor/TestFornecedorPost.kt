package org.greenhouse.integration.fornecedor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Fornecedor
import org.greenhouse.integration.fixture.FornecedorFixture

class TestFornecedorPost {

    @Test
    fun `deve criar fornecedor ativo por padrao`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado = fixture.novoFornecedor()
            id = criado.id

            assertEquals(true, criado.ativo)
            assertEquals(true, criado.id != null)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve ignorar id informado na criacao`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado =
                fixture.useCase.criar(
                    Fornecedor(
                        id = -1,
                        nome = "Fornecedor com id",
                    ),
                )

            id = criado.id

            assertNotEquals(-1L, criado.id)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve criar fornecedor sem campos opcionais`() {
        val fixture = FornecedorFixture()
        var id: Long? = null

        try {
            val criado =
                fixture.useCase.criar(
                    Fornecedor(
                        nome = "Fornecedor sem opcionais",
                    ),
                )

            id = criado.id

            assertEquals(null, criado.cnpj)
            assertEquals(null, criado.telefone)
            assertEquals(null, criado.email)
            assertEquals(null, criado.contato)
        } finally {
            fixture.limpar(id)
        }
    }

    @Test
    fun `deve rejeitar nome maior que o limite da tabela`() {
        val fixture = FornecedorFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(
                Fornecedor(
                    nome = "a".repeat(151),
                ),
            )
        }
    }

    @Test
    fun `deve rejeitar cnpj com formato invalido`() {
        val fixture = FornecedorFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(
                Fornecedor(
                    nome = "Fornecedor",
                    cnpj = "123",
                ),
            )
        }
    }

    @Test
    fun `deve rejeitar cnpj contendo letras`() {
        val fixture = FornecedorFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.useCase.criar(
                Fornecedor(
                    nome = "Fornecedor",
                    cnpj = "1234567890123A",
                ),
            )
        }
    }
}
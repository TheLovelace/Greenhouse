package org.greenhouse.integration.fornecedor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import org.greenhouse.adapter.ConflictException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.FornecedorCreateRequestDTO as Fornecedor
import org.greenhouse.integration.fixture.FornecedorFixture

class TestFornecedorPost {

    @Test
    fun `deve criar fornecedor ativo por padrao`() {
        val fixture = FornecedorFixture()

        try {
            val criado = fixture.novoFornecedor()

            assertEquals(true, criado.ativo)
            assertEquals(true, criado.id > 0)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve criar fornecedor sem campos opcionais`() {
        val fixture = FornecedorFixture()

        try {
            val criado =
                fixture.criarFornecedor(
                    Fornecedor(
                        nome = "Fornecedor sem opcionais",
                    ),
                )

            assertEquals(null, criado.cnpj)
            assertEquals(null, criado.telefone)
            assertEquals(null, criado.email)
            assertEquals(null, criado.contato)
            assertEquals(null, criado.prazoMedioEntregaDias)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve persistir prazo medio de entrega`() {
        val fixture = FornecedorFixture()

        try {
            val criado = fixture.novoFornecedor(prazoMedioEntregaDias = 15)

            assertEquals(15, criado.prazoMedioEntregaDias)
            assertEquals(15, fixture.service.buscarPorId(criado.id).prazoMedioEntregaDias)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar cnpj duplicado`() {
        val fixture = FornecedorFixture()

        try {
            val criado = fixture.novoFornecedor()
            val cnpjDuplicado = criado.cnpj

            assertFailsWith<ConflictException> {
                fixture.criarFornecedor(
                    Fornecedor(
                        nome = "Outro fornecedor",
                        cnpj = cnpjDuplicado,
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar prazo medio de entrega negativo`() {
        val fixture = FornecedorFixture()

        assertFailsWith<UnprocessableEntityException> {
            fixture.criarFornecedor(
                Fornecedor(
                    nome = "Fornecedor",
                    prazoMedioEntregaDias = -1,
                ),
            )
        }
    }
}

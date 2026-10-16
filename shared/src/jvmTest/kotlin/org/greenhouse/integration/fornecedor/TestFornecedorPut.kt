package org.greenhouse.integration.fornecedor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.greenhouse.adapter.ConflictException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.dto.request.FornecedorUpdateRequestDTO
import org.greenhouse.integration.fixture.FornecedorFixture

class TestFornecedorPut {

    @Test
    fun `deve substituir todos os campos do fornecedor`() {
        val fixture = FornecedorFixture()

        try {
            val criado = fixture.novoFornecedor()

            val atualizado =
                fixture.service.substituir(
                    criado.id,
                    FornecedorUpdateRequestDTO(
                        nome = "Fornecedor substituído",
                        cnpj = "12345678901234",
                        telefone = "11988887777",
                        email = "novo@greenhouse.com",
                        contato = "Maria",
                        ativo = false,
                        prazoMedioEntregaDias = 30,
                    ),
                )

            assertEquals(criado.id, atualizado.id)
            assertEquals("Fornecedor substituído", atualizado.nome)
            assertEquals("12345678901234", atualizado.cnpj)
            assertEquals("11988887777", atualizado.telefone)
            assertEquals("novo@greenhouse.com", atualizado.email)
            assertEquals("Maria", atualizado.contato)
            assertEquals(false, atualizado.ativo)
            assertEquals(30, atualizado.prazoMedioEntregaDias)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve permitir limpar campos opcionais`() {
        val fixture = FornecedorFixture()

        try {
            val criado = fixture.novoFornecedor()

            val atualizado =
                fixture.service.substituir(
                    criado.id,
                    FornecedorUpdateRequestDTO(nome = "Somente nome"),
                )

            assertEquals("Somente nome", atualizado.nome)
            assertNull(atualizado.cnpj)
            assertNull(atualizado.telefone)
            assertNull(atualizado.prazoMedioEntregaDias)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar cnpj de outro fornecedor na substituicao`() {
        val fixture = FornecedorFixture()

        try {
            val outro = fixture.novoFornecedor()
            val atual = fixture.novoFornecedor()

            assertFailsWith<ConflictException> {
                fixture.service.substituir(
                    atual.id,
                    FornecedorUpdateRequestDTO(
                        nome = "Fornecedor",
                        cnpj = outro.cnpj,
                    ),
                )
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve lancar not found ao substituir fornecedor inexistente`() {
        val fixture = FornecedorFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.substituir(
                Long.MAX_VALUE,
                FornecedorUpdateRequestDTO(nome = "Fornecedor"),
            )
        }
    }
}

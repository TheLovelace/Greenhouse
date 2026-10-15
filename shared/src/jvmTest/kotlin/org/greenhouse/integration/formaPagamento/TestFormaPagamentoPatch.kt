package org.greenhouse.integration.formaPagamento

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.integration.fixture.FormaPagamentoFixture
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestFormaPagamentoPatch {
    @Test
    fun `deve atualizar apenas os campos enviados`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()
            val id = requireNotNull(criada.id)
            val novoNome = "Editada-${UUID.randomUUID()}"
            val atualizada = fixture.service.atualizar(id, mapOf("nome" to novoNome))

            assertEquals(criada.id, atualizada.id)
            assertEquals(novoNome, atualizada.nome)
            assertEquals(criada.ativo, atualizada.ativo)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve permitir atualizar status e rejeitar campos invalidos`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()
            val id = requireNotNull(criada.id)
            val inativada = fixture.service.atualizar(id, mapOf("ativo" to false))

            assertEquals(false, inativada.ativo)
            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(id, mapOf("campo" to "valor"))
            }
            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(id, mapOf("nome" to 123))
            }
            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(id, mapOf("ativo" to "false"))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(id, mapOf("nome" to "  "))
            }
            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(id, mapOf("id" to 10))
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacao de forma de pagamento inexistente`() {
        val fixture = FormaPagamentoFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(Long.MAX_VALUE, mapOf("nome" to "PIX"))
        }
    }

    @Test
    fun `deve atualizar e limpar campos opcionais adicionados pela migration`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento(descricao = "Original", limiteParcelas = 2, prazoEstornoDias = 7)
            val id = requireNotNull(criada.id)
            val atualizada =
                fixture.service.atualizar(
                    id,
                    mapOf("descricao" to "Atualizada", "limiteParcelas" to 6, "prazoEstornoDias" to 0),
                )

            assertEquals("Atualizada", atualizada.descricao)
            assertEquals(6, atualizada.limiteParcelas)
            assertEquals(0, atualizada.prazoEstornoDias)

            val limpa =
                fixture.service.atualizar(
                    id,
                    mapOf("descricao" to null, "prazoEstornoDias" to null),
                )
            assertEquals(null, limpa.descricao)
            assertEquals(6, limpa.limiteParcelas)
            assertEquals(null, limpa.prazoEstornoDias)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar valores invalidos dos campos novos`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()
            val id = requireNotNull(criada.id)
            assertFailsWith<BadRequestException> {
                fixture.service.atualizar(id, mapOf("limiteParcelas" to "3"))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(id, mapOf("limiteParcelas" to 0))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(id, mapOf("prazoEstornoDias" to -1))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(id, mapOf("descricao" to "a".repeat(251)))
            }
        } finally {
            fixture.limpar()
        }
    }
}

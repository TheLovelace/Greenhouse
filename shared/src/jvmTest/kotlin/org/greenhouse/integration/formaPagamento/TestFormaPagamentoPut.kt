package org.greenhouse.integration.formaPagamento

import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.FormaPagamentoDTO
import org.greenhouse.integration.fixture.FormaPagamentoFixture
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestFormaPagamentoPut {
    @Test
    fun `deve substituir todos os campos da forma de pagamento`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada =
                fixture.novaFormaPagamento(
                    descricao = "Descrição anterior",
                    limiteParcelas = 2,
                    prazoEstornoDias = 7,
                )
            val id = requireNotNull(criada.id)
            val novoNome = "Editada-${UUID.randomUUID()}"
            val atualizada =
                fixture.service.atualizar(
                    id,
                    FormaPagamentoDTO(
                        id = Long.MAX_VALUE,
                        nome = novoNome,
                        ativo = false,
                        descricao = "Nova descrição",
                        limiteParcelas = 6,
                        prazoEstornoDias = 0,
                    ),
                )

            assertEquals(criada.id, atualizada.id)
            assertEquals(novoNome, atualizada.nome)
            assertEquals(false, atualizada.ativo)
            assertEquals("Nova descrição", atualizada.descricao)
            assertEquals(6, atualizada.limiteParcelas)
            assertEquals(0, atualizada.prazoEstornoDias)
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `campos opcionais omitidos assumem os valores padrao do dto`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento(
                ativo = false,
                descricao = "Descrição anterior",
                limiteParcelas = 4,
                prazoEstornoDias = 10,
            )
            val id = requireNotNull(criada.id)
            val substituida =
                fixture.service.atualizar(
                    id,
                    FormaPagamentoDTO(nome = "PIX-${UUID.randomUUID()}"),
                )

            assertEquals(true, substituida.ativo)
            assertEquals(null, substituida.descricao)
            assertEquals(1, substituida.limiteParcelas)
            assertEquals(null, substituida.prazoEstornoDias)
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(id, FormaPagamentoDTO(nome = "  "))
            }
        } finally {
            fixture.limpar()
        }
    }

    @Test
    fun `deve rejeitar atualizacao de forma de pagamento inexistente`() {
        val fixture = FormaPagamentoFixture()

        assertFailsWith<NotFoundException> {
            fixture.service.atualizar(Long.MAX_VALUE, FormaPagamentoDTO(nome = "PIX"))
        }
    }

    @Test
    fun `deve validar limites dos campos adicionados pela migration`() {
        val fixture = FormaPagamentoFixture()

        try {
            val criada = fixture.novaFormaPagamento()
            val id = requireNotNull(criada.id)
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(id, FormaPagamentoDTO(nome = "PIX", limiteParcelas = 0))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(id, FormaPagamentoDTO(nome = "PIX", prazoEstornoDias = -1))
            }
            assertFailsWith<UnprocessableEntityException> {
                fixture.service.atualizar(id, FormaPagamentoDTO(nome = "PIX", descricao = "a".repeat(251)))
            }
        } finally {
            fixture.limpar()
        }
    }
}

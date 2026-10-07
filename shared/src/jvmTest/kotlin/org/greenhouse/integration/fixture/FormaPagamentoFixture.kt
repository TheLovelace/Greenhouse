package org.greenhouse.integration.fixture

import org.greenhouse.dto.FormaPagamentoDTO as FormaPagamento
import org.greenhouse.repository.FormaPagamentoRepository
import org.greenhouse.service.FormaPagamentoService
import java.sql.DriverManager
import java.util.UUID

class FormaPagamentoFixture {
    private val databaseUrl =
        requireNotNull(System.getenv("DATABASE_URL")) {
            "A variável DATABASE_URL é necessária para os testes de integração"
        }
    private val repository = SpringTestContext.bean<FormaPagamentoRepository>()
    private val idsCriados = mutableListOf<Long>()
    val service = SpringTestContext.bean<FormaPagamentoService>()

    fun novaFormaPagamento(
        nome: String = "Forma ${UUID.randomUUID()}",
        ativo: Boolean = true,
        id: Long? = null,
    ): FormaPagamento = criarFormaPagamento(FormaPagamento(id = id, nome = nome, ativo = ativo))

    fun criarFormaPagamento(formaPagamento: FormaPagamento): FormaPagamento =
        service
            .criar(formaPagamento)
            .also { criada ->
                idsCriados.add(requireNotNull(criada.id) { "O banco não retornou o ID da forma de pagamento" })
            }

    fun limpar() {
        if (idsCriados.isEmpty()) return

        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.prepareStatement("DELETE FROM forma_pagamento WHERE id = ?").use { statement ->
                idsCriados.forEach { id ->
                    statement.setLong(1, id)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
        }
        idsCriados.clear()
    }
}

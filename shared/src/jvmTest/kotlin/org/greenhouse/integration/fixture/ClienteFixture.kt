package org.greenhouse.integration.fixture

import org.greenhouse.adapter.DatabaseConfig
import org.greenhouse.entity.Cliente
import org.greenhouse.repository.ClienteRepository
import org.greenhouse.usecase.ClienteUseCase
import java.util.UUID

class ClienteFixture {
    private val repository = ClienteRepository(DatabaseConfig(System.getenv("DATABASE_URL")))
    val useCase = ClienteUseCase(repository)

    fun novoCliente(
        nome: String = "Cliente ${UUID.randomUUID()}",
        telefone: String = "11987654321",
        whatsapp: Boolean = false,
        cpf: String? = null,
        email: String? = null,
    ): Cliente =
        useCase.criar(
            Cliente(
                nome = nome,
                telefone = telefone,
                whatsapp = whatsapp,
                cpf = cpf,
                email = email,
            ),
        )

    fun tokenUnico(): String = UUID.randomUUID().toString()

    fun digitosUnicos(tamanho: Int = 11): String =
        (UUID.randomUUID().toString().filter { it.isDigit() } + "00000000000")
            .take(tamanho)

    fun limpar(id: Long?) {
        id?.let(repository::excluir)
    }
}

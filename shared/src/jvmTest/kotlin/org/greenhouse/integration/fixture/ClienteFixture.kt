package org.greenhouse.integration.fixture

import org.greenhouse.dto.ClienteDTO as Cliente
import org.greenhouse.repository.ClienteRepository
import org.greenhouse.service.ClienteService
import java.util.UUID

class ClienteFixture {
    private val repository = SpringTestContext.bean<ClienteRepository>()
    val service = SpringTestContext.bean<ClienteService>()

    fun novoCliente(
        nome: String = "Cliente ${UUID.randomUUID()}",
        telefone: String = "11987654321",
        whatsapp: Boolean = false,
        cpf: String? = null,
        email: String? = null,
    ): Cliente =
        service.criar(
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

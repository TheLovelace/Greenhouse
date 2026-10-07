package org.greenhouse.integration.fixture

import org.greenhouse.dto.FornecedorDTO as Fornecedor
import org.greenhouse.repository.FornecedorRepository
import org.greenhouse.service.FornecedorService
import java.util.UUID

class FornecedorFixture {
    private val repository = SpringTestContext.bean<FornecedorRepository>()

    val service = SpringTestContext.bean<FornecedorService>()

    fun novoFornecedor(
        nome: String = "Fornecedor ${UUID.randomUUID()}",
        cnpj: String? = gerarCnpj(),
        telefone: String? = "11999999999",
        email: String? = "teste-${UUID.randomUUID()}@greenhouse.com",
        contato: String? = "Contato teste",
        ativo: Boolean = true,
    ): Fornecedor =
        service.criar(
            Fornecedor(
                nome = nome,
                cnpj = cnpj,
                telefone = telefone,
                email = email,
                contato = contato,
                ativo = ativo,
            ),
        )

    fun limpar(id: Long?) {
        id?.let(repository::excluir)
    }

    private fun gerarCnpj(): String =
        UUID.randomUUID()
            .toString()
            .filter { it.isDigit() }
            .padEnd(14, '0')
            .take(14)
}
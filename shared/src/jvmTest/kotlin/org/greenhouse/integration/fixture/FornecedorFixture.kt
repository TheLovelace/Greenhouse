package org.greenhouse.integration.fixture

import org.greenhouse.dto.request.FornecedorCreateRequestDTO
import org.greenhouse.dto.response.FornecedorResponseDTO
import org.greenhouse.repository.FornecedorRepository
import org.greenhouse.service.FornecedorService
import java.util.UUID

class FornecedorFixture {
    private val repository = SpringTestContext.bean<FornecedorRepository>()
    private val idsCriados = mutableListOf<Long>()

    val service = SpringTestContext.bean<FornecedorService>()

    fun criarFornecedor(fornecedor: FornecedorCreateRequestDTO): FornecedorResponseDTO =
        service.criar(fornecedor).also { criado ->
            idsCriados.add(criado.id)
        }

    fun novoFornecedor(
        nome: String = "Fornecedor ${UUID.randomUUID()}",
        cnpj: String? = gerarCnpj(),
        telefone: String? = "11999999999",
        email: String? = "teste-${UUID.randomUUID()}@greenhouse.com",
        contato: String? = "Contato teste",
        ativo: Boolean = true,
        prazoMedioEntregaDias: Int? = 7,
    ): FornecedorResponseDTO =
        criarFornecedor(
            FornecedorCreateRequestDTO(
                nome = nome,
                cnpj = cnpj,
                telefone = telefone,
                email = email,
                contato = contato,
                ativo = ativo,
                prazoMedioEntregaDias = prazoMedioEntregaDias,
            ),
        )

    fun limpar(id: Long?) {
        id?.let(repository::excluir)
        idsCriados.remove(id)
    }

    fun limpar() {
        idsCriados.toList().forEach(repository::excluir)
        idsCriados.clear()
    }

    private fun gerarCnpj(): String =
        UUID.randomUUID()
            .toString()
            .filter { it.isDigit() }
            .padEnd(14, '0')
            .take(14)
}

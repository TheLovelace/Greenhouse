package org.greenhouse.usecase

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.Fornecedor
import org.greenhouse.repository.FornecedorRepository
import org.springframework.stereotype.Service

@Service
class FornecedorUseCase(
    private val fornecedorRepository: FornecedorRepository,
) {

    fun criar(fornecedor: Fornecedor): Fornecedor {
        validar(fornecedor)
        return fornecedorRepository.criar(fornecedor.copy(id = null))
    }

    fun listar(): List<Fornecedor> = fornecedorRepository.listar()

    fun buscarPorId(id: Long): Fornecedor =
        fornecedorRepository.buscarPorId(validarId(id))
            ?: throw NotFoundException("Fornecedor não encontrado")

    fun atualizar(id: Long, campos: Map<String, Any?>): Fornecedor {
        val idValidado = validarId(id)

        val fornecedorAtual =
            fornecedorRepository.buscarPorId(idValidado)
                ?: throw NotFoundException("Fornecedor não encontrado")

        val camposPermitidos =
            setOf(
                "id",
                "nome",
                "cnpj",
                "telefone",
                "email",
                "contato",
                "ativo",
            )

        val camposDesconhecidos = campos.keys - camposPermitidos

        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException(
                "Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}",
            )
        }

        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val fornecedorAtualizado =
            fornecedorAtual.copy(
                nome = campos.texto("nome", fornecedorAtual.nome),
                cnpj = campos.textoOpcional("cnpj", fornecedorAtual.cnpj),
                telefone = campos.textoOpcional("telefone", fornecedorAtual.telefone),
                email = campos.textoOpcional("email", fornecedorAtual.email),
                contato = campos.textoOpcional("contato", fornecedorAtual.contato),
                ativo = campos.booleano("ativo", fornecedorAtual.ativo),
            )

        validar(fornecedorAtualizado)

        return fornecedorRepository.atualizar(idValidado, fornecedorAtualizado)
            ?: throw NotFoundException("Fornecedor não encontrado")
    }

    fun excluir(id: Long) {
        if (!fornecedorRepository.excluir(validarId(id))) {
            throw NotFoundException("Fornecedor não encontrado")
        }
    }

    private fun validar(fornecedor: Fornecedor) {
        if (fornecedor.nome.length > 150) {
            throw UnprocessableEntityException(
                "O nome do fornecedor deve ter no máximo 150 caracteres",
            )
        }

        if (
            fornecedor.cnpj != null &&
            !fornecedor.cnpj.matches(Regex("^[0-9]{14}$"))
        ) {
            throw UnprocessableEntityException(
                "O CNPJ deve conter exatamente 14 números",
            )
        }

        if (fornecedor.telefone != null && fornecedor.telefone.length > 20) {
            throw UnprocessableEntityException(
                "O telefone deve ter no máximo 20 caracteres",
            )
        }

        if (fornecedor.email != null && fornecedor.email.length > 150) {
            throw UnprocessableEntityException(
                "O email deve ter no máximo 150 caracteres",
            )
        }

        if (fornecedor.contato != null && fornecedor.contato.length > 150) {
            throw UnprocessableEntityException(
                "O contato deve ter no máximo 150 caracteres",
            )
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException(
                "O id do fornecedor deve ser maior que zero",
            )
        }

        return id
    }

    private fun Map<String, Any?>.texto(
        campo: String,
        valorAtual: String,
    ): String {
        if (!containsKey(campo)) return valorAtual

        return this[campo] as? String
            ?: throw BadRequestException(
                "O campo '$campo' deve ser texto",
            )
    }

    private fun Map<String, Any?>.textoOpcional(
        campo: String,
        valorAtual: String?,
    ): String? {
        if (!containsKey(campo)) return valorAtual

        val valor = this[campo]

        if (valor == null) {
            return null
        }

        return valor as? String
            ?: throw BadRequestException(
                "O campo '$campo' deve ser texto ou nulo",
            )
    }

    private fun Map<String, Any?>.booleano(
        campo: String,
        valorAtual: Boolean,
    ): Boolean {
        if (!containsKey(campo)) return valorAtual

        return this[campo] as? Boolean
            ?: throw BadRequestException(
                "O campo '$campo' deve ser booleano",
            )
    }
}
package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.FornecedorDTO as Fornecedor
import org.greenhouse.mapper.FornecedorMapper
import org.greenhouse.repository.FornecedorRepository
import org.springframework.stereotype.Service

@Service
class FornecedorService(
    private val fornecedorRepository: FornecedorRepository,
    private val fornecedorMapper: FornecedorMapper,
) {

    fun criar(fornecedor: Fornecedor): Fornecedor {
        validar(fornecedor)
        val model = fornecedorMapper.toModel(fornecedor.copy(id = null))
        return fornecedorMapper.toDto(fornecedorRepository.criar(model))
    }

    fun listar(): List<Fornecedor> = fornecedorRepository.listar().map(fornecedorMapper::toDto)

    fun buscarPorId(id: Long): Fornecedor =
        fornecedorRepository.buscarPorId(validarId(id))
            ?.let(fornecedorMapper::toDto) ?: throw NotFoundException("Fornecedor não encontrado")

    fun atualizar(id: Long, campos: Map<String, Any?>): Fornecedor {
        val idValidado = validarId(id)

        val fornecedorAtual =
            fornecedorRepository.buscarPorId(idValidado)
                ?.let(fornecedorMapper::toDto) ?: throw NotFoundException("Fornecedor não encontrado")

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

        return fornecedorRepository.atualizar(idValidado, fornecedorMapper.toModel(fornecedorAtualizado))
            ?.let(fornecedorMapper::toDto) ?: throw NotFoundException("Fornecedor não encontrado")
    }

    fun excluir(id: Long) {
        if (!fornecedorRepository.excluir(validarId(id))) {
            throw NotFoundException("Fornecedor não encontrado")
        }
    }

    private fun validar(fornecedor: Fornecedor) {
        val cnpj = fornecedor.cnpj
        val telefone = fornecedor.telefone
        val email = fornecedor.email
        val contato = fornecedor.contato

        if (fornecedor.nome.length > 150) {
            throw UnprocessableEntityException(
                "O nome do fornecedor deve ter no máximo 150 caracteres",
            )
        }

        if (
            cnpj != null &&
            !cnpj.matches(Regex("^[0-9]{14}$"))
        ) {
            throw UnprocessableEntityException(
                "O CNPJ deve conter exatamente 14 números",
            )
        }

        if (telefone != null && telefone.length > 20) {
            throw UnprocessableEntityException(
                "O telefone deve ter no máximo 20 caracteres",
            )
        }

        if (email != null && email.length > 150) {
            throw UnprocessableEntityException(
                "O email deve ter no máximo 150 caracteres",
            )
        }

        if (contato != null && contato.length > 150) {
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
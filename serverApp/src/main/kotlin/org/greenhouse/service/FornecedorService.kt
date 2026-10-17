package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.ConflictException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.FornecedorCreateRequestDTO
import org.greenhouse.dto.request.FornecedorUpdateRequestDTO
import org.greenhouse.dto.response.FornecedorResponseDTO
import org.greenhouse.mapper.FornecedorMapper
import org.greenhouse.repository.FornecedorRepository
import org.springframework.stereotype.Service

@Service
class FornecedorService(
    private val fornecedorRepository: FornecedorRepository,
    private val fornecedorMapper: FornecedorMapper,
) {
    fun criar(fornecedor: FornecedorCreateRequestDTO): FornecedorResponseDTO {
        validarCampos(
            nome = fornecedor.nome,
            cnpj = fornecedor.cnpj,
            telefone = fornecedor.telefone,
            email = fornecedor.email,
            contato = fornecedor.contato,
            prazoMedioEntregaDias = fornecedor.prazoMedioEntregaDias,
        )
        validarCnpjDisponivel(fornecedor.cnpj, ignorarId = null)
        return fornecedorMapper.toResponseDto(
            fornecedorRepository.criar(fornecedorMapper.toModel(fornecedor)),
        )
    }

    fun listar(nome: String? = null): List<FornecedorResponseDTO> =
        (if (nome == null) fornecedorRepository.listar() else fornecedorRepository.buscarPorNome(nome))
            .map(fornecedorMapper::toResponseDto)

    fun buscarPorId(id: Long): FornecedorResponseDTO =
        fornecedorRepository.buscarPorId(validarId(id))
            ?.let(fornecedorMapper::toResponseDto) ?: throw NotFoundException("Fornecedor não encontrado")

    fun substituir(
        id: Long,
        fornecedor: FornecedorUpdateRequestDTO,
    ): FornecedorResponseDTO {
        val idValidado = validarId(id)
        if (fornecedorRepository.buscarPorId(idValidado) == null) {
            throw NotFoundException("Fornecedor não encontrado")
        }

        validarCampos(
            nome = fornecedor.nome,
            cnpj = fornecedor.cnpj,
            telefone = fornecedor.telefone,
            email = fornecedor.email,
            contato = fornecedor.contato,
            prazoMedioEntregaDias = fornecedor.prazoMedioEntregaDias,
        )
        validarCnpjDisponivel(fornecedor.cnpj, ignorarId = idValidado)

        return fornecedorRepository.atualizar(idValidado, fornecedorMapper.toModel(fornecedor))
            ?.let(fornecedorMapper::toResponseDto) ?: throw NotFoundException("Fornecedor não encontrado")
    }

    fun atualizar(
        id: Long,
        campos: Map<String, Any?>,
    ): FornecedorResponseDTO {
        val idValidado = validarId(id)
        val fornecedorAtual =
            fornecedorRepository.buscarPorId(idValidado)
                ?.let(fornecedorMapper::toResponseDto) ?: throw NotFoundException("Fornecedor não encontrado")

        val camposPermitidos =
            setOf(
                "id",
                "nome",
                "cnpj",
                "telefone",
                "email",
                "contato",
                "ativo",
                "prazoMedioEntregaDias",
            )
        val camposDesconhecidos = campos.keys - camposPermitidos
        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val fornecedorAtualizado =
            FornecedorUpdateRequestDTO(
                nome = campos.texto("nome", fornecedorAtual.nome),
                cnpj = campos.textoOpcional("cnpj", fornecedorAtual.cnpj),
                telefone = campos.textoOpcional("telefone", fornecedorAtual.telefone),
                email = campos.textoOpcional("email", fornecedorAtual.email),
                contato = campos.textoOpcional("contato", fornecedorAtual.contato),
                ativo = campos.booleano("ativo", fornecedorAtual.ativo),
                prazoMedioEntregaDias =
                    campos.inteiroOpcional(
                        "prazoMedioEntregaDias",
                        fornecedorAtual.prazoMedioEntregaDias,
                    ),
            )
        validar(fornecedorAtualizado)
        validarCnpjDisponivel(fornecedorAtualizado.cnpj, ignorarId = idValidado)

        return fornecedorRepository.atualizar(idValidado, fornecedorMapper.toModel(fornecedorAtualizado))
            ?.let(fornecedorMapper::toResponseDto) ?: throw NotFoundException("Fornecedor não encontrado")
    }

    fun excluir(id: Long) {
        val idValidado = validarId(id)
        if (fornecedorRepository.buscarPorId(idValidado) == null) {
            throw NotFoundException("Fornecedor não encontrado")
        }
        if (fornecedorRepository.possuiCompras(idValidado)) {
            throw UnprocessableEntityException(
                "Fornecedor não pode ser excluído porque possui compras vinculadas",
            )
        }
        if (!fornecedorRepository.excluir(idValidado)) {
            throw NotFoundException("Fornecedor não encontrado")
        }
    }

    private fun validar(fornecedor: FornecedorUpdateRequestDTO) =
        validarCampos(
            nome = fornecedor.nome,
            cnpj = fornecedor.cnpj,
            telefone = fornecedor.telefone,
            email = fornecedor.email,
            contato = fornecedor.contato,
            prazoMedioEntregaDias = fornecedor.prazoMedioEntregaDias,
        )

    private fun validarCampos(
        nome: String,
        cnpj: String?,
        telefone: String?,
        email: String?,
        contato: String?,
        prazoMedioEntregaDias: Int?,
    ) {
        if (nome.isBlank()) {
            throw UnprocessableEntityException("O nome do fornecedor é obrigatório")
        }
        if (nome.length > 150) {
            throw UnprocessableEntityException("O nome do fornecedor deve ter no máximo 150 caracteres")
        }
        if (cnpj != null && !cnpj.matches(Regex("^[0-9]{14}$"))) {
            throw UnprocessableEntityException("O CNPJ deve conter exatamente 14 números")
        }
        if ((telefone?.length ?: 0) > 20) {
            throw UnprocessableEntityException("O telefone deve ter no máximo 20 caracteres")
        }
        if ((email?.length ?: 0) > 150) {
            throw UnprocessableEntityException("O email deve ter no máximo 150 caracteres")
        }
        if ((contato?.length ?: 0) > 150) {
            throw UnprocessableEntityException("O contato deve ter no máximo 150 caracteres")
        }
        if ((prazoMedioEntregaDias ?: 0) < 0) {
            throw UnprocessableEntityException("O prazo médio de entrega não pode ser negativo")
        }
    }

    private fun validarCnpjDisponivel(
        cnpj: String?,
        ignorarId: Long?,
    ) {
        if (cnpj != null && fornecedorRepository.cnpjEmUso(cnpj, ignorarId)) {
            throw ConflictException("Já existe um fornecedor cadastrado com este CNPJ")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id do fornecedor deve ser maior que zero")
        }
        return id
    }

    private fun Map<String, Any?>.texto(
        campo: String,
        valorAtual: String,
    ): String {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto")
    }

    private fun Map<String, Any?>.textoOpcional(
        campo: String,
        valorAtual: String?,
    ): String? {
        if (!containsKey(campo)) return valorAtual
        val valorInformado = this[campo] ?: return null
        return valorInformado as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto ou null")
    }

    private fun Map<String, Any?>.booleano(
        campo: String,
        valorAtual: Boolean,
    ): Boolean {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? Boolean
            ?: throw BadRequestException("O campo '$campo' deve ser booleano")
    }

    private fun Map<String, Any?>.inteiroOpcional(
        campo: String,
        valorAtual: Int?,
    ): Int? {
        if (!containsKey(campo)) return valorAtual
        val valorInformado = this[campo] ?: return null
        val numero =
            valorInformado as? Number
                ?: throw BadRequestException("O campo '$campo' deve ser um número inteiro ou null")
        return try {
            numero.toString().toBigDecimal().intValueExact()
        } catch (e: NumberFormatException) {
            throw BadRequestException("O campo '$campo' deve ser um número inteiro", e)
        } catch (e: ArithmeticException) {
            throw BadRequestException("O campo '$campo' deve ser um número inteiro dentro do limite suportado", e)
        }
    }
}

package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.DescontoRequestDTO
import org.greenhouse.dto.response.DescontoResponseDTO
import org.greenhouse.mapper.DescontoMapper
import org.greenhouse.enums.TipoDesconto
import org.greenhouse.repository.DescontoRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

private val VALOR_MAXIMO_DESCONTO = BigDecimal("9999999999.99")

@Service
class DescontoService(
    private val descontoRepository: DescontoRepository,
    private val descontoMapper: DescontoMapper,
) {
    fun criar(desconto: DescontoRequestDTO): DescontoResponseDTO {
        validar(desconto)
        return descontoMapper.toResponseDto(descontoRepository.criar(descontoMapper.toModel(desconto)))
    }

    fun listar(): List<DescontoResponseDTO> =
        descontoRepository.listar().map(descontoMapper::toResponseDto)

    fun buscarPorId(id: Long): DescontoResponseDTO =
        descontoRepository.buscarPorId(validarId(id))
            ?.let(descontoMapper::toResponseDto) ?: throw NotFoundException("Desconto não encontrado")

    fun substituir(
        id: Long,
        desconto: DescontoRequestDTO,
    ): DescontoResponseDTO {
        val idValidado = validarId(id)
        if (descontoRepository.buscarPorId(idValidado) == null) {
            throw NotFoundException("Desconto não encontrado")
        }

        validar(desconto)

        return descontoRepository.atualizar(idValidado, descontoMapper.toModel(desconto))
            ?.let(descontoMapper::toResponseDto) ?: throw NotFoundException("Desconto não encontrado")
    }

    fun atualizar(
        id: Long,
        campos: Map<String, Any?>,
    ): DescontoResponseDTO {
        val idValidado = validarId(id)
        val descontoAtual =
            descontoRepository.buscarPorId(idValidado)
                ?.let(descontoMapper::toResponseDto) ?: throw NotFoundException("Desconto não encontrado")

        val camposPermitidos =
            setOf(
                "id",
                "titulo",
                "descricao",
                "tipo",
                "valor",
                "dataInicio",
                "dataFim",
                "ativo",
                "quantidadeMinimaItens",
            )
        val camposDesconhecidos = campos.keys - camposPermitidos
        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val descontoAtualizado =
            DescontoRequestDTO(
                titulo = campos.texto("titulo", descontoAtual.titulo),
                descricao = campos.textoOpcional("descricao", descontoAtual.descricao),
                tipo = campos.tipo("tipo", descontoAtual.tipo),
                valor = campos.valor("valor", descontoAtual.valor),
                dataInicio = campos.data("dataInicio", descontoAtual.dataInicio),
                dataFim = campos.data("dataFim", descontoAtual.dataFim),
                ativo = campos.booleano("ativo", descontoAtual.ativo),
                quantidadeMinimaItens =
                    campos.quantidadeMinimaItens(
                        "quantidadeMinimaItens",
                        descontoAtual.quantidadeMinimaItens,
                    ),
            )
        validar(descontoAtualizado)

        return descontoRepository.atualizar(idValidado, descontoMapper.toModel(descontoAtualizado))
            ?.let(descontoMapper::toResponseDto) ?: throw NotFoundException("Desconto não encontrado")
    }

    fun excluir(id: Long) {
        if (!descontoRepository.inativar(validarId(id))) {
            throw NotFoundException("Desconto não encontrado")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id do desconto deve ser maior que zero")
        }
        return id
    }

    private fun validar(desconto: DescontoRequestDTO) {
        if (desconto.titulo.isBlank()) {
            throw UnprocessableEntityException("O título do desconto é obrigatório")
        }
        if (desconto.titulo.length > 150) {
            throw UnprocessableEntityException("O título do desconto deve ter no máximo 150 caracteres")
        }
        if (desconto.descricao != null && desconto.descricao.length > 1000) {
            throw UnprocessableEntityException("A descrição do desconto deve ter no máximo 1000 caracteres")
        }
        if (desconto.quantidadeMinimaItens < 1) {
            throw UnprocessableEntityException("A quantidade mínima de itens deve ser maior que zero")
        }
        if (desconto.valor <= BigDecimal.ZERO) {
            throw UnprocessableEntityException("O valor do desconto deve ser maior que zero")
        }
        if (desconto.tipo == TipoDesconto.PERCENTUAL && desconto.valor > BigDecimal("100")) {
            throw UnprocessableEntityException("O desconto percentual não pode ser maior que 100")
        }
        if (desconto.valor > VALOR_MAXIMO_DESCONTO ||
            desconto.valor.stripTrailingZeros().scale() > 2
        ) {
            throw UnprocessableEntityException("O valor deve respeitar o formato NUMERIC(12,2)")
        }
        val dataInicio = desconto.dataInicio
        val dataFim = desconto.dataFim
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw UnprocessableEntityException("A data final não pode ser anterior à data inicial")
        }
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

    private fun Map<String, Any?>.quantidadeMinimaItens(
        campo: String,
        valorAtual: Int,
    ): Int {
        if (!containsKey(campo)) return valorAtual
        val valorInformado =
            this[campo] as? Number
                ?: throw BadRequestException("O campo '$campo' deve ser um número inteiro")
        return try {
            BigDecimal(valorInformado.toString()).intValueExact()
        } catch (e: NumberFormatException) {
            throw BadRequestException("O campo '$campo' deve ser um número inteiro", e)
        } catch (e: ArithmeticException) {
            throw BadRequestException("O campo '$campo' deve ser um número inteiro dentro do limite suportado", e)
        }
    }

    private fun Map<String, Any?>.tipo(
        campo: String,
        valorAtual: TipoDesconto,
    ): TipoDesconto {
        if (!containsKey(campo)) return valorAtual
        val tipoInformado =
            this[campo] as? String
                ?: throw BadRequestException("O campo '$campo' deve ser PERCENTUAL ou VALOR_FIXO")
        return try {
            TipoDesconto.valueOf(tipoInformado)
        } catch (e: IllegalArgumentException) {
            throw UnprocessableEntityException("O tipo do desconto deve ser PERCENTUAL ou VALOR_FIXO", e)
        }
    }

    private fun Map<String, Any?>.valor(
        campo: String,
        valorAtual: BigDecimal,
    ): BigDecimal {
        if (!containsKey(campo)) return valorAtual
        val valorInformado = this[campo]
        if (valorInformado !is Number) {
            throw BadRequestException("O campo '$campo' deve ser numérico")
        }
        return try {
            BigDecimal(valorInformado.toString())
        } catch (e: NumberFormatException) {
            throw BadRequestException("O campo '$campo' deve ser numérico", e)
        }
    }

    private fun Map<String, Any?>.data(
        campo: String,
        valorAtual: OffsetDateTime?,
    ): OffsetDateTime? {
        if (!containsKey(campo)) return valorAtual
        val dataInformada = this[campo]
        if (dataInformada == null) return null
        val texto =
            dataInformada as? String
                ?: throw BadRequestException("O campo '$campo' deve ser uma data ISO-8601 ou null")
        return try {
            OffsetDateTime.parse(texto)
        } catch (e: DateTimeParseException) {
            throw BadRequestException("O campo '$campo' deve ser uma data ISO-8601 válida", e)
        }
    }
}

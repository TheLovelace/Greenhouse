package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.ConflictException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.request.EstoqueCreateRequestDTO
import org.greenhouse.dto.request.EstoqueUpdateRequestDTO
import org.greenhouse.dto.response.EstoqueResponseDTO
import org.greenhouse.mapper.EstoqueMapper
import org.greenhouse.model.Estoque
import org.greenhouse.repository.EstoqueRepository
import org.greenhouse.repository.ProdutoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class EstoqueService(
    private val estoqueRepository: EstoqueRepository,
    private val produtoRepository: ProdutoRepository,
    private val estoqueMapper: EstoqueMapper,
) {
    @Transactional(rollbackFor = [Exception::class])
    fun criarPosicao(estoque: EstoqueCreateRequestDTO): EstoqueResponseDTO {
        validarId(estoque.produtoId)
        val produto =
            produtoRepository.buscarParaAtualizar(estoque.produtoId)
                ?: throw NotFoundException("Produto não encontrado")

        if (estoqueRepository.buscarParaAtualizar(estoque.produtoId) != null) {
            throw ConflictException("Já existe uma posição de estoque para este produto")
        }
        if (estoque.quantidadeFisica < 0) {
            throw UnprocessableEntityException("A quantidade física não pode ser negativa")
        }
        if (estoque.quantidadeMinima < 0) {
            throw UnprocessableEntityException("A quantidade mínima não pode ser negativa")
        }
        if ((estoque.localizacao?.length ?: 0) > 100) {
            throw UnprocessableEntityException("A localização deve ter no máximo 100 caracteres")
        }

        val novaPosicao =
            Estoque(
                produtoId = requireNotNull(produto.id),
                quantidadeFisica = estoque.quantidadeFisica,
                quantidadeMinima = estoque.quantidadeMinima,
                localizacao = estoque.localizacao,
                bloqueado = estoque.bloqueado,
            )
        return estoqueMapper.toResponseDto(estoqueRepository.save(novaPosicao))
    }

    fun listarPosicoes(): List<EstoqueResponseDTO> =
        estoqueRepository.listar().map(estoqueMapper::toResponseDto)

    @Transactional(rollbackFor = [Exception::class])
    fun excluirPosicao(produtoId: Long) {
        validarId(produtoId)
        produtoRepository.buscarParaAtualizar(produtoId)
            ?: throw NotFoundException("Produto não encontrado")
        val estoque =
            estoqueRepository.buscarParaAtualizar(produtoId)
                ?: throw NotFoundException("Posição de estoque não encontrada")

        if (estoque.quantidadeReservada > 0) {
            throw UnprocessableEntityException(
                "A posição de estoque não pode ser excluída enquanto houver quantidade reservada",
            )
        }

        estoqueRepository.delete(estoque)
    }

    fun buscarPosicaoPorProdutoId(produtoId: Long): EstoqueResponseDTO {
        validarId(produtoId)
        if (produtoRepository.buscarPorId(produtoId) == null) {
            throw NotFoundException("Produto não encontrado")
        }

        return estoqueRepository.buscarPorProdutoId(produtoId)?.let(estoqueMapper::toResponseDto)
            ?: EstoqueResponseDTO(
                produtoId = produtoId,
                quantidadeFisica = 0,
                quantidadeReservada = 0,
                quantidadeDisponivel = 0,
                quantidadeMinima = 0,
                localizacao = null,
                bloqueado = false,
            )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun substituirPosicao(
        produtoId: Long,
        atualizacao: EstoqueUpdateRequestDTO,
    ): EstoqueResponseDTO {
        validarId(produtoId)
        val produto =
            produtoRepository.buscarParaAtualizar(produtoId)
                ?: throw NotFoundException("Produto não encontrado")
        val estoque =
            estoqueRepository.buscarParaAtualizar(produtoId)
                ?: throw NotFoundException("Posição de estoque não encontrada")

        if (atualizacao.quantidadeFisica < 0) {
            throw UnprocessableEntityException("A quantidade física não pode ser negativa")
        }
        if (atualizacao.quantidadeMinima < 0) {
            throw UnprocessableEntityException("A quantidade mínima não pode ser negativa")
        }
        if ((atualizacao.localizacao?.length ?: 0) > 100) {
            throw UnprocessableEntityException("A localização deve ter no máximo 100 caracteres")
        }
        if (atualizacao.quantidadeFisica < estoque.quantidadeReservada) {
            throw UnprocessableEntityException(
                "A quantidade física não pode ser menor que a quantidade reservada",
            )
        }

        estoque.produtoId = requireNotNull(produto.id)
        estoque.quantidadeFisica = atualizacao.quantidadeFisica
        estoque.quantidadeMinima = atualizacao.quantidadeMinima
        estoque.localizacao = atualizacao.localizacao
        estoque.bloqueado = atualizacao.bloqueado

        return estoqueMapper.toResponseDto(estoqueRepository.save(estoque))
    }

    @Transactional(rollbackFor = [Exception::class])
    fun atualizarPosicao(
        produtoId: Long,
        campos: Map<String, Any?>,
    ): EstoqueResponseDTO {
        validarId(produtoId)
        val produto =
            produtoRepository.buscarParaAtualizar(produtoId)
                ?: throw NotFoundException("Produto não encontrado")

        val camposPermitidos = setOf("quantidadeFisica", "quantidadeMinima", "localizacao", "bloqueado")
        val camposDesconhecidos = campos.keys - camposPermitidos
        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.isEmpty()) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val estoque =
            estoqueRepository.buscarParaAtualizar(produtoId)
                ?: Estoque(produtoId = requireNotNull(produto.id))

        if (campos.containsKey("quantidadeFisica")) {
            estoque.quantidadeFisica = campos.inteiro("quantidadeFisica")
        }
        if (campos.containsKey("quantidadeMinima")) {
            estoque.quantidadeMinima = campos.inteiro("quantidadeMinima")
        }
        if (campos.containsKey("localizacao")) {
            estoque.localizacao = campos.textoOpcional("localizacao")
        }
        if (campos.containsKey("bloqueado")) {
            estoque.bloqueado = campos.booleano("bloqueado")
        }

        if (estoque.quantidadeFisica < 0) {
            throw UnprocessableEntityException("A quantidade física não pode ser negativa")
        }
        if (estoque.quantidadeMinima < 0) {
            throw UnprocessableEntityException("A quantidade mínima não pode ser negativa")
        }
        if ((estoque.localizacao?.length ?: 0) > 100) {
            throw UnprocessableEntityException("A localização deve ter no máximo 100 caracteres")
        }
        if (estoque.quantidadeFisica < estoque.quantidadeReservada) {
            throw UnprocessableEntityException(
                "A quantidade física não pode ser menor que a quantidade reservada",
            )
        }

        return estoqueMapper.toResponseDto(estoqueRepository.save(estoque))
    }

    @Transactional(rollbackFor = [Exception::class])
    fun validarDisponibilidade(
        produtoId: Long,
        quantidade: Int,
    ) {
        val estoque = estoqueParaPedido(produtoId, quantidade)
        validarNaoBloqueado(estoque)
        validarDisponivel(estoque, quantidade)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun reservar(
        produtoId: Long,
        quantidade: Int,
    ): EstoqueResponseDTO {
        val estoque = estoqueParaPedido(produtoId, quantidade)
        validarNaoBloqueado(estoque)
        validarDisponivel(estoque, quantidade)

        val novaQuantidadeReservada = estoque.quantidadeReservada.toLong() + quantidade
        if (novaQuantidadeReservada > Int.MAX_VALUE) {
            throw UnprocessableEntityException("A quantidade reservada excede o limite suportado pelo estoque")
        }
        estoque.quantidadeReservada = novaQuantidadeReservada.toInt()
        return estoqueMapper.toResponseDto(estoqueRepository.save(estoque))
    }

    @Transactional(rollbackFor = [Exception::class])
    fun liberarReserva(
        produtoId: Long,
        quantidade: Int,
    ): EstoqueResponseDTO {
        val estoque = estoqueParaPedido(produtoId, quantidade)
        if (estoque.quantidadeReservada < quantidade) {
            throw UnprocessableEntityException("A quantidade reservada é insuficiente para liberar")
        }

        estoque.quantidadeReservada -= quantidade
        return estoqueMapper.toResponseDto(estoqueRepository.save(estoque))
    }

    @Transactional(rollbackFor = [Exception::class])
    fun concluirPedidoAgendado(
        produtoId: Long,
        quantidade: Int,
    ): EstoqueResponseDTO {
        val estoque = estoqueParaPedido(produtoId, quantidade)
        if (estoque.quantidadeReservada < quantidade) {
            throw UnprocessableEntityException("A quantidade reservada é insuficiente para concluir o pedido")
        }

        estoque.quantidadeFisica -= quantidade
        estoque.quantidadeReservada -= quantidade
        return estoqueMapper.toResponseDto(estoqueRepository.save(estoque))
    }

    @Transactional(rollbackFor = [Exception::class])
    fun concluirPedidoImediato(
        produtoId: Long,
        quantidade: Int,
    ): EstoqueResponseDTO {
        val estoque = estoqueParaPedido(produtoId, quantidade)
        validarNaoBloqueado(estoque)
        validarDisponivel(estoque, quantidade)

        estoque.quantidadeFisica -= quantidade
        return estoqueMapper.toResponseDto(estoqueRepository.save(estoque))
    }

    private fun estoqueParaPedido(
        produtoId: Long,
        quantidade: Int,
    ): Estoque {
        validarId(produtoId)
        if (quantidade <= 0) {
            throw BadRequestException("A quantidade deve ser maior que zero")
        }
        val produto =
            produtoRepository.buscarParaAtualizar(produtoId)
                ?: throw NotFoundException("Produto não encontrado")
        return estoqueRepository.buscarParaAtualizar(produtoId)
            ?: Estoque(produtoId = requireNotNull(produto.id))
    }

    private fun validarDisponivel(
        estoque: Estoque,
        quantidade: Int,
    ) {
        if (estoque.quantidadeFisica - estoque.quantidadeReservada < quantidade) {
            throw UnprocessableEntityException("Estoque disponível insuficiente")
        }
    }

    private fun validarNaoBloqueado(estoque: Estoque) {
        if (estoque.bloqueado) {
            throw UnprocessableEntityException("O estoque deste produto está bloqueado")
        }
    }

    private fun validarId(produtoId: Long) {
        if (produtoId <= 0) {
            throw BadRequestException("O id do produto deve ser maior que zero")
        }
    }

    private fun Map<String, Any?>.inteiro(campo: String): Int {
        val valor = this[campo] as? Number
            ?: throw BadRequestException("O campo '$campo' deve ser um número inteiro")
        return try {
            BigDecimal(valor.toString()).intValueExact()
        } catch (e: NumberFormatException) {
            throw BadRequestException("O campo '$campo' deve ser um número inteiro", e)
        } catch (e: ArithmeticException) {
            throw BadRequestException("O campo '$campo' deve ser um número inteiro dentro do limite suportado", e)
        }
    }

    private fun Map<String, Any?>.textoOpcional(campo: String): String? {
        val valor = this[campo] ?: return null
        return valor as? String ?: throw BadRequestException("O campo '$campo' deve ser texto ou null")
    }

    private fun Map<String, Any?>.booleano(campo: String): Boolean =
        this[campo] as? Boolean ?: throw BadRequestException("O campo '$campo' deve ser booleano")
}

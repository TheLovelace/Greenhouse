package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.ConflictException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.EspecieRequestDTO
import org.greenhouse.dto.EspecieResponseDTO
import org.greenhouse.mapper.EspecieMapper
import org.greenhouse.model.Especie
import org.greenhouse.repository.EspecieRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class EspecieService(
    private val repository: EspecieRepository,
    private val mapper: EspecieMapper,
) {

    fun cadastrar(dto: EspecieRequestDTO): EspecieResponseDTO {
        validar(dto)
        return mapper.toResponseDto(repository.save(mapper.toEntity(dto)))
    }

    fun listar(): List<EspecieResponseDTO> =
        repository.findAll().map(mapper::toResponseDto)

    fun buscarPorId(id: Long): EspecieResponseDTO =
        mapper.toResponseDto(buscarEntidade(id))

    fun buscarPorNomeParcial(nome: String): List<EspecieResponseDTO> =
        repository.buscarPorNomePopular(nome.trim()).map(mapper::toResponseDto)

    fun atualizar(id: Long, dto: EspecieRequestDTO): EspecieResponseDTO {
        validar(dto)
        val especie = buscarEntidade(id)
        mapper.updateEntity(especie, dto)
        return mapper.toResponseDto(repository.save(especie))
    }

    fun alterarStatus(id: Long): EspecieResponseDTO {
        val especie = buscarEntidade(id)
        especie.ativo = !especie.ativo
        return mapper.toResponseDto(repository.save(especie))
    }

    fun remover(id: Long) {
        val especie = buscarEntidade(id)

        // Preserva o histórico: espécie em uso só pode ser inativada, nunca apagada.
        if (repository.contarAlertas(id) > 0) {
            throw ConflictException("A espécie possui regras de alerta vinculadas; inative-a em vez de removê-la")
        }
        if (repository.contarProdutosVinculados(id) > 0) {
            throw ConflictException("A espécie está vinculada a produtos; inative-a em vez de removê-la")
        }

        repository.delete(especie)
    }

    private fun buscarEntidade(id: Long): Especie {
        if (id <= 0) throw BadRequestException("O id da espécie deve ser maior que zero")
        return repository.findById(id).orElseThrow { NotFoundException("Espécie não encontrada") }
    }

    // Espelha as CHECK constraints da tabela para devolver 400 em vez de erro de banco.
    private fun validar(dto: EspecieRequestDTO) {
        verificarFaixa(dto.temperaturaMin, dto.temperaturaMax, "A temperatura mínima não pode ser maior que a máxima")
        verificarFaixa(dto.umidadeArMin, dto.umidadeArMax, "A umidade do ar mínima não pode ser maior que a máxima")
        verificarFaixa(dto.umidadeSoloMin, dto.umidadeSoloMax, "A umidade do solo mínima não pode ser maior que a máxima")
    }

    private fun verificarFaixa(min: BigDecimal?, max: BigDecimal?, mensagem: String) {
        if (min != null && max != null && min > max) throw UnprocessableEntityException(mensagem)
    }
}
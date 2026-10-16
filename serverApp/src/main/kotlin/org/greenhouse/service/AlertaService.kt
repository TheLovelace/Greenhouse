package org.greenhouse.service

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.dto.AlertaRequestDTO
import org.greenhouse.dto.AlertaResponseDTO
import org.greenhouse.dto.DispositivoIotResponseDTO
import org.greenhouse.mapper.AlertaMapper
import org.greenhouse.model.Alerta
import org.greenhouse.model.DispositivoIot
import org.greenhouse.model.Especie
import org.greenhouse.repository.AlertaRepository
import org.greenhouse.repository.DispositivoIotRepository
import org.greenhouse.repository.EspecieRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AlertaService(
    private val repository: AlertaRepository,
    private val dispositivoRepository: DispositivoIotRepository,
    private val especieRepository: EspecieRepository,
    private val mapper: AlertaMapper,
) {

    @Transactional
    fun cadastrar(dto: AlertaRequestDTO): AlertaResponseDTO {
        validar(dto)
        val dispositivo = buscarDispositivo(dto.dispositivoIotId)
        val especie = buscarEspecie(dto.especieId)
        exigirDispositivoAtivo(dispositivo)
        exigirEspecieAtiva(especie)

        val alerta = mapper.toEntity(dto, dispositivo, especie)
        return mapper.toResponseDto(repository.save(alerta))
    }

    @Transactional(readOnly = true)
    fun listar(
        titulo: String? = null,
        ativo: Boolean? = null,
        dispositivoId: Long? = null,
        especieId: Long? = null,
    ): List<AlertaResponseDTO> {
        val filtroTitulo = titulo?.trim()?.takeIf { it.isNotEmpty() }
        val base = if (filtroTitulo != null) repository.buscarPorTitulo(filtroTitulo) else repository.listarTodos()

        return base
            .filter { ativo == null || it.ativo == ativo }
            .filter { dispositivoId == null || it.dispositivo.id == dispositivoId }
            .filter { especieId == null || it.especie.id == especieId }
            .map(mapper::toResponseDto)
    }

    @Transactional(readOnly = true)
    fun listarDispositivos(): List<DispositivoIotResponseDTO> =
        dispositivoRepository.findByAtivoTrueOrderByIdAsc().map(mapper::toDispositivoDto)

    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): AlertaResponseDTO =
        mapper.toResponseDto(buscarEntidade(id))

    @Transactional
    fun atualizar(id: Long, dto: AlertaRequestDTO): AlertaResponseDTO {
        validar(dto)
        val alerta = buscarEntidade(id)
        val dispositivo = buscarDispositivo(dto.dispositivoIotId)
        val especie = buscarEspecie(dto.especieId)

        // Só exige "ativo" de quem foi trocado; editar título/mensagem não deve ser bloqueado.
        if (dispositivo.id != alerta.dispositivo.id) exigirDispositivoAtivo(dispositivo)
        if (especie.id != alerta.especie.id) exigirEspecieAtiva(especie)

        mapper.updateEntity(alerta, dto, dispositivo, especie)
        return mapper.toResponseDto(repository.save(alerta))
    }

    @Transactional
    fun alternarStatus(id: Long): AlertaResponseDTO {
        val alerta = buscarEntidade(id)

        // Inativar é sempre permitido; reativar exige dispositivo e espécie ativos.
        if (!alerta.ativo) {
            exigirDispositivoAtivo(alerta.dispositivo)
            exigirEspecieAtiva(alerta.especie)
        }

        alerta.ativo = !alerta.ativo
        return mapper.toResponseDto(repository.save(alerta))
    }

    // Espelha as CHECK constraints da tabela para devolver 400 em vez de erro de banco.
    private fun validar(dto: AlertaRequestDTO) {
        if (!dto.monitorarTemperatura && !dto.monitorarUmidadeAr && !dto.monitorarUmidadeSolo) {
            throw UnprocessableEntityException("Selecione ao menos uma condição para monitorar")
        }
        if (dto.titulo.isBlank()) {
            throw UnprocessableEntityException("O título do alerta é obrigatório")
        }
        if (dto.prioridade !in 1..3) {
            throw UnprocessableEntityException("A prioridade deve ser 1, 2 ou 3")
        }
    }

    private fun buscarEntidade(id: Long): Alerta {
        if (id <= 0) throw BadRequestException("O ID do alerta deve ser maior que zero")
        return repository.buscarComRelacionamentos(id) ?: throw NotFoundException("Alerta não encontrado")
    }

    private fun buscarDispositivo(id: Long): DispositivoIot =
        dispositivoRepository.findById(id).orElseThrow { NotFoundException("Dispositivo IoT não encontrado") }

    private fun buscarEspecie(id: Long): Especie =
        especieRepository.findById(id).orElseThrow { NotFoundException("Espécie não encontrada") }

    private fun exigirDispositivoAtivo(dispositivo: DispositivoIot) {
        if (!dispositivo.ativo) throw UnprocessableEntityException("O dispositivo IoT está inativo")
    }

    private fun exigirEspecieAtiva(especie: Especie) {
        if (!especie.ativo) throw UnprocessableEntityException("A espécie está inativa")
    }
}
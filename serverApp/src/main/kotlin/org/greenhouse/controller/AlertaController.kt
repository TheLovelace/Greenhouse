package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.AlertaRequestDTO
import org.greenhouse.dto.AlertaResponseDTO
import org.greenhouse.dto.DispositivoIotResponseDTO
import org.greenhouse.service.AlertaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/alertas")
class AlertaController(
    private val service: AlertaService,
) {

    @PostMapping
    fun cadastrar(@RequestBody @Valid dto: AlertaRequestDTO): ResponseEntity<AlertaResponseDTO> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(dto))

    @GetMapping
    fun listar(
        @RequestParam(required = false) titulo: String?,
        @RequestParam(required = false) ativo: Boolean?,
        @RequestParam(required = false) dispositivoId: Long?,
        @RequestParam(required = false) especieId: Long?,
    ): List<AlertaResponseDTO> = service.listar(titulo, ativo, dispositivoId, especieId)

    /** Dispositivos disponíveis para vincular a uma regra (alimenta o formulário). */
    @GetMapping("/dispositivos")
    fun listarDispositivos(): List<DispositivoIotResponseDTO> = service.listarDispositivos()

    @GetMapping("/{id}")
    fun consultar(@PathVariable id: Long): AlertaResponseDTO = service.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody @Valid dto: AlertaRequestDTO,
    ): AlertaResponseDTO = service.atualizar(id, dto)

    @PatchMapping("/{id}/status")
    fun alternarStatus(@PathVariable id: Long): AlertaResponseDTO = service.alternarStatus(id)
}
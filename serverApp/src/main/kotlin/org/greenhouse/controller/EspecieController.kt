package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.EspecieRequestDTO
import org.greenhouse.dto.EspecieResponseDTO
import org.greenhouse.service.EspecieService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
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
@RequestMapping("/especies")
class EspecieController(
    private val service: EspecieService,
) {

    @PostMapping
    fun cadastrar(@RequestBody @Valid dto: EspecieRequestDTO): ResponseEntity<EspecieResponseDTO> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(dto))

    @GetMapping
    fun listar(@RequestParam(required = false) nome: String?): List<EspecieResponseDTO> =
        if (!nome.isNullOrBlank()) service.buscarPorNomeParcial(nome) else service.listar()

    @GetMapping("/{id}")
    fun consultar(@PathVariable id: Long): EspecieResponseDTO = service.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody @Valid dto: EspecieRequestDTO,
    ): EspecieResponseDTO = service.atualizar(id, dto)

    @PatchMapping("/{id}/status")
    fun alternarStatus(@PathVariable id: Long): EspecieResponseDTO = service.alterarStatus(id)

    @DeleteMapping("/{id}")
    fun remover(@PathVariable id: Long): ResponseEntity<Unit> {
        service.remover(id)
        return ResponseEntity.noContent().build()
    }
}
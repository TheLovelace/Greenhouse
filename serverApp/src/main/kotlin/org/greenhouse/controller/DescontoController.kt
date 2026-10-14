package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.DescontoRequestDTO
import org.greenhouse.dto.response.DescontoResponseDTO
import org.greenhouse.service.DescontoService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/desconto")
class DescontoController(
    private val descontoUseCase: DescontoService,
) {
    @PostMapping
    fun criar(
        @Valid
        @RequestBody desconto: DescontoRequestDTO,
    ): ResponseEntity<DescontoResponseDTO> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(descontoUseCase.criar(desconto))

    @GetMapping
    fun listar(): List<DescontoResponseDTO> = descontoUseCase.listar()

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
    ): DescontoResponseDTO = descontoUseCase.buscarPorId(id)

    @PutMapping("/{id}")
    fun substituir(
        @PathVariable id: Long,
        @Valid
        @RequestBody desconto: DescontoRequestDTO,
    ): DescontoResponseDTO = descontoUseCase.substituir(id, desconto)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): DescontoResponseDTO = descontoUseCase.atualizar(id, campos)

    @DeleteMapping("/{id}")
    fun excluir(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        descontoUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}

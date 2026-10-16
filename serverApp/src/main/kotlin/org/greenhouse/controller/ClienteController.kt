package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.ClienteCreateRequestDTO
import org.greenhouse.dto.request.ClienteUpdateRequestDTO
import org.greenhouse.dto.response.ClienteResponseDTO
import org.greenhouse.service.ClienteService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cliente")
class ClienteController(
    private val clienteUseCase: ClienteService,
) {

    @PostMapping
    fun criar(@Valid @RequestBody cliente: ClienteCreateRequestDTO): ResponseEntity<ClienteResponseDTO> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(clienteUseCase.criar(cliente))

    @GetMapping
    fun listar(
        @RequestParam(required = false) nome: String?,
        @RequestParam(required = false) telefone: String?,
        @RequestParam(required = false) ativo: Boolean?,
    ): List<ClienteResponseDTO> = clienteUseCase.listar(nome, telefone, ativo)

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ClienteResponseDTO = clienteUseCase.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody cliente: ClienteUpdateRequestDTO,
    ): ClienteResponseDTO = clienteUseCase.atualizar(id, cliente)
}

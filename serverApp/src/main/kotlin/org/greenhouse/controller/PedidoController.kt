package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.AtualizarStatusRequestDTO
import org.greenhouse.dto.request.PedidoCreateRequestDTO
import org.greenhouse.dto.request.PedidoUpdateRequestDTO
import org.greenhouse.dto.response.PedidoResponseDTO
import org.greenhouse.service.PedidoService
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
@RequestMapping("/pedido")
class PedidoController(
    private val pedidoUseCase: PedidoService,
) {
    @PostMapping
    fun criar(
        @Valid @RequestBody pedido: PedidoCreateRequestDTO,
    ): ResponseEntity<PedidoResponseDTO> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(pedidoUseCase.criar(pedido))

    @GetMapping
    fun listar(
        @RequestParam(required = false) clienteId: Long?,
        @RequestParam(required = false) funcionarioId: Long?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) modalidade: String?,
    ): List<PedidoResponseDTO> = pedidoUseCase.listar(clienteId, funcionarioId, status, modalidade)

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
    ): PedidoResponseDTO = pedidoUseCase.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody pedido: PedidoUpdateRequestDTO,
    ): PedidoResponseDTO = pedidoUseCase.atualizar(id, pedido)

    @PatchMapping("/{id}/status")
    fun atualizarStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: AtualizarStatusRequestDTO,
    ): PedidoResponseDTO = pedidoUseCase.atualizarStatus(id, request.status)
}

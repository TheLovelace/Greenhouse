package org.greenhouse.controller

import org.greenhouse.entity.Pedido
import org.greenhouse.usecase.PedidoUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pedido")
class PedidoController(
    private val pedidoUseCase: PedidoUseCase,
) {
    @PostMapping
    fun criar(
        @RequestBody pedido: Pedido,
    ): ResponseEntity<Pedido> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(pedidoUseCase.criar(pedido))

    @GetMapping
    fun listar(
        @RequestParam(required = false) clienteId: Long?,
        @RequestParam(required = false) funcionarioId: Long?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) modalidade: String?,
    ): List<Pedido> = pedidoUseCase.listar(clienteId, funcionarioId, status, modalidade)

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
    ): Pedido = pedidoUseCase.buscarPorId(id)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): Pedido = pedidoUseCase.atualizar(id, campos)

    @PatchMapping("/{id}/status")
    fun atualizarStatus(
        @PathVariable id: Long,
        @RequestBody request: AtualizarStatusPedidoRequest,
    ): Pedido = pedidoUseCase.atualizarStatus(id, request.status)
}

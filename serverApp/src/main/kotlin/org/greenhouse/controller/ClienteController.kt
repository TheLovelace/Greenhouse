package org.greenhouse.controller

import org.greenhouse.dto.ClienteDTO as Cliente
import org.greenhouse.service.ClienteService
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
@RequestMapping("/cliente")
class ClienteController(
    private val clienteUseCase: ClienteService,
) {

    @PostMapping
    fun criar(@RequestBody cliente: Cliente): ResponseEntity<Cliente> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(clienteUseCase.criar(cliente))

    @GetMapping
    fun listar(
        @RequestParam(required = false) nome: String?,
        @RequestParam(required = false) telefone: String?,
        @RequestParam(required = false) ativo: Boolean?,
    ): List<Cliente> = clienteUseCase.listar(nome, telefone, ativo)

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Cliente = clienteUseCase.buscarPorId(id)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): Cliente = clienteUseCase.atualizar(id, campos)
}

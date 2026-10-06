package org.greenhouse.controller

import org.greenhouse.entity.Desconto
import org.greenhouse.usecase.DescontoUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AtualizarStatusDescontoRequest(
    val ativo: Boolean,
)

@RestController
@RequestMapping("/desconto")
class DescontoController(
    private val descontoUseCase: DescontoUseCase,
) {
    @PostMapping
    fun criar(
        @RequestBody desconto: Desconto,
    ): ResponseEntity<Desconto> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(descontoUseCase.criar(desconto))

    @GetMapping
    fun listar(): List<Desconto> = descontoUseCase.listar()

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
    ): Desconto = descontoUseCase.buscarPorId(id)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): Desconto = descontoUseCase.atualizar(id, campos)

    @PatchMapping("/{id}/status")
    fun atualizarStatus(
        @PathVariable id: Long,
        @RequestBody request: AtualizarStatusDescontoRequest,
    ): Desconto = descontoUseCase.atualizarStatus(id, request.ativo)
}

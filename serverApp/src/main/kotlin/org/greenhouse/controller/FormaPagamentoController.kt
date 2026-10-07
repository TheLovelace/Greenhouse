package org.greenhouse.controller

import org.greenhouse.dto.FormaPagamentoDTO as FormaPagamento
import org.greenhouse.service.FormaPagamentoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/forma-pagamento")
class FormaPagamentoController(
    private val formaPagamentoUseCase: FormaPagamentoService,
) {
    @PostMapping
    fun criar(
        @RequestBody formaPagamento: FormaPagamento,
    ): ResponseEntity<FormaPagamento> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(formaPagamentoUseCase.criar(formaPagamento))

    @GetMapping
    fun listar(): List<FormaPagamento> = formaPagamentoUseCase.listar()

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
    ): FormaPagamento =
        formaPagamentoUseCase
            .buscarPorId(id)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): FormaPagamento =
        formaPagamentoUseCase
            .atualizar(id, campos)

    @DeleteMapping("/{id}")
    fun excluir(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        formaPagamentoUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}

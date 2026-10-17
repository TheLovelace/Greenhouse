package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.FormaPagamentoRequestDTO
import org.greenhouse.dto.response.FormaPagamentoResponseDTO
import org.greenhouse.service.FormaPagamentoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/forma-pagamento")
class FormaPagamentoController(
    private val formaPagamentoUseCase: FormaPagamentoService,
) {
    @PostMapping
    fun criar(
        @Valid
        @RequestBody formaPagamento: FormaPagamentoRequestDTO,
    ): ResponseEntity<FormaPagamentoResponseDTO> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(formaPagamentoUseCase.criar(formaPagamento))

    @GetMapping
    fun listar(
        @RequestParam(required = false) nome: String?,
    ): List<FormaPagamentoResponseDTO> = formaPagamentoUseCase.listar(nome)

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
    ): FormaPagamentoResponseDTO =
        formaPagamentoUseCase
            .buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid
        @RequestBody formaPagamento: FormaPagamentoRequestDTO,
    ): FormaPagamentoResponseDTO =
        formaPagamentoUseCase
            .atualizar(id, formaPagamento)

    @DeleteMapping("/{id}")
    fun excluir(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        formaPagamentoUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}

package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.AtualizarStatusCompraRequestDTO
import org.greenhouse.dto.request.CompraFornecedorCreateRequestDTO
import org.greenhouse.dto.request.CompraFornecedorUpdateRequestDTO
import org.greenhouse.dto.response.CompraFornecedorResponseDTO
import org.greenhouse.service.CompraFornecedorService
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
@RequestMapping("/compra-fornecedor")
class CompraFornecedorController(
    private val compraFornecedorUseCase: CompraFornecedorService,
) {
    @PostMapping
    fun criar(
        @Valid
        @RequestBody compra: CompraFornecedorCreateRequestDTO,
    ): ResponseEntity<CompraFornecedorResponseDTO> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(compraFornecedorUseCase.criar(compra))

    @GetMapping
    fun listar(
        @RequestParam(required = false) observacao: String?,
    ): List<CompraFornecedorResponseDTO> = compraFornecedorUseCase.listar(observacao)

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
    ): CompraFornecedorResponseDTO = compraFornecedorUseCase.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid
        @RequestBody compra: CompraFornecedorUpdateRequestDTO,
    ): CompraFornecedorResponseDTO = compraFornecedorUseCase.atualizar(id, compra)

    @PatchMapping("/{id}/status")
    fun atualizarStatus(
        @PathVariable id: Long,
        @Valid
        @RequestBody request: AtualizarStatusCompraRequestDTO,
    ): CompraFornecedorResponseDTO = compraFornecedorUseCase.atualizarStatus(id, request.status)

    @DeleteMapping("/{id}")
    fun excluir(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        compraFornecedorUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}

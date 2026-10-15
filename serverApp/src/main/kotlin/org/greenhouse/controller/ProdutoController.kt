package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.ProdutoCreateRequestDTO
import org.greenhouse.dto.request.ProdutoUpdateRequestDTO
import org.greenhouse.dto.response.ProdutoResponseDTO
import org.greenhouse.service.ProdutoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/produto")
class ProdutoController(
    private val produtoUseCase: ProdutoService,
) {

    @PostMapping
    fun criar(@Valid @RequestBody produto: ProdutoCreateRequestDTO): ResponseEntity<ProdutoResponseDTO> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(produtoUseCase.criar(produto))

    @GetMapping
    fun listar(@RequestParam(required = false) nome: String?): List<ProdutoResponseDTO> =
        produtoUseCase.listar(nome)

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ProdutoResponseDTO = produtoUseCase.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody produto: ProdutoUpdateRequestDTO,
    ): ProdutoResponseDTO = produtoUseCase.atualizar(id, produto)

    @DeleteMapping("/{id}")
    fun excluir(@PathVariable id: Long): ResponseEntity<Unit> {
        produtoUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}

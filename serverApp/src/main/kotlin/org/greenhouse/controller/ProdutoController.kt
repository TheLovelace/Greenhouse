package org.greenhouse.controller

import org.greenhouse.dto.ProdutoDTO as Produto
import org.greenhouse.service.ProdutoService
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
@RequestMapping("/produto")
class ProdutoController(
    private val produtoUseCase: ProdutoService,
) {

    @PostMapping
    fun criar(@RequestBody produto: Produto): ResponseEntity<Produto> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(produtoUseCase.criar(produto))

    @GetMapping
    fun listar(): List<Produto> = produtoUseCase.listar()

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Produto = produtoUseCase.buscarPorId(id)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): Produto = produtoUseCase.atualizar(id, campos)

    @DeleteMapping("/{id}")
    fun excluir(@PathVariable id: Long): ResponseEntity<Unit> {
        produtoUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}

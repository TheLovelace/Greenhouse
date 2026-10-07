package org.greenhouse.controller

import org.greenhouse.dto.CategoriaDTO as Categoria
import org.greenhouse.service.CategoriaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/categoria")
class CategoriaController(
    private val categoriaUseCase: CategoriaService,
) {

    @PostMapping
    fun criar(@RequestBody categoria: Categoria): ResponseEntity<Categoria> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(categoriaUseCase.criar(categoria))

    @GetMapping
    fun listar(): List<Categoria> = categoriaUseCase.listar()

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Categoria = categoriaUseCase.buscarPorId(id)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): Categoria = categoriaUseCase.atualizar(id, campos)

    @DeleteMapping("/{id}")
    fun excluir(@PathVariable id: Long): ResponseEntity<Unit> {
        categoriaUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
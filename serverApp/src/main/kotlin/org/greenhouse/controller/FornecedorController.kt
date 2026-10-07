package org.greenhouse.controller

import org.greenhouse.entity.Fornecedor
import org.greenhouse.usecase.FornecedorUseCase
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
@RequestMapping("/fornecedor")
class FornecedorController(
    private val fornecedorUseCase: FornecedorUseCase,
) {

    @PostMapping
    fun criar(@RequestBody fornecedor: Fornecedor): ResponseEntity<Fornecedor> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(fornecedorUseCase.criar(fornecedor))

    @GetMapping
    fun listar(): List<Fornecedor> = fornecedorUseCase.listar()

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
    ): Fornecedor = fornecedorUseCase.buscarPorId(id)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): Fornecedor = fornecedorUseCase.atualizar(id, campos)

    @DeleteMapping("/{id}")
    fun excluir(@PathVariable id: Long): ResponseEntity<Unit> {
        fornecedorUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
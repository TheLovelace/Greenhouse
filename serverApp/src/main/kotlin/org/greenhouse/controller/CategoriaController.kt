package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.CategoriaCreateRequestDTO
import org.greenhouse.dto.request.CategoriaUpdateRequestDTO
import org.greenhouse.dto.response.CategoriaResponseDTO
import org.greenhouse.service.CategoriaService
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
@RequestMapping("/categoria")
class CategoriaController(
    private val categoriaUseCase: CategoriaService,
) {

    @PostMapping
    fun criar(@Valid @RequestBody categoria: CategoriaCreateRequestDTO): ResponseEntity<CategoriaResponseDTO> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(categoriaUseCase.criar(categoria))

    @GetMapping
    fun listar(@RequestParam(required = false) nome: String?): List<CategoriaResponseDTO> =
        categoriaUseCase.listar(nome)

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): CategoriaResponseDTO = categoriaUseCase.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody categoria: CategoriaUpdateRequestDTO,
    ): CategoriaResponseDTO = categoriaUseCase.atualizar(id, categoria)

    @DeleteMapping("/{id}")
    fun excluir(@PathVariable id: Long): ResponseEntity<Unit> {
        categoriaUseCase.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
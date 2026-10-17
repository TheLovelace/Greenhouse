package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.EstoqueCreateRequestDTO
import org.greenhouse.dto.request.EstoqueUpdateRequestDTO
import org.greenhouse.dto.response.EstoqueResponseDTO
import org.greenhouse.service.EstoqueService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/estoque")
class EstoqueController(
    private val estoqueService: EstoqueService,
) {
    @PostMapping
    fun criarRegistroEstoque(
        @Valid
        @RequestBody estoque: EstoqueCreateRequestDTO,
    ): ResponseEntity<EstoqueResponseDTO> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(estoqueService.criarRegistroEstoque(estoque))

    @GetMapping
    fun listarRegistrosEstoque(): List<EstoqueResponseDTO> = estoqueService.listarRegistrosEstoque()

    @GetMapping("/{produtoId}")
    fun buscarRegistroEstoquePorProdutoId(
        @PathVariable produtoId: Long,
    ): EstoqueResponseDTO = estoqueService.buscarRegistroEstoquePorProdutoId(produtoId)

    @PutMapping("/{produtoId}")
    fun substituirRegistroEstoque(
        @PathVariable produtoId: Long,
        @Valid
        @RequestBody estoque: EstoqueUpdateRequestDTO,
    ): EstoqueResponseDTO = estoqueService.substituirRegistroEstoque(produtoId, estoque)

    @PatchMapping("/{produtoId}")
    fun atualizarRegistroEstoque(
        @PathVariable produtoId: Long,
        @RequestBody campos: Map<String, Any?>,
    ): EstoqueResponseDTO = estoqueService.atualizarRegistroEstoque(produtoId, campos)

    @DeleteMapping("/{produtoId}")
    fun excluirRegistroEstoque(
        @PathVariable produtoId: Long,
    ): ResponseEntity<Unit> {
        estoqueService.excluirRegistroEstoque(produtoId)
        return ResponseEntity.noContent().build()
    }
}

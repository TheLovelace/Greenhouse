package org.greenhouse.controller

import org.greenhouse.dto.EstoqueCreateDTO
import org.greenhouse.dto.EstoqueDTO as Estoque
import org.greenhouse.service.EstoqueService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/estoque")
class EstoqueController(
    private val estoqueService: EstoqueService,
) {
    @PostMapping
    fun criarPosicao(
        @RequestBody estoque: EstoqueCreateDTO,
    ): ResponseEntity<Estoque> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(estoqueService.criarPosicao(estoque))

    @GetMapping("/{produtoId}")
    fun buscarPosicaoPorProdutoId(
        @PathVariable produtoId: Long,
    ): Estoque = estoqueService.buscarPosicaoPorProdutoId(produtoId)

    @PatchMapping("/{produtoId}")
    fun atualizarPosicao(
        @PathVariable produtoId: Long,
        @RequestBody campos: Map<String, Any?>,
    ): Estoque = estoqueService.atualizarPosicao(produtoId, campos)
}

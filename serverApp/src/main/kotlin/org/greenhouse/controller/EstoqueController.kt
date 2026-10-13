package org.greenhouse.controller

import org.greenhouse.dto.EstoqueDTO as Estoque
import org.greenhouse.service.EstoqueService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/estoque")
class EstoqueController(
    private val estoqueService: EstoqueService,
) {
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

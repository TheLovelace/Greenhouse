package org.greenhouse.controller

import org.greenhouse.entity.EstoquePosicao
import org.greenhouse.usecase.EstoqueUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/estoque")
class EstoqueController(
    private val estoqueUseCase: EstoqueUseCase,
) {
    @GetMapping("/{produtoId}")
    fun buscarPosicaoPorProdutoId(
        @PathVariable produtoId: Long,
    ): EstoquePosicao = estoqueUseCase.buscarPosicaoPorProdutoId(produtoId)
}

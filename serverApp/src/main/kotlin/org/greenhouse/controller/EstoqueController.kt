package org.greenhouse.controller

import org.greenhouse.dto.EstoqueDTO as Estoque
import org.greenhouse.dto.MovimentacaoEstoqueDTO as MovimentacaoEstoque
import org.greenhouse.service.EstoqueService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/estoque")
class EstoqueController(
    private val estoqueUseCase: EstoqueService,
) {
    @GetMapping("/{produtoId}")
    fun buscarPosicaoPorProdutoId(
        @PathVariable produtoId: Long,
    ): Estoque = estoqueUseCase.buscarPosicaoPorProdutoId(produtoId)

    @GetMapping("/movimentacoes")
    fun listarMovimentacoes(): List<MovimentacaoEstoque> = estoqueUseCase.listarMovimentacoes()

    @PostMapping("/movimentacoes")
    fun registrarMovimentacao(
        @RequestBody request: RegistrarMovimentacaoEstoqueRequest,
    ): ResponseEntity<MovimentacaoEstoque> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                estoqueUseCase.registrarMovimentacao(
                    produtoId = request.produtoId,
                    tipo = request.tipo,
                    quantidade = request.quantidade,
                    observacao = request.observacao,
                ),
            )
}

package org.greenhouse.controller

import org.greenhouse.entity.Funcionario
import org.greenhouse.usecase.FuncionarioUseCase
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
@RequestMapping("/funcionario")
class FuncionarioController(
    private val funcionarioUseCase: FuncionarioUseCase,
) {

    @PostMapping
    fun criar(
        @RequestBody funcionario: Funcionario,
    ): ResponseEntity<Funcionario> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(funcionarioUseCase.criar(funcionario))
    }

    @GetMapping
    fun listar(): List<Funcionario> = funcionarioUseCase.listar()

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Funcionario = funcionarioUseCase.buscarPorId(id)

    @PatchMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody campos: Map<String, Any?>,
    ): Funcionario {
        return funcionarioUseCase.atualizar(id, campos)
    }

    @DeleteMapping("/{id}")
    fun desativar(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        funcionarioUseCase.desativar(id)
        return ResponseEntity.noContent().build()
    }
}
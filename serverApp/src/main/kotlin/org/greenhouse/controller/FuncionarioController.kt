package org.greenhouse.controller

import org.greenhouse.dto.FuncionarioDTO as Funcionario
import org.greenhouse.service.FuncionarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/funcionario")
class FuncionarioController(
    private val funcionarioUseCase: FuncionarioService,
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

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody funcionario: Funcionario,
    ): Funcionario {
        return funcionarioUseCase.atualizar(id, funcionario)
    }

    @DeleteMapping("/{id}")
    fun desativar(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        funcionarioUseCase.desativar(id)
        return ResponseEntity.noContent().build()
    }
}
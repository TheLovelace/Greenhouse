package org.greenhouse.controller

import jakarta.validation.Valid
import org.greenhouse.dto.request.FuncionarioRequestDTO
import org.greenhouse.dto.response.FuncionarioResponseDTO
import org.greenhouse.service.FuncionarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/funcionario")
class FuncionarioController(
    private val funcionarioUseCase: FuncionarioService,
) {

    @PostMapping
    fun criar(
        @Valid
        @RequestBody funcionario: FuncionarioRequestDTO,
    ): ResponseEntity<FuncionarioResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(funcionarioUseCase.criar(funcionario))
    }

    @GetMapping
    fun listar(
        @RequestParam(required = false) nome: String?,
    ): List<FuncionarioResponseDTO> = funcionarioUseCase.listar(nome)

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): FuncionarioResponseDTO = funcionarioUseCase.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid
        @RequestBody funcionario: FuncionarioRequestDTO,
    ): FuncionarioResponseDTO {
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
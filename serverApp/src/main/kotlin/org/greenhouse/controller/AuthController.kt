package org.greenhouse.controller

// Trocamos os imports da pasta DTO para a pasta usecase, onde as data classes estão agora
import org.greenhouse.usecase.LoginRequest
import org.greenhouse.usecase.PrimeiroAcessoRequest
import org.greenhouse.usecase.TokenResponse
import org.greenhouse.usecase.AuthUseCase

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authUseCase: AuthUseCase,
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val token = authUseCase.login(request)
        return ResponseEntity.ok(token)
    }

    @PostMapping("/primeiro-acesso")
    fun primeiroAcesso(@RequestBody request: PrimeiroAcessoRequest): ResponseEntity<Unit> {
        authUseCase.primeiroAcesso(request)
        return ResponseEntity.noContent().build()
    }
}
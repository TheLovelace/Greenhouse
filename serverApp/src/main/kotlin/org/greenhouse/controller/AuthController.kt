package org.greenhouse.controller

// Trocamos os imports da pasta DTO para a pasta usecase, onde as data classes estão agora
import org.greenhouse.service.LoginRequest
import org.greenhouse.service.PrimeiroAcessoRequest
import org.greenhouse.service.TokenResponse
import org.greenhouse.service.AuthService

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authUseCase: AuthService,
) {

    @PostMapping("/signin")
    fun signin(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val token = authUseCase.login(request)
        return ResponseEntity.ok(token)
    }

    @PostMapping("/signup")
    fun signup(@RequestBody request: PrimeiroAcessoRequest): ResponseEntity<Unit> {
        authUseCase.primeiroAcesso(request)
        return ResponseEntity.noContent().build()
    }
}
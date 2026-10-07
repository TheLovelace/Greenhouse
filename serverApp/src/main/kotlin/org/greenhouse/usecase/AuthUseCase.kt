package org.greenhouse.usecase

import org.greenhouse.adapter.AuthenticationException
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.security.JwtService
import org.springframework.stereotype.Service

@Service
class AuthUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val jwtService: JwtService
) {

    fun login(request: LoginRequest): TokenResponse {
        val usuario = usuarioRepository.buscarPorLogin(request.login)
            ?: throw AuthenticationException("Credenciais inválidas")

        if (!usuario.ativo) {
            throw AuthenticationException("Usuário inativo")
        }

        if (usuario.senhaHash != request.senhaHash) {
            throw AuthenticationException("Credenciais inválidas")
        }

        val tokenReal = jwtService.gerarToken(usuario.login, usuario.perfil)

        return TokenResponse(
            token = tokenReal,
            perfil = usuario.perfil
        )
    }

    fun primeiroAcesso(request: PrimeiroAcessoRequest) {
        if (request.novaSenha.length < 6) {
            throw BadRequestException("A nova senha deve ter no mínimo 6 caracteres")
        }

        val usuario = usuarioRepository.buscarPorCpfEEmail(request.cpf, request.email)
            ?: throw NotFoundException("Dados de validação incorretos ou usuário não encontrado")

        if (!usuario.ativo) {
            throw BadRequestException("Este usuário está inativo")
        }

        val sucesso = usuarioRepository.atualizarSenha(usuario.id!!, request.novaSenha)
        if (!sucesso) {
            throw BadRequestException("Não foi possível redefinir a senha")
        }
    }
}

// Declarando as estruturas de dados aqui embaixo para burlar a necessidade de arquivos DTO separados
data class LoginRequest(val login: String, val senhaHash: String)
data class PrimeiroAcessoRequest(val cpf: String, val email: String, val novaSenha: String)
data class TokenResponse(val token: String, val perfil: String)
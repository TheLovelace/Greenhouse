package org.greenhouse.usecase

import org.greenhouse.adapter.AuthenticationException
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.ServerException
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.security.JwtService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
class AuthUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val jwtService: JwtService
) {

    private val passwordEncoder = BCryptPasswordEncoder()
    private val bcryptHashRegex = Regex("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$")

    fun login(request: LoginRequest): TokenResponse {
        val usuario = usuarioRepository.buscarPorLogin(request.login)
            ?: throw AuthenticationException("Credenciais inválidas")

        if (!usuario.ativo) {
            throw AuthenticationException("Usuário inativo")
        }

        if (!usuario.primeiroAcessoFeito) {
            throw AuthenticationException("O primeiro acesso ainda não foi concluído")
        }

        val isBcryptHash = usuario.senhaHash.matches(bcryptHashRegex)
        val senhaCorreta =
            if (isBcryptHash) {
                request.senhaHash.toByteArray(StandardCharsets.UTF_8).size <= BCRYPT_MAX_PASSWORD_BYTES &&
                    passwordEncoder.matches(request.senhaHash, usuario.senhaHash)
            } else {
                usuario.senhaHash == request.senhaHash
            }

        if (!senhaCorreta) {
            throw AuthenticationException("Credenciais inválidas")
        }

        if (!isBcryptHash && request.senhaHash.toByteArray(StandardCharsets.UTF_8).size <= BCRYPT_MAX_PASSWORD_BYTES) {
            val atualizado = usuarioRepository.atualizarSenha(usuario.id!!, passwordEncoder.encode(request.senhaHash))
            if (!atualizado) {
                throw ServerException("Não foi possível atualizar a credencial")
            }
        }

        val tokenReal = jwtService.gerarToken(usuario.login, usuario.perfil)

        return TokenResponse(
            token = tokenReal,
            perfil = usuario.perfil
        )
    }

    fun primeiroAcesso(request: PrimeiroAcessoRequest) {
        if (request.novaSenha.length < MIN_PASSWORD_LENGTH) {
            throw BadRequestException("A nova senha deve ter no mínimo 6 caracteres")
        }
        if (request.novaSenha.toByteArray(StandardCharsets.UTF_8).size > BCRYPT_MAX_PASSWORD_BYTES) {
            throw BadRequestException("A nova senha deve ter no máximo 72 bytes em UTF-8")
        }

        val usuario = usuarioRepository.buscarPorCpfEEmail(request.cpf, request.email)
            ?: throw NotFoundException("Dados de validação incorretos ou usuário não encontrado")

        if (!usuario.ativo) {
            throw BadRequestException("Este usuário está inativo")
        }
        if (usuario.primeiroAcessoFeito) {
            throw BadRequestException("O primeiro acesso já foi concluído")
        }

        val sucesso =
            usuarioRepository.concluirPrimeiroAcesso(
                usuario.id!!,
                passwordEncoder.encode(request.novaSenha),
            )
        if (!sucesso) {
            throw BadRequestException("Não foi possível redefinir a senha")
        }
    }

    private companion object {
        const val BCRYPT_MAX_PASSWORD_BYTES = 72
        const val MIN_PASSWORD_LENGTH = 6
    }
}

// Declarando as estruturas de dados aqui embaixo para burlar a necessidade de arquivos DTO separados
data class LoginRequest(val login: String, val senhaHash: String)
data class PrimeiroAcessoRequest(val cpf: String, val email: String, val novaSenha: String)
data class TokenResponse(val token: String, val perfil: String)
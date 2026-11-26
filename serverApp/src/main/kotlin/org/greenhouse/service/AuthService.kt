package org.greenhouse.service

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Locale
import org.greenhouse.adapter.AuthenticationException
import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.ServerException
import org.greenhouse.mapper.UsuarioMapper
import org.greenhouse.model.RefreshToken
import org.greenhouse.repository.RefreshTokenRepository
import org.greenhouse.repository.UsuarioRepository
import org.greenhouse.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.core.AuthenticationException as SpringAuthenticationException

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val usuarioMapper: UsuarioMapper,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val usuario =
            usuarioRepository
                .buscarPorLogin(request.login)
                ?.let(usuarioMapper::toDto)
                ?: throw AuthenticationException("Credenciais inválidas")

        if (!usuario.ativo) {
            throw AuthenticationException("Usuário inativo")
        }

        if (!usuario.primeiroAcessoFeito) {
            throw AuthenticationException("O primeiro acesso ainda não foi concluído")
        }

        autenticarComSpringSecurity(request)

        val tokenReal = jwtService.gerarToken(usuario.login, usuario.perfil)
        val refreshToken =
            if (request.lembrarDispositivo) {
                criarRefreshToken(usuario.id!!)
            } else {
                null
            }

        return TokenResponse(
            token = tokenReal,
            perfil = usuario.perfil,
            refreshToken = refreshToken,
            expiresInSeconds = jwtService.accessTokenLifetimeSeconds,
        )
    }

    @Transactional(noRollbackFor = [AuthenticationException::class])
    fun refresh(request: RefreshTokenRequest): TokenResponse {
        val now = Instant.now()
        val persistedToken =
            refreshTokenRepository.buscarPorHashParaAtualizacao(hashToken(request.refreshToken))
                ?: throw AuthenticationException("Sessão expirada. Faça login novamente.")

        if (persistedToken.revogadoEm != null || !persistedToken.expiraEm.isAfter(now)) {
            throw AuthenticationException("Sessão expirada. Faça login novamente.")
        }

        val usuario =
            usuarioRepository.findById(persistedToken.usuarioId).orElse(null)
                ?: throw AuthenticationException("Sessão inválida. Faça login novamente.")
        val usuarioDto = usuarioMapper.toDto(usuario)
        if (!usuarioDto.ativo || !usuarioDto.primeiroAcessoFeito) {
            persistedToken.revogadoEm = now
            throw AuthenticationException("Usuário inativo ou acesso pendente.")
        }

        persistedToken.revogadoEm = now
        refreshTokenRepository.save(persistedToken)
        val nextRefreshToken = gerarTokenAleatorio()
        refreshTokenRepository.save(
            RefreshToken(
                usuarioId = usuario.id!!,
                tokenHash = hashToken(nextRefreshToken),
                criadoEm = now,
                expiraEm = persistedToken.expiraEm,
            ),
        )

        return TokenResponse(
            token = jwtService.gerarToken(usuario.login, usuario.perfil),
            perfil = usuario.perfil,
            refreshToken = nextRefreshToken,
            expiresInSeconds = jwtService.accessTokenLifetimeSeconds,
        )
    }

    private fun autenticarComSpringSecurity(request: LoginRequest) {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.login, request.senhaHash),
            )
        } catch (exception: InternalAuthenticationServiceException) {
            throw ServerException("Não foi possível autenticar o usuário")
        } catch (exception: SpringAuthenticationException) {
            throw AuthenticationException("Credenciais inválidas")
        }
    }

    fun validarPrimeiroAcesso(request: ValidarPrimeiroAcessoRequest) {
        val usuario =
            usuarioRepository.buscarPorLogin(normalizarLogin(request.login))?.let(usuarioMapper::toDto)
                ?: throw NotFoundException(FIRST_ACCESS_VALIDATION_ERROR)

        if (!usuario.ativo || usuario.primeiroAcessoFeito) {
            throw NotFoundException(FIRST_ACCESS_VALIDATION_ERROR)
        }
    }

    fun primeiroAcesso(request: PrimeiroAcessoRequest) {
        if (request.novaSenha.length < MIN_PASSWORD_LENGTH) {
            throw BadRequestException("A nova senha deve ter no mínimo 6 caracteres")
        }
        if (request.novaSenha.toByteArray(StandardCharsets.UTF_8).size > BCRYPT_MAX_PASSWORD_BYTES) {
            throw BadRequestException("A nova senha deve ter no máximo 72 bytes em UTF-8")
        }

        val usuario =
            usuarioRepository.buscarPorCpfEEmail(request.cpf, request.email)?.let(usuarioMapper::toDto)
                ?: throw NotFoundException(FIRST_ACCESS_VALIDATION_ERROR)

        if (request.login != null && usuario.login != normalizarLogin(request.login)) {
            throw NotFoundException(FIRST_ACCESS_VALIDATION_ERROR)
        }

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

    private fun criarRefreshToken(usuarioId: Long): String {
        val rawToken = gerarTokenAleatorio()
        val agora = Instant.now()
        refreshTokenRepository.save(
            RefreshToken(
                usuarioId = usuarioId,
                tokenHash = hashToken(rawToken),
                criadoEm = agora,
                expiraEm = agora.plus(REFRESH_TOKEN_LIFETIME_DAYS, ChronoUnit.DAYS),
            ),
        )
        return rawToken
    }

    private fun gerarTokenAleatorio(): String {
        val bytes = ByteArray(REFRESH_TOKEN_BYTES)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(token: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(token.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }

    private fun normalizarLogin(login: String): String = login.trim().substringBefore('@').lowercase(Locale.ROOT)

    private companion object {
        val secureRandom = SecureRandom()
        const val FIRST_ACCESS_VALIDATION_ERROR = "Não foi possível validar os dados do primeiro acesso"
        const val REFRESH_TOKEN_BYTES = 32
        const val REFRESH_TOKEN_LIFETIME_DAYS = 30L
        const val BCRYPT_MAX_PASSWORD_BYTES = 72
        const val MIN_PASSWORD_LENGTH = 6
    }
}

// Declarando as estruturas de dados aqui embaixo para burlar a necessidade de arquivos DTO separados
data class LoginRequest(
    val login: String,
    val senhaHash: String,
    val lembrarDispositivo: Boolean = false,
)

data class RefreshTokenRequest(
    val refreshToken: String,
)

data class ValidarPrimeiroAcessoRequest(
    val login: String,
)

data class PrimeiroAcessoRequest(
    val cpf: String,
    val email: String,
    val novaSenha: String,
    val login: String? = null,
)

data class TokenResponse(
    val token: String,
    val perfil: String,
    val refreshToken: String? = null,
    val expiresInSeconds: Long = 86_400,
)
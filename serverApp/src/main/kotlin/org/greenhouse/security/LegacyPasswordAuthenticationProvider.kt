package org.greenhouse.security

import org.greenhouse.repository.UsuarioRepository
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class LegacyPasswordAuthenticationProvider(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication? {
        val usuario = usuarioRepository.buscarPorLogin(authentication.name) ?: return null
        if (usuario.senhaHash.matches(BCRYPT_HASH_REGEX)) return null

        val senhaInformada = authentication.credentials as? String ?: return null
        if (usuario.senhaHash != senhaInformada) {
            throw BadCredentialsException("Credenciais inválidas")
        }
        if (!usuario.ativo || !usuario.primeiroAcessoFeito) {
            throw BadCredentialsException("Credenciais inválidas")
        }

        if (senhaInformada.toByteArray(StandardCharsets.UTF_8).size <= BCRYPT_MAX_PASSWORD_BYTES) {
            val id = usuario.id ?: throw InternalAuthenticationServiceException("Não foi possível atualizar a credencial")
            val atualizado = usuarioRepository.atualizarSenha(id, passwordEncoder.encode(senhaInformada))
            if (!atualizado) {
                throw InternalAuthenticationServiceException("Não foi possível atualizar a credencial")
            }
        }

        return UsernamePasswordAuthenticationToken.authenticated(
            usuario.login,
            null,
            listOf(SimpleGrantedAuthority("ROLE_${usuario.perfil}")),
        )
    }

    override fun supports(authentication: Class<*>): Boolean =
        UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)

    private companion object {
        const val BCRYPT_MAX_PASSWORD_BYTES = 72
        val BCRYPT_HASH_REGEX = Regex("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$")
    }
}

package org.greenhouse.security

import org.greenhouse.repository.UsuarioRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UsuarioUserDetailsService(
    private val usuarioRepository: UsuarioRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val usuario = usuarioRepository.buscarPorLogin(username)
            ?: throw UsernameNotFoundException("Credenciais inválidas")

        return User.builder()
            .username(usuario.login)
            .password(usuario.senhaHash)
            .authorities(SimpleGrantedAuthority("ROLE_${usuario.perfil}"))
            .disabled(!usuario.ativo || !usuario.primeiroAcessoFeito)
            .build()
    }
}

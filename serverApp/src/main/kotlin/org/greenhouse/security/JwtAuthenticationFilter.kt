package org.greenhouse.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // Se não houver cabeçalho ou não começar com "Bearer ", ignora o filtro e segue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        if (jwtService.validarToken(token)) {
            val login = jwtService.extrairLogin(token)
            val perfil = jwtService.extrairPerfil(token)

            if (login != null && perfil != null && SecurityContextHolder.getContext().authentication == null) {

                val authorities = listOf(SimpleGrantedAuthority("ROLE_$perfil"))

                val authToken = UsernamePasswordAuthenticationToken(
                    login,
                    null, // Não guardamos a senha no token
                    authorities
                )

                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }
}
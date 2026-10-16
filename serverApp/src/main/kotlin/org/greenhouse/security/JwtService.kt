package org.greenhouse.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.expiration:86400000}")
    private val expirationTime: Long
) {
    private val key: SecretKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    fun gerarToken(login: String, perfil: String): String {
        val now = Date()
        val validity = Date(now.time + expirationTime)

        return Jwts.builder()
            .subject(login)
            .claim("perfil", perfil)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun validarToken(token: String): Boolean {
        return try {
            val claims = extrairClaims(token)
            claims != null && claims.expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun extrairLogin(token: String): String? {
        return extrairClaims(token)?.subject
    }

    fun extrairPerfil(token: String): String? {
        return extrairClaims(token)?.get("perfil", String::class.java)
    }

    private fun extrairClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}
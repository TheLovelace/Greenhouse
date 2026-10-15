package org.greenhouse.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }

            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            .authorizeHttpRequests { auth ->

                auth.requestMatchers("/api/auth/signin", "/api/auth/signup").permitAll()
                auth.requestMatchers("/api/status").permitAll()

                auth.requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/v3/api-docs/**",
                    "/openapi/**",
                    "/openapi.yaml",
                    "/api/openapi/**",
                    "/api/openapi.yaml"
                ).permitAll()

                auth.requestMatchers("/api/funcionario", "/api/funcionario/**").hasRole("ADMIN")
                auth.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
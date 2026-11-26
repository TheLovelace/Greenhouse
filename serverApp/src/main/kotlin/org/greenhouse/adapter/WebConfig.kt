package org.greenhouse.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class WebConfig(
    @Value("\${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private val allowedOrigins: List<String>,
) : WebMvcConfigurer {
    private val corsAllowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    private val corsAllowedHeaders = listOf("Authorization", "Content-Type", "Accept")

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.addPathPrefix("/api") { controllerType ->
            controllerType.isAnnotationPresent(org.springframework.web.bind.annotation.RestController::class.java)
        }
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/api/**")
            .allowedOriginPatterns(*allowedOrigins.toTypedArray())
            .allowedMethods(*corsAllowedMethods.toTypedArray())
            .allowedHeaders(*corsAllowedHeaders.toTypedArray())
            .maxAge(3600)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration =
            CorsConfiguration().apply {
                setAllowedOriginPatterns(this@WebConfig.allowedOrigins)
                allowedMethods = this@WebConfig.corsAllowedMethods
                allowedHeaders = this@WebConfig.corsAllowedHeaders
                maxAge = 3600
            }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", corsConfiguration)
        }
    }
}
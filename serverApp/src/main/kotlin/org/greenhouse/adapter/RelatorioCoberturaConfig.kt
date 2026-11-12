package org.greenhouse.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class RelatorioCoberturaConfig(
    @Value("\${relatorio.jacoco.dir}") private val diretorio: String,
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/jacoco/**").addResourceLocations(comBarraFinal(diretorio))
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/jacoco", "/jacoco/index.html")
        registry.addRedirectViewController("/jacoco/", "/jacoco/index.html")
    }

    private fun comBarraFinal(local: String) = if (local.endsWith("/")) local else "$local/"
}

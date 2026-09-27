package org.greenhouse.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.addPathPrefix("/api") { controllerType ->
            controllerType.isAnnotationPresent(org.springframework.web.bind.annotation.RestController::class.java)
        }
    }
}
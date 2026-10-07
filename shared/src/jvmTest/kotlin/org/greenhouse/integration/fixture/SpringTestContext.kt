package org.greenhouse.integration.fixture

import org.greenhouse.GreenhouseApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext

object SpringTestContext {
    private val applicationContext: ConfigurableApplicationContext by lazy {
        requireNotNull(System.getenv("DATABASE_URL")) {
            "A variável DATABASE_URL é necessária para os testes de integração"
        }
        SpringApplicationBuilder(GreenhouseApplication::class.java)
            .web(WebApplicationType.NONE)
            .properties(
                "server.port=0",
                "spring.flyway.enabled=false",
            )
            .run()
    }

    fun <T : Any> bean(type: Class<T>): T = applicationContext.getBean(type)

    inline fun <reified T : Any> bean(): T = bean(T::class.java)
}

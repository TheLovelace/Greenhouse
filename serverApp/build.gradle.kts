plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.kotlinSpring)
    application
    alias(libs.plugins.jib)
}

application {
    mainClass.set("org.greenhouse.MainKt")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.6"))

    implementation(libs.spring.boot.starter.web)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.springdoc.openapi)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.postgresql)

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation(libs.androidx.constraintlayout.core)

    testImplementation(libs.spring.boot.starter.test)

    implementation("org.springframework.boot:spring-boot-starter-security")

    // Biblioteca JJWT para criar, assinar e extrair dados dos tokens JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
}

val registryOwner: String? = System.getenv("REGISTRY_OWNER")?.lowercase()
val imageName: String =
    if (registryOwner != null) "ghcr.io/$registryOwner/app" else "app"

val imageTag: String = System.getenv("IMAGE_TAG") ?: "local"

jib {
    from {
        image = "eclipse-temurin:21-jre"

        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
        }
    }

    to {
        image = imageName
        tags = setOf(imageTag)
    }

    container {
        ports = listOf("8080")

        jvmFlags = listOf(
            "-XX:MaxRAMPercentage=75",
            "-XX:+UseSerialGC"
        )

        System.getenv("GITHUB_REPOSITORY")?.let { repo ->
            labels.set(
                mapOf(
                    "org.opencontainers.image.source" to
                        "https://github.com/$repo"
                )
            )
        }
    }
}

val envFileTest = rootProject.file(".env.test.local")

tasks.register<Exec>("runTest") {
    group = "docker"
    description = "Builda a imagem no Docker local e sobe o container de teste"

    dependsOn("jibDockerBuild")

    doFirst {
        require(envFileTest.exists()) {
            "Arquivo ${envFileTest.name} nao encontrado na raiz."
        }
    }

    commandLine(
        "docker", "run", "-d", "--rm",
        "--name", "app-test",
        "-p", "8081:8080",
        "--env-file", envFileTest.absolutePath,
        "$imageName:$imageTag"
    )
}

tasks.register<Exec>("stopTest") {
    group = "docker"
    description = "Para o container de teste"

    isIgnoreExitValue = true

    commandLine("docker", "stop", "app-test")
}
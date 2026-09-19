
plugins {
    alias(libs.plugins.kotlinJvm)
    application
    alias(libs.plugins.jib)
}

application {
    mainClass.set("org.greenhouse.MainKt")
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
        "docker", "run", "--rm",
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
plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

tasks.register("devMobile") {
    group = "application"
    description = "Builda e instala o app Android no dispositivo/emulador conectado"
    dependsOn(":androidApp:installDebug")
}

tasks.register("devWeb") {
    group = "application"
    description = "Builda e roda o app web"
    dependsOn(":webApp:jsBrowserDevelopmentRun")
}

tasks.register("devDesktop") {
    group = "application"
    description = "Builda e roda o app desktop"
    dependsOn(":desktopApp:run")
}

tasks.register("testMobile") {
    group = "verification"
    description = "Roda os testes de Android e iOS"
    dependsOn(
            ":shared:testAndroidHostTest",
            ":shared:iosSimulatorArm64Test",
    )
}

tasks.register("testWeb") {
    group = "verification"
    description = "Roda os testes de JS e Wasm"
    dependsOn(
            ":shared:jsTest",
            ":shared:wasmJsTest",
    )
}

tasks.register("testDesktop") {
    group = "verification"
    description = "Roda os testes JVM (desktop)"
    dependsOn(":shared:jvmTest")
}


tasks.register("checkCommitMessage") {
    group = "verification"
    description = "Valida se a mensagem do último commit segue Conventional Commits"

    doLast {
        val commitMsg =
            providers
                .exec {
                    commandLine("git", "log", "-1", "--pretty=%B")
                }.standardOutput.asText
                .get()
                .trim()

        val pattern = Regex("^(feat|fix|docs|style|refactor|test|chore|perf|ci|build)(\\(.+\\))?: .{1,100}")

        if (!pattern.matches(commitMsg.lines().first())) {
            throw GradleException(
                "Mensagem de commit inválida: \"$commitMsg\"\n" +
                    "Formato esperado: tipo(escopo opcional): descrição\n" +
                    "Tipos válidos: feat, fix, docs, style, refactor, test, chore, perf, ci, build",
            )
        }
        println("Mensagem de commit válida: \"$commitMsg\"")
    }
}

tasks.register("lintAll") {
    group = "verification"
    description = "Formata e roda todos os linters do projeto"
    dependsOn(":androidApp:lintFix", "ktlintFormat")
}

tasks.register("lintCheck") {
    group = "verification"
    description = "Só verifica lint, sem alterar arquivos"
    dependsOn(":androidApp:lint", "ktlintCheck")
}
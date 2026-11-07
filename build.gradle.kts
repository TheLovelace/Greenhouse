import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.springBoot) apply false
    alias(libs.plugins.kotlinSpring) apply false
    alias(libs.plugins.jib) apply false

    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
    jacoco
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks.withType<AbstractTestTask>().configureEach {
        testLogging {
            events = emptySet()
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
            showStandardStreams = false
        }

        afterTest(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
                val reset = "\u001B[0m"
                val (color, label) =
                    when (result.resultType) {
                        TestResult.ResultType.SUCCESS -> "\u001B[32m" to "PASSED "
                        TestResult.ResultType.FAILURE -> "\u001B[31m" to "FAILED "
                        else -> "\u001B[33m" to "SKIPPED"
                    }
                val className = descriptor.className?.substringAfterLast('.') ?: "?"
                val elapsed = result.endTime - result.startTime
                println("  $color$label$reset $className > ${descriptor.displayName} ($elapsed ms)")

                if (result.resultType == TestResult.ResultType.FAILURE) {
                    result.exceptions.forEach { e ->
                        println("      $color${e.toString().lines().joinToString("\n      ")}$reset")
                        e.stackTrace
                            .filter { it.className.startsWith("org.greenhouse") }
                            .ifEmpty { e.stackTrace.toList() }
                            .take(5)
                            .forEach { println("        at $it") }
                    }
                }
            }),
        )

        afterSuite(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
                if (descriptor.parent == null && result.testCount > 0) {
                    val ok = result.resultType == TestResult.ResultType.SUCCESS
                    val color = if (ok) "\u001B[32m" else "\u001B[31m"
                    val reset = "\u001B[0m"
                    val seconds = (result.endTime - result.startTime) / 1000.0
                    val summary =
                        "${result.testCount} testes: ${result.successfulTestCount} passed, " +
                            "${result.failedTestCount} failed, ${result.skippedTestCount} skipped " +
                            "em ${"%.1f".format(seconds)}s"
                    val line = "=".repeat(summary.length + 4)
                    println("$color$line\n= $summary =\n$line$reset")
                }
            }),
        )
    }
}

val envFileTest = rootProject.file(".env.test.local")

fun loadEnvFile(file: File): Map<String, String> =
    file
        .readLines()
        .filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
        .associate {
            val (key, value) = it.split("=", limit = 2)
            key.trim() to value.trim()
        }

project(":shared") {
    tasks.withType<Test>().configureEach {
        if (envFileTest.exists()) {
            environment(loadEnvFile(envFileTest))
        }
    }
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

    val execOps = serviceOf<ExecOperations>()

    doLast {
        val output = java.io.ByteArrayOutputStream()
        execOps.exec {
            commandLine("git", "log", "-1", "--pretty=%B")
            standardOutput = output
        }
        val commitMsg = output.toString().trim()
        val firstLine = commitMsg.lines().first()

        if (firstLine.startsWith("Merge ")) {
            println("Commit de merge detectado, pulando validação: \"$firstLine\"")
            return@doLast
        }

        val pattern = Regex("^(feat|fix|docs|style|refactor|test|chore|perf|ci|build)(\\(.+\\))?: .{1,100}")

        if (!pattern.matches(firstLine)) {
            throw GradleException(
                "Mensagem de commit inválida: \"$firstLine\"\n" +
                    "Formato esperado: tipo(escopo opcional): descrição\n" +
                    "Tipos válidos: feat, fix, docs, style, refactor, test, chore, perf, ci, build",
            )
        }
        println("Mensagem de commit válida: \"$firstLine\"")
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

val serverAppProject = project(":serverApp")

tasks.register<JacocoReport>("coverage") {
    group = "verification"
    description = "Roda os testes do backend e gera o relatório de cobertura (JaCoCo) em build/reports/jacoco/coverage"

    dependsOn(":serverApp:test", ":shared:jvmTest")

    executionData.from(
        fileTree(serverAppProject.layout.buildDirectory.dir("jacoco")) { include("test.exec") },
        fileTree(project(":shared").layout.buildDirectory.dir("jacoco")) { include("jvmTest.exec") },
    )
    classDirectories.from(
        fileTree(serverAppProject.layout.buildDirectory.dir("classes/kotlin/main")) {
            exclude("org/greenhouse/MainKt*.class")
        },
    )
    sourceDirectories.from(serverAppProject.file("src/main/kotlin"))

    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }

    doLast {
        println("Relatório de cobertura: ${reports.html.outputLocation.get().asFile}/index.html")
    }
}

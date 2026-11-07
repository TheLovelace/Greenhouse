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

object CommitMessageChecker {
    val tipos = listOf("feat", "fix", "docs", "style", "refactor", "test", "tests", "chore", "perf", "ci", "build", "hotfix")

    private val alternativas = tipos.sortedByDescending { it.length }.joinToString("|")

    private val padraoComDoisPontos =
        Regex("""(?<![\p{L}\p{N}_-])($alternativas)(\([^)]*\))?!?:""", RegexOption.IGNORE_CASE)

    private val padraoEntreColchetes =
        Regex("""\[($alternativas)(\([^)]*\))?\]""", RegexOption.IGNORE_CASE)

    private val padraoAmend = Regex("""^--amend(\s|$)""", RegexOption.IGNORE_CASE)

    fun identificarTipo(mensagem: String): String? {
        val assunto =
            mensagem
                .trim()
                .lineSequence()
                .firstOrNull()
                .orEmpty()
        if (padraoAmend.containsMatchIn(assunto)) return "amend"
        val encontrado = padraoComDoisPontos.find(assunto) ?: padraoEntreColchetes.find(assunto)
        return encontrado?.groupValues?.get(1)?.lowercase()
    }

    fun isMerge(mensagem: String): Boolean = mensagem.trimStart().startsWith("Merge ")
}

tasks.register("checkCommitMessage") {
    group = "verification"
    description = "Verifica se a mensagem do commit (-PcommitRef, padrão HEAD, ou -PcommitMessage) contém um tipo de commit reconhecido"

    val execOps = serviceOf<ExecOperations>()
    val mensagemInformada = providers.gradleProperty("commitMessage")
    val commitRef = providers.gradleProperty("commitRef").orElse("HEAD")

    doLast {
        val commitMsg =
            mensagemInformada.orNull ?: run {
                val output = java.io.ByteArrayOutputStream()
                execOps.exec {
                    commandLine("git", "log", "-1", "--pretty=%B", commitRef.get())
                    standardOutput = output
                }
                output.toString()
            }
        val assunto =
            commitMsg
                .trim()
                .lineSequence()
                .firstOrNull()
                .orEmpty()

        if (CommitMessageChecker.isMerge(commitMsg)) {
            println("Commit de merge detectado, pulando validação: \"$assunto\"")
            return@doLast
        }

        val tipo =
            CommitMessageChecker.identificarTipo(commitMsg)
                ?: throw GradleException(
                    "Mensagem de commit sem tipo reconhecido: \"$assunto\"\n" +
                        "Inclua um tipo na mensagem, ex.: \"feat: descrição\", \"[fix] descrição\" ou \"✨ docs: descrição\"\n" +
                        "Tipos válidos: ${CommitMessageChecker.tipos.joinToString()}",
                )

        println("Mensagem de commit válida (tipo: $tipo): \"$assunto\"")
    }
}

tasks.register("checkCommitMessageTest") {
    group = "verification"
    description = "Testa a identificação de tipos da task checkCommitMessage"

    doLast {
        val casos =
            listOf(
                "feat: adiciona cadastro de categoria" to "feat",
                "docs: atualiza documentação" to "docs",
                "tests: adiciona testes para categoria" to "tests",
                "test: adiciona testes" to "test",
                "fix: corrige bug no produto" to "fix",
                "refactor: extrai validador" to "refactor",
                "chore: atualiza dependências" to "chore",
                "Implementa feat: cadastro de produto" to "feat",
                "[feat] adiciona cadastro" to "feat",
                "✨ feat: adiciona cadastro" to "feat",
                ":sparkles: feat: adiciona formulário" to "feat",
                ":fire: Hotfix: Consertando o problema do Authorize" to "hotfix",
                "feat(api): adiciona rota" to "feat",
                "feat!: remove endpoint antigo" to "feat",
                "Fix: corrige login" to "fix",
                "feat:sem espaço depois dos dois pontos" to "feat",
                "adiciona cadastro de categoria" to null,
                "corrige bug no produto" to null,
                "implementação inicial" to null,
                "asuhashuashau" to null,
                "--amend" to "amend",
                "--amend corrige typo" to "amend",
                "--amendment não é amend" to null,
                "prefix: não é um tipo" to null,
                "hotfix: corrige login em produção" to "hotfix",
                "superhotfix: não é um tipo" to null,
                "feature: não é um tipo" to null,
                "adiciona cadastro\n\nfeat: tipo só no corpo não conta" to null,
                "" to null,
            )

        val falhas =
            casos.mapNotNull { (mensagem, esperado) ->
                val obtido = CommitMessageChecker.identificarTipo(mensagem)
                val ok = obtido == esperado
                val status = if (ok) "PASSED" else "FAILED"
                println("  $status  \"${mensagem.replace("\n", "\\n")}\" -> ${obtido ?: "inválida"}")
                if (ok) null else "\"$mensagem\": esperado ${esperado ?: "inválida"}, obtido ${obtido ?: "inválida"}"
            }

        println("${casos.size} casos: ${casos.size - falhas.size} passed, ${falhas.size} failed")
        if (falhas.isNotEmpty()) {
            throw GradleException("Falhas na identificação de tipo:\n" + falhas.joinToString("\n"))
        }
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

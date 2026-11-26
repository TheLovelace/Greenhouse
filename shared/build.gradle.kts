import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    jacoco
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

val gerarApiConfig by tasks.registering {
    val apiUrl =
        providers
            .environmentVariable("API_URL")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .orElse("http://localhost:8081/api")
    val apiUrlAndroid =
        providers
            .environmentVariable("API_URL_ANDROID")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .orElse("http://10.0.2.2:8081/api")
    val saida = layout.buildDirectory.dir("generated/apiConfig/commonMain/kotlin")
    inputs.property("apiUrl", apiUrl)
    inputs.property("apiUrlAndroid", apiUrlAndroid)
    outputs.dir(saida)
    doLast {
        val arquivo = saida.get().file("org/greenhouse/app/repository/ApiConfig.kt").asFile
        arquivo.parentFile.mkdirs()
        arquivo.writeText(
            "package org.greenhouse.app.repository\n\n" +
                "internal object ApiConfig {\n" +
                "    const val API_URL: String = \"${apiUrl.get()}\"\n" +
                "    const val API_URL_ANDROID: String = \"${apiUrlAndroid.get()}\"\n" +
                "}\n",
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    android {
        namespace = "org.greenhouse.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.android)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        webMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.navigation3.browser)
        }

        commonMain {
            kotlin.srcDir(gerarApiConfig)
        }

        commonMain.dependencies {
            implementation(libs.compose.animation)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.navigation3.viewmodel)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.bundles.ktor.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmTest.dependencies {
            implementation(project(":serverApp"))
            implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.6")
            implementation("org.springframework.boot:spring-boot-starter-validation:3.5.6")
            implementation("org.springframework.boot:spring-boot-starter-test:3.5.6")
            implementation("org.springframework.boot:spring-boot-starter-web:3.5.6")
            implementation("org.springframework.boot:spring-boot-starter-security:3.5.6")
            implementation("org.junit.vintage:junit-vintage-engine:5.12.2")
            implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
            implementation("org.springframework.boot:spring-boot-starter-mail:3.5.6")
        }

        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    val isBackendTest = name == "jvmTest"
    extensions.configure<JacocoTaskExtension> {
        isEnabled = isBackendTest
    }
}

val pacoteTestesFila = "org.greenhouse.integration.fila.*"

tasks.named<Test>("jvmTest") {
    filter { excludeTestsMatching(pacoteTestesFila) }
}

tasks.register<Test>("testFila") {
    group = "verification"
    description = "Roda os testes da fila IoT contra o broker MQTT e o SMTP configurados no .env.test.local"
    val jvmTest = tasks.named<Test>("jvmTest").get()
    testClassesDirs = jvmTest.testClassesDirs
    classpath = jvmTest.classpath
    filter { includeTestsMatching(pacoteTestesFila) }
    outputs.upToDateWhen { false }
}
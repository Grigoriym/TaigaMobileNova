import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.grappim.taigamobile.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.multiplatform.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
    compileOnly(libs.kover.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.taigamobile.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = libs.plugins.taigamobile.kmp.library.asProvider().get().pluginId
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpLibraryCompose") {
            id = libs.plugins.taigamobile.kmp.library.compose.get().pluginId
            implementationClass = "KmpLibraryComposeConventionPlugin"
        }
        register("kmpSerialization") {
            id = libs.plugins.taigamobile.kmp.serialization.get().pluginId
            implementationClass = "KmpSerializationConventionPlugin"
        }
        register("kmpDi") {
            id = libs.plugins.taigamobile.kmp.di.get().pluginId
            implementationClass = "KmpDiConventionPlugin"
        }
        register("kmpNetwork") {
            id = libs.plugins.taigamobile.kmp.network.get().pluginId
            implementationClass = "KmpNetworkConventionPlugin"
        }
    }
}

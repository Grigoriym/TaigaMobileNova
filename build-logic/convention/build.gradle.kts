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
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
//    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.compose.multiplatform.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.taigamobile.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.taigamobile.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = libs.plugins.taigamobile.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("kotlinSerialization") {
            id = libs.plugins.taigamobile.kotlin.serialization.get().pluginId
            implementationClass = "KotlinSerializationConventionPlugin"
        }
        register("kotlinLibrary") {
            id = libs.plugins.taigamobile.kotlin.library.get().pluginId
            implementationClass = "KotlinLibraryConventionPlugin"
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
    }
}

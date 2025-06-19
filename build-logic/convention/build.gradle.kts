import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.grappim.taigamobile.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
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
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.taigamobile.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidHilt") {
            id = libs.plugins.taigamobile.android.hilt.get().pluginId
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("kotlinHilt") {
            id = libs.plugins.taigamobile.kotlin.hilt.get().pluginId
            implementationClass = "KotlinHiltConventionPlugin"
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
    }
}

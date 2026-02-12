plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
    alias(libs.plugins.taigamobile.kotlin.serialization)
    application
}

application {
    mainClass.set("com.grappim.taigamobile.tools.seed.MainKt")
}

dependencies {
    implementation(libs.ktor.core)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.slf4j.simple)
}

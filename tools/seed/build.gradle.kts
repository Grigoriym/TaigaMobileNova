plugins {
    alias(libs.plugins.kotlin.serialization)
    kotlin("jvm")
}

dependencies {
    implementation(libs.ktor.core)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.date.time)
    implementation(libs.slf4j.simple)
}

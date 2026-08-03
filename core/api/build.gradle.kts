plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.network)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.contentNegotiation)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.serialization.json)

            implementation(projects.core.appinfoApi)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.feature.login.dto)
        }
        commonTest.dependencies {
            // MockEngine — the Ktor plugins in this module can only be driven through a real
            // HttpClient, since HttpSend.intercept is not reachable any other way.
            implementation(libs.ktor.client.mock)
        }
    }
}

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.serialization)
    alias(libs.plugins.taigamobile.kmp.network)
}

android {
    namespace = "com.grappim.taigamobile.feature.wiki.data"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.asyncKmp)

            implementation(projects.feature.wiki.domain)

            implementation(projects.feature.workitem.mapper)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.dto)
        }
    }
}

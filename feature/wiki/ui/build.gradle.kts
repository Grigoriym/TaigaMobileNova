plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}
android {
    namespace = "com.grappim.taigamobile.feature.wiki.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.strings)
            implementation(projects.uikit)
            implementation(projects.utils.ui)
            implementation(projects.core.navigation)
            implementation(projects.core.domain)
            implementation(projects.core.storage)

            implementation(projects.feature.wiki.domain)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.workitem.ui)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.projects.domain)

            implementation(libs.androidx.paging.compose)
        }
    }
}

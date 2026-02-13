plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

android {
    namespace = "com.grappim.taigamobile.feature.dashboard.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.projects.domain)
            implementation(projects.feature.workitem.domain)
            implementation(projects.utils.formatter.datetime)
        }
    }
}

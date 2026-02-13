plugins {
    alias(libs.plugins.taigamobile.kmp.library)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.filters.domain)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.projects.domain)
        }
    }
}

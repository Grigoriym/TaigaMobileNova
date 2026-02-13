plugins {
    alias(libs.plugins.taigamobile.kmp.library)
}

android {
    namespace = "com.grappim.taigamobile.feature.sprint.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)

            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.filters.domain)

            implementation(libs.androidx.paging.common)
        }
    }
}

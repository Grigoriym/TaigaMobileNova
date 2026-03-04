plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)
            implementation(projects.utils.ui)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.dto)

            implementation(projects.feature.workitem.dto)
        }
    }
}

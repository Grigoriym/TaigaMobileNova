plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.network)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)
            implementation(projects.core.storage)
            implementation(projects.utils.ui)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.mapper)
            implementation(projects.feature.filters.dto)

            implementation(projects.feature.workitem.data)
            implementation(projects.feature.workitem.domain)
        }
    }
}

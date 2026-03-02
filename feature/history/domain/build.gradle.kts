plugins {
    alias(libs.plugins.taigamobile.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.workitem.domain)
        }
    }
}

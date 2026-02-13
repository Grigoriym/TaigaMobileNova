plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(projects.strings)
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)

            implementation(libs.androidx.paging.compose)
            implementation(libs.androidx.navigation.compose)
        }
    }
}

android {
    namespace = "com.grappim.taigamobile.utils.ui"
}

dependencies {
    testImplementation(libs.robolectric)
}

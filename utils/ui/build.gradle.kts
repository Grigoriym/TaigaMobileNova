plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.strings)
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)

            implementation(libs.androidx.paging.compose)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.core)
        }

        iosMain.dependencies {
            implementation(libs.ktor.core)
        }
    }
}

android {
    namespace = "com.grappim.taigamobile.utils.ui"
}

dependencies {
    testImplementation(libs.robolectric)
}

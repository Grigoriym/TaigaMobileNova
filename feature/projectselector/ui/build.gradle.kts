@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

val composeUiTestDep = compose.dependencies.uiTest
val composeDesktopUiTestJUnit4Dep = compose.dependencies.desktop.uiTestJUnit4
val composeDesktopCurrentOsDep = compose.dependencies.desktop.currentOs

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.strings)
            implementation(projects.uikit)
            implementation(projects.utils.ui)
            implementation(projects.core.navigation)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.feature.projects.domain)

            implementation(libs.androidx.paging.compose)
        }
        jvmTest.dependencies {
            implementation(composeUiTestDep)
            implementation(composeDesktopUiTestJUnit4Dep)
            implementation(composeDesktopCurrentOsDep)
        }
    }
}

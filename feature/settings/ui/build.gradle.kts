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
            implementation(projects.core.api)
            implementation(projects.core.appinfoApi)
            implementation(projects.core.crashApi)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.navigation)
            implementation(projects.utils.ui)
            implementation(projects.uikit)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.projects.domain)
            implementation(projects.feature.workitem.ui)
            implementation(projects.feature.filters.domain)

            implementation(libs.coil.compose)
            implementation(libs.coil.ktor)
            implementation(libs.compose.colorpicker)

            implementation(libs.jetbrains.compose.icons.extended)
        }
        jvmTest.dependencies {
            implementation(composeUiTestDep)
            implementation(composeDesktopUiTestJUnit4Dep)
            implementation(composeDesktopCurrentOsDep)
        }
    }
}

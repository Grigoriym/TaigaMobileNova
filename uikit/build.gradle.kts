@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.compose)
}

compose.resources {
    packageOfResClass = "com.grappim.taigamobile.uikit.generated.resources"
    generateResClass = always
    publicResClass = true
}

val composeUiTestDep = compose.dependencies.uiTest
val composeDesktopUiTestJUnit4Dep = compose.dependencies.desktop.uiTestJUnit4
val composeDesktopCurrentOsDep = compose.dependencies.desktop.currentOs

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.jetbrains.compose.components.resources)

            implementation(projects.utils.ui)
            implementation(projects.strings)
            implementation(projects.core.domain)
            implementation(projects.feature.filters.domain)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.projects.domain)
            implementation(projects.feature.users.domain)
            implementation(projects.core.navigation)
            implementation(projects.utils.formatter.datetime)

            implementation(libs.coil.compose)

            implementation(libs.androidx.paging.compose)
            implementation(libs.androidx.paging.common)

            implementation(libs.jetbrains.compose.icons)

            implementation(libs.markdownRenderer.core)
            implementation(libs.markdownRenderer.m3)
            implementation(libs.markdownRenderer.coil)
        }
        jvmTest.dependencies {
            implementation(composeUiTestDep)
            implementation(composeDesktopUiTestJUnit4Dep)
            implementation(composeDesktopCurrentOsDep)
        }
    }
}

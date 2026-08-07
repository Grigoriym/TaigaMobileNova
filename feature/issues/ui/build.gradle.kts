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
            implementation(projects.feature.issues.domain)
            implementation(projects.feature.history.domain)
            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.ui)
            implementation(projects.feature.workitem.ui)
            implementation(projects.feature.workitem.domain)
            implementation(projects.utils.formatter.datetime)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.sprint.domain)
            implementation(projects.feature.projects.domain)

            implementation(projects.strings)
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.asyncKmp)
            implementation(projects.utils.ui)
            implementation(projects.utils.formatter.decimal)
            implementation(projects.uikit)
            implementation(projects.core.navigation)

            implementation(libs.androidx.paging.compose)

            implementation(libs.coil.compose)
        }
        jvmTest.dependencies {
            implementation(composeUiTestDep)
            implementation(composeDesktopUiTestJUnit4Dep)
            implementation(composeDesktopCurrentOsDep)
        }
    }
}

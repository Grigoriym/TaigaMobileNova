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
            implementation(libs.filekit.dialogs)
            api(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)

            implementation(projects.strings)
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.asyncKmp)
            implementation(libs.grappim.kit.navigation)
            implementation(projects.utils.ui)
            implementation(projects.utils.formatter.decimal)
            implementation(projects.utils.formatter.datetime)
            implementation(projects.uikit)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.projects.domain)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.history.domain)
            implementation(projects.feature.sprint.domain)
            implementation(projects.feature.epics.domain)
            implementation(projects.feature.userstories.domain)

            implementation(libs.androidx.paging.compose)

            implementation(libs.coil.compose)
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

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.ui"
}

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
    }
}

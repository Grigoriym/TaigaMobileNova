plugins {
    alias(libs.plugins.taigamobile.android.library)
}

android {
    namespace = "com.grappim.taigamobile.testing"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(projects.core.domain)
    implementation(projects.core.storage)

    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.dto)
    implementation(projects.feature.issues.domain)
    implementation(projects.feature.issues.ui)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.projects.dto)
    implementation(projects.feature.workitem.data)
    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.ui)
    implementation(projects.feature.workitem.dto)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.users.dto)
    implementation(projects.feature.userstories.dto)
    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.epics.dto)
    implementation(projects.feature.epics.domain)
    implementation(projects.feature.sprint.domain)
    implementation(projects.feature.sprint.data)
    implementation(projects.feature.swimlanes.data)
    implementation(projects.feature.swimlanes.domain)
    implementation(projects.feature.tasks.domain)
    implementation(projects.feature.tasks.data)
    implementation(projects.utils.ui)

    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.mockk)
    api(libs.mockk.android)
    api(libs.androidx.arch.core.testing)
    api(libs.androidx.test.runner)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.core)
    api(libs.androidx.compose.ui.test)
    implementation(libs.androidx.navigation.compose)

    debugApi(libs.androidx.compose.ui.testManifest)

    implementation(libs.androidx.appcompat)
    implementation(libs.robolectric)
}

plugins {
    alias(libs.plugins.taigamobile.android.application)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.moduleGraphAssertion)
}

android {
    namespace = "com.grappim.taigamobile"

    defaultConfig {
        applicationId = namespace!!
        testApplicationId = "${namespace!!}.tests"

        versionCode = 31
        versionName = "2.0.0"

        project.base.archivesName.set("TaigaMobile-$versionName")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

val isGooglePlayBuild = project.gradle.startParameter.taskRequests.toString().contains("Gplay")
if (isGooglePlayBuild) {
    logger.lifecycle("✅ Applying Google Services Plugin due to detected Google Play Build!")
} else {
    logger.lifecycle("🚫 Google Services Plugin is NOT applied for this variant.")
    // Disable DependencyInfoBlock for fdroid builds
    android {
        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
        }
    }
}

dependencies {
    implementation(projects.utils.ui)
    implementation(projects.utils.formatter.decimal)
    implementation(projects.utils.formatter.datetime)

    implementation(projects.uikit)
    implementation(projects.strings)

    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)
    implementation(projects.core.asyncAndroid)
    implementation(projects.core.appinfoApi)
    implementation(projects.core.navigation)
    implementation(projects.core.serialization)

    implementation(projects.feature.dashboard.domain)
    implementation(projects.feature.dashboard.ui)

    implementation(projects.feature.login.domain)
    implementation(projects.feature.login.ui)
    implementation(projects.feature.login.data)

    implementation(projects.feature.projects.data)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.projects.dto)
    implementation(projects.feature.projects.mapper)

    implementation(projects.feature.wiki.domain)
    implementation(projects.feature.wiki.data)
    implementation(projects.feature.wiki.ui)

    implementation(projects.feature.settings.ui)

    implementation(projects.feature.projectselector.ui)

    implementation(projects.feature.profile.ui)

    implementation(projects.feature.tasks.data)
    implementation(projects.feature.tasks.domain)
    implementation(projects.feature.tasks.ui)
    implementation(projects.feature.tasks.mapper)

    implementation(projects.feature.scrum.ui)

    implementation(projects.feature.teams.ui)

    implementation(projects.feature.profile.ui)

    implementation(projects.feature.filters.data)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.ui)
    implementation(projects.feature.filters.mapper)
    implementation(projects.feature.filters.dto)

    implementation(projects.feature.swimlanes.data)
    implementation(projects.feature.swimlanes.domain)

    implementation(projects.feature.users.data)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.users.dto)
    implementation(projects.feature.users.mapper)

    implementation(projects.feature.history.domain)
    implementation(projects.feature.history.data)

    implementation(projects.feature.kanban.ui)
    implementation(projects.feature.kanban.data)
    implementation(projects.feature.kanban.domain)

    implementation(projects.feature.epics.ui)
    implementation(projects.feature.epics.domain)
    implementation(projects.feature.epics.data)
    implementation(projects.feature.epics.dto)
    implementation(projects.feature.epics.mapper)

    implementation(projects.feature.issues.data)
    implementation(projects.feature.issues.domain)
    implementation(projects.feature.issues.ui)
    implementation(projects.feature.issues.dto)
    implementation(projects.feature.issues.mapper)

    implementation(projects.feature.sprint.data)
    implementation(projects.feature.sprint.domain)
    implementation(projects.feature.sprint.ui)

    implementation(projects.feature.userstories.data)
    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.userstories.ui)
    implementation(projects.feature.userstories.dto)
    implementation(projects.feature.userstories.mapper)

    implementation(projects.feature.workitem.ui)
    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.data)
    implementation(projects.feature.workitem.mapper)
    implementation(projects.feature.workitem.dto)

    implementation(kotlin("reflect"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.viewmodel.compose)
    implementation(libs.androidx.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.core.splashScreen)
    implementation(libs.material)

    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation.layout)

    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlin.serialization)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.timber)

    implementation(libs.vanpra.color)

    implementation(libs.coil.core)
    implementation(libs.coil.okhttp)
    implementation(libs.coil.compose)

    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    // can be removed eventually with dagger update
    // https://github.com/google/dagger/issues/5001#issuecomment-3687444052
    ksp("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0")
}

moduleGraphAssert {
    maxHeight = 20
    assertOnAnyBuild = true
}

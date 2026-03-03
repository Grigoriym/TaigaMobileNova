plugins {
    alias(libs.plugins.taigamobile.android.application)
}

koinCompiler {
    userLogs = true
    debugLogs = true
}

android {
    val isGooglePlayBuild = project.gradle.startParameter.taskRequests.toString().contains("Gplay")
    if (!isGooglePlayBuild) {
        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
        }
    }

    namespace = libs.versions.app.pkg.get().toString()

    defaultConfig {
        applicationId = libs.versions.app.pkg.get().toString()
        testApplicationId = "${libs.versions.app.pkg.get()}.test"

        versionCode = libs.versions.version.code.get().toString().toInt()
        versionName = libs.versions.version.name.get().toString()
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.uikit)
    implementation(projects.core.storage)

    implementation(projects.core.logger)
    implementation(projects.core.appinfoApi)
    implementation(projects.core.asyncKmp)
    implementation(projects.strings)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.annotations)

    implementation(project.dependencies.platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashScreen)
    implementation(libs.androidx.core.ktx)

    implementation(libs.coil.core)
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
    implementation(libs.okhttp)
    implementation(libs.timber)
    implementation(libs.filekit.dialogs)
}

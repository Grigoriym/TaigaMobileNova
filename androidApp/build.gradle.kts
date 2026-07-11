plugins {
    alias(libs.plugins.taigamobile.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

koinCompiler {
    userLogs = true
    debugLogs = true
    compileSafety = false
}

android {
    if (!project.hasProperty("gplayBuild")) {
        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
        }
    }

    namespace = libs.versions.app.pkg.get()

    defaultConfig {
        applicationId = libs.versions.app.pkg.get()
        testApplicationId = "${libs.versions.app.pkg.get()}.test"

        versionCode = libs.versions.version.code.get().toInt()
        versionName = libs.versions.version.name.get()
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.feature.login.domain)
    implementation(projects.uikit)
    implementation(projects.core.storage)

    implementation(projects.core.logger)
    implementation(projects.core.appinfoApi)
    implementation(projects.core.crashApi)
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

    // Crashlytics ships in the gplay flavor only — the fdroid flavor never pulls in
    // this proprietary dependency, only a no-op CrashReporter implementation.
    gplayImplementation(project.dependencies.platform(libs.firebase.bom))
    gplayImplementation(libs.firebase.crashlytics)
}

plugins {
    alias(libs.plugins.taigamobile.android.application)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

// Applied only for the gplay flavor — F-Droid's build must not carry these plugins at all
// (they inject Firebase-related string resources regardless of flavor's dependency graph,
// which breaks both F-Droid's non-free scanner and its binary reproducibility check).
// The plugins {} block can't express this condition itself (it has no access to `project`
// or `providers`), so the plugin IDs are pulled from the catalog and applied imperatively.
if (project.hasProperty("gplayBuild")) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
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
    implementation(libs.material)

    // Applies a generated Baseline Profile (docs/perf/profiling-plan.md, task 3) at first launch
    // after install — required regardless of Play Store distribution, a plain `adb install` or
    // F-Droid-style install path does not apply the profile without it.
    implementation(libs.androidx.profileinstaller)

    // Crashlytics ships in the gplay flavor only — the fdroid flavor never pulls in
    // this proprietary dependency, only a no-op CrashReporter implementation.
    gplayImplementation(project.dependencies.platform(libs.firebase.bom))
    gplayImplementation(libs.firebase.crashlytics)

    // Play In-App Updates ship in the gplay flavor only — the fdroid flavor never pulls in
    // this proprietary dependency, only a no-op AppUpdateChecker implementation.
    gplayImplementation(libs.google.inapp.update.ktx)
}

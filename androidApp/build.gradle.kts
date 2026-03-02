
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.koin.compiler)
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
    compileSdk = libs.versions.compileSdk.get().toString().toInt()

    defaultConfig {
        applicationId = libs.versions.app.pkg.get().toString()
        testApplicationId = "${libs.versions.app.pkg.get()}.test"

        minSdk = libs.versions.minSdk.get().toString().toInt()
        targetSdk = libs.versions.targetSdk.get().toString().toInt()

        versionCode = libs.versions.version.code.get().toString().toInt()
        versionName = libs.versions.version.name.get().toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("../taigamobilenova_keystore_release.jks")
            keyAlias = System.getenv("TAIGA_ALIAS_R")
            keyPassword = System.getenv("TAIGA_KEY_PASS_R")
            storePassword = System.getenv("TAIGA_STORE_PASS_R")
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    flavorDimensions += "STORE"
    productFlavors {
        create("gplay") {
            dimension = "STORE"
            buildConfigField("Boolean", "IS_FDROID", "false")
        }
        create("fdroid") {
            dimension = "STORE"
            applicationIdSuffix = ".fdroid"
            buildConfigField("Boolean", "IS_FDROID", "true")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"

            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false

            val debugLocalHost = findProperty("debug.local.host") as String? ?: ""
            buildConfigField("String", "DEBUG_LOCAL_HOST", "\"$debugLocalHost\"")
        }
        release {
//            applicationIdSuffix = AppBuildTypes.RELEASE.applicationIdSuffix

            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "DEBUG_LOCAL_HOST", "\"\"")
        }
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    packaging.resources.excludes.apply {
        add("META-INF/ASL2.0")
        add("META-INF/notice.txt")
        add("META-INF/NOTICE.txt")
        add("META-INF/NOTICE")
        add("META-INF/license.txt")
        add("DEPENDENCIES")
    }
}

dependencies {
    implementation(project(":composeApp"))
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

    coreLibraryDesugaring(libs.android.desugarJdkLibs)
}

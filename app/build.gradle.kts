import com.android.build.api.dsl.AndroidSourceSet

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    compileSdk = 35

    namespace = "com.grappim.taigamobile"

    defaultConfig {
        applicationId = namespace!!
        testApplicationId = "${namespace!!}.tests"
        minSdk = 24
        targetSdk = 36

        versionCode = 29
        versionName = "2.0"

        project.base.archivesName.set("TaigaMobile-$versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
    }

    sourceSets {
        fun AndroidSourceSet.setupTestSrcDirs() {
            kotlin.srcDir("src/sharedTest/kotlin")
            resources.srcDir("src/sharedTest/resources")
        }

        getByName("test").setupTestSrcDirs()
        getByName("androidTest").setupTestSrcDirs()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    implementation(kotlin("reflect"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.viewmodel.compose)
    implementation(libs.androidx.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.material)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation.layout)

    debugImplementation(libs.androidx.compose.ui.testManifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.coil.compose)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.timber)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.markwon.core)
    implementation(libs.markwon.image.coil)

    implementation(libs.vanpra.color)

    testImplementation(libs.gson)
    testImplementation(libs.kotlinx.coroutines.test)

    allTestsImplementation(kotlin("test-junit"))

    testRuntimeOnly(libs.robolectric)

    allTestsImplementation("androidx.test:core-ktx:1.6.1")
    allTestsImplementation("androidx.test:runner:1.6.2")
    allTestsImplementation("androidx.test.ext:junit-ktx:1.2.1")

    val postgresDriverVersion = "42.3.6"
    testRuntimeOnly("org.postgresql:postgresql:$postgresDriverVersion")
    androidTestRuntimeOnly("org.postgresql:postgresql:$postgresDriverVersion")

    testImplementation(libs.mockk)
}

fun DependencyHandler.allTestsImplementation(dependencyNotation: Any) {
    testImplementation(dependencyNotation)
    androidTestImplementation(dependencyNotation)
}

tasks.register<Exec>("launchTestInstance") {
    commandLine("../taiga-test-instance/launch-taiga.sh")
}

tasks.register<Exec>("stopTestInstance") {
    commandLine("../taiga-test-instance/stop-taiga.sh")
}

tasks.withType<Test> {
    dependsOn("launchTestInstance")
    finalizedBy("stopTestInstance")
}

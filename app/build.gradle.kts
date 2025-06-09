import com.android.build.api.dsl.AndroidSourceSet

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
}

val composeVersion = "1.1.1"

android {
    compileSdk = 35

    namespace = "io.eugenethedev.taigamobile"

    defaultConfig {
        applicationId = namespace!!
        testApplicationId = "${namespace!!}.tests"
        minSdk = 24
        targetSdk = 36
        versionCode = 29
        versionName = "1.9"
        project.base.archivesName.set("TaigaMobile-$versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

//    signingConfigs {
//        getByName("debug") {
//            storeFile = file("./keystores/debug.keystore")
//            storePassword = "android"
//            keyAlias = "debug"
//            keyPassword = "android"
//        }
//
//        create("release") {
//            val properties = Properties().also {
//                it.load(file("./signing.properties").inputStream())
//            }
//            storeFile = file("./keystores/release.keystore")
//            storePassword = properties.getProperty("password")
//            keyAlias = properties.getProperty("alias")
//            keyPassword = properties.getProperty("password")
//        }
//    }


    buildTypes {
        getByName("debug") {
//            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//            signingConfig = signingConfigs.getByName("release")
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
    implementation(enforcedPlatform(kotlin("bom")))

    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    implementation(kotlin("reflect"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation("com.google.android.material:material:1.6.0")

    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material3:material3:1.0.0-alpha09")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation(libs.androidx.activity.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation(libs.androidx.navigation.compose)

    val accompanistVersion = "0.23.1"
    implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager-indicators:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-insets:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")

    implementation("io.coil-kt:coil-compose:1.3.2")

    implementation("androidx.paging:paging-compose:1.0.0-alpha14")

    val coroutinesVersion = "1.6.2"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    val moshiVersion = "1.15.2"
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")

    val okHttpVersion = "4.9.3"
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    val daggerVersion = "2.56.2"
    implementation("com.google.dagger:dagger-android:$daggerVersion")
    ksp("com.google.dagger:dagger-android-processor:$daggerVersion")
    ksp("com.google.dagger:dagger-compiler:$daggerVersion")

    implementation(libs.timber)

    val markwonVersion = "4.6.2"
    implementation("io.noties.markwon:core:$markwonVersion")
    implementation("io.noties.markwon:image-coil:$markwonVersion")

    implementation("io.github.vanpra.compose-material-dialogs:color:0.7.0")

    allTestsImplementation(kotlin("test-junit"))

    testRuntimeOnly("org.robolectric:robolectric:4.8.1")

    allTestsImplementation("androidx.test:core-ktx:1.4.0")
    allTestsImplementation("androidx.test:runner:1.4.0")
    allTestsImplementation("androidx.test.ext:junit-ktx:1.1.3")

    val postgresDriverVersion = "42.3.6"
    testRuntimeOnly("org.postgresql:postgresql:$postgresDriverVersion")
    androidTestRuntimeOnly("org.postgresql:postgresql:$postgresDriverVersion")

    implementation("com.google.code.gson:gson:2.9.0")

    testImplementation("io.mockk:mockk:1.12.4")
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



import org.gradle.api.attributes.Usage

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.koin.compiler)
}

// Collect CMP compose resources from KMP library modules that have them.
// Needed because CMP 1.10.1 does not wire resources into AARs when using
// the AGP 9 KMP library plugin (componentSources.assets returns null).
val composeAndroidResources by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "compose-android-resources"))
    }
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

    composeAndroidResources(projects.strings)
    composeAndroidResources(projects.uikit)
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

// Custom task to merge CMP resources from multiple KMP library modules into one directory.
// Uses Gradle's managed properties so AGP can call addGeneratedSourceDirectory with it.
abstract class CollectComposeAssetsTask : DefaultTask() {
    @get:InputFiles
    abstract val sourceDirectories: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun collect() {
        val dest = outputDirectory.get().asFile
        dest.deleteRecursively()
        dest.mkdirs()
        sourceDirectories.forEach { srcDir ->
            if (srcDir.isDirectory) srcDir.copyRecursively(dest, overwrite = true)
        }
    }
}

// Wire CMP compose resources (strings, uikit) into each Android variant's assets.
// See docs/cmp-resources-android-fix.md for root cause analysis.
androidComponents {
    onVariants { variant ->
        val assets = variant.sources.assets ?: return@onVariants
        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        val collectTask = tasks.register(
            "collect${variantName}ComposeAssets",
            CollectComposeAssetsTask::class.java,
        ) {
            sourceDirectories.from(composeAndroidResources.incoming.files)
        }
        assets.addGeneratedSourceDirectory(collectTask, CollectComposeAssetsTask::outputDirectory)
    }
}
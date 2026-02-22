import com.grappim.taigamobile.buildlogic.AppBuildTypes
import com.grappim.taigamobile.buildlogic.configureFlavors
import com.grappim.taigamobile.buildlogic.configureKmp
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

koinCompiler {
    userLogs = true
    debugLogs = true
}

android {
    namespace = "com.grappim.taigamobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.grappim.taigamobile"
        testApplicationId = "com.grappim.taigamobile.test"

        minSdk = 24
        targetSdk = 36

        versionCode = 38
        versionName = "2.0.7"
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

    configureFlavors(this)

    buildTypes {
        debug {
            applicationIdSuffix = AppBuildTypes.DEBUG.applicationIdSuffix

            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false

            val debugLocalHost = findProperty("debug.local.host") as String? ?: ""
            buildConfigField("String", "DEBUG_LOCAL_HOST", "\"$debugLocalHost\"")
        }
        release {
            applicationIdSuffix = AppBuildTypes.RELEASE.applicationIdSuffix

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

val isGooglePlayBuild = project.gradle.startParameter.taskRequests.toString().contains("Gplay")
if (!isGooglePlayBuild) {
    android {
        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
        }
    }
}

kotlin {
    configureKmp()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                implementation(libs.filekit.dialogs.compose)

                implementation(libs.compose.icons.extended)

                implementation(projects.utils.ui)
                implementation(projects.utils.formatter.decimal)
                implementation(projects.utils.formatter.datetime)

                implementation(projects.uikit)
                implementation(projects.strings)

                implementation(projects.core.api)
                implementation(projects.core.domain)
                implementation(projects.core.storage)
                implementation(projects.core.asyncKmp)
                implementation(projects.core.appinfoApi)
                implementation(projects.core.navigation)
                implementation(projects.core.serialization)

                implementation(projects.feature.dashboard.domain)
                implementation(projects.feature.dashboard.ui)

                implementation(projects.feature.login.domain)
                implementation(projects.feature.login.ui)
                implementation(projects.feature.login.data)
                implementation(projects.feature.login.dto)

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
                implementation(projects.feature.profile.domain)

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
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.material3)
                implementation(libs.androidx.compose.material)
                implementation(libs.androidx.compose.ui)

                implementation(libs.material)
                implementation(libs.timber)

                implementation(libs.androidx.core.splashScreen)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.viewmodel.compose)

                implementation(libs.okhttp)
                implementation(libs.coil.okhttp)
                implementation(libs.coil.core)
                implementation(libs.coil.compose)
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugarJdkLibs)
}

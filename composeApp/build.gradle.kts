import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
    alias(libs.plugins.build.konfig)
}

buildkonfig {
    packageName = libs.versions.app.pkg.get()

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", libs.versions.version.name.get())
        buildConfigField(FieldSpec.Type.STRING, "VERSION_CODE", libs.versions.version.code.get())
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEBUG", "false")
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_FDROID", "false")
        buildConfigField(FieldSpec.Type.STRING, "DEBUG_LOCAL_HOST", "")
        buildConfigField(FieldSpec.Type.STRING, "BUILD_TYPE", "release")
    }

    defaultConfigs("debug") {
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEBUG", "true")
        buildConfigField(FieldSpec.Type.STRING, "BUILD_TYPE", "debug")
        val host = findProperty("debug.local.host") as String? ?: ""
        buildConfigField(FieldSpec.Type.STRING, "DEBUG_LOCAL_HOST", host)
    }
}

koinCompiler {
    userLogs = true
    debugLogs = true
}

compose.desktop {
    application {
        mainClass = "com.grappim.taigamobile.TaigaMobileDesktopKt"

        jvmArgs += listOf(
            "-Xmx256m",
            "-Xms64m",
            "-XX:+UseSerialGC",
            "-XX:MaxMetaspaceSize=128m"
        )

//        https://github.com/Guardsquare/proguard/releases
        buildTypes.release.proguard {
            version.set("7.8.2")
            isEnabled = true
            configurationFiles.from("proguard-desktop.pro")
        }

        nativeDistributions {
            modules("jdk.unsupported")

            targetFormats(TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)
            packageName = libs.versions.app.name.get()
            packageVersion = libs.versions.version.name.get()
            description = libs.versions.app.description.get()
            vendor = libs.versions.app.vendor.get()
            copyright = "Copyright 2026 ${libs.versions.app.vendor.get()}"

            linux {
                iconFile.set(project.file("../art/taiga-mobile-logo.png"))

                debMaintainer = libs.versions.app.vendor.get()
                debPackageVersion = libs.versions.version.name.get()
                appCategory = libs.versions.app.category.get()
                menuGroup = libs.versions.app.menugroup.get()
                shortcut = true
            }
            windows {
                iconFile.set(project.file("../art/taiga-mobile-logo.ico"))

                menuGroup = libs.versions.app.menugroup.get()
                shortcut = true
            }
            macOS {
                iconFile.set(project.file("../art/taiga-mobile-logo.icns"))
            }
        }
    }
}

kotlin {
    android {
        namespace = "${libs.versions.app.pkg.get()}.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "TaigaMobileNovaIos"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                implementation(libs.filekit.dialogs.compose)

                implementation(libs.jetbrains.compose.icons.extended)

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
                implementation(projects.core.crashApi)
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

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))

                // koin-test is JVM-only here on purpose: KoinGraphTest resolves the whole
                // real graph, which only works on a platform that has every actual (JVM does).
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.test)
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

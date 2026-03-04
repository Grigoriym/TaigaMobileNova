plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.jetbrains.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.build.konfig) apply false

//    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

kover {
    reports {
        filters {
            excludes {
                // Generates all 4 JVM variants for each suffix:
                // **.*Foo, **.*FooKt, **.*Foo$*, **.*FooKt$*
                fun variants(vararg names: String): Array<String> = names.flatMap { name ->
                    listOf(
                        "**.*$name",
                        "**.*${name}Kt",
                        "**.*$name\$*",
                        "**.*${name}Kt\$*"
                    )
                }.toTypedArray()

                classes(
                    *variants(
                        // Data layer
                        "Api", "ApiImpl", "DTO", "Repository",
                        // Architecture boilerplate
                        "Delegate", "Plugin", "Module",
                        "TimberLogger", "PagingSource", "Exception",
                        // App entry points & platform glue
                        "App", "Desktop", "Activity",
                        // UI — composables & navigation
                        "DrawerDestination", "IconSource",
                        "UI", "Widget", "Screen", "Dialog", "BottomSheet",
                        "Destination", "NavigationExtensions", "Graph", "NavHost",
                        // Compose compiler synthetic lambdas (always appear as ComposableSingletons$FileKt)
                        "ComposableSingletons",
                        // Misc
                        "ImmutableListSerializer", "BuildKonfig"
                    )
                )

                classes(
                    // Top-level extension files (no base class, only a Kt facade)
                    "**.*ResultExtensionKt",
                    "**.*TryCatchExtensionsKt",
                    // Constants objects
                    "**.*ApiConstants",
                    // Preferences — broad glob covers all generated variants
                    "**.*Preferences*",
                    "**.*BuildConfig"
                )
            }
        }
        total {
            xml { }
            html { }
        }
    }
}

dependencies {
    kover(projects.feature.login.domain)
    kover(projects.feature.login.ui)
    kover(projects.feature.login.data)
    kover(projects.feature.login.dto)
    kover(projects.utils.ui)
    kover(projects.core.api)
    kover(projects.core.storage)
    kover(projects.core.domain)
    kover(projects.core.appinfoApi)
    kover(projects.core.navigation)
    kover(projects.core.serialization)
    kover(projects.core.asyncKmp)
    kover(projects.core.logger)
    kover(projects.feature.dashboard.ui)
    kover(projects.feature.dashboard.domain)
    kover(projects.feature.projects.data)
    kover(projects.feature.projects.domain)
    kover(projects.feature.projects.mapper)
    kover(projects.feature.projects.dto)
    kover(projects.feature.wiki.data)
    kover(projects.feature.wiki.domain)
    kover(projects.feature.wiki.ui)
    kover(projects.feature.epics.data)
    kover(projects.feature.epics.domain)
    kover(projects.feature.epics.ui)
    kover(projects.feature.epics.dto)
    kover(projects.feature.epics.mapper)
    kover(projects.feature.issues.data)
    kover(projects.feature.issues.ui)
    kover(projects.feature.issues.domain)
    kover(projects.feature.issues.dto)
    kover(projects.feature.issues.mapper)
    kover(projects.strings)
    kover(projects.feature.sprint.data)
    kover(projects.feature.sprint.ui)
    kover(projects.feature.sprint.domain)
    kover(projects.feature.userstories.data)
    kover(projects.feature.userstories.ui)
    kover(projects.feature.userstories.domain)
    kover(projects.feature.userstories.mapper)
    kover(projects.feature.userstories.dto)
    kover(projects.feature.settings.ui)
    kover(projects.feature.users.data)
    kover(projects.feature.users.domain)
    kover(projects.feature.users.mapper)
    kover(projects.feature.users.dto)
    kover(projects.feature.kanban.ui)
    kover(projects.feature.kanban.domain)
    kover(projects.feature.tasks.data)
    kover(projects.feature.tasks.domain)
    kover(projects.feature.tasks.ui)
    kover(projects.feature.tasks.mapper)
    kover(projects.feature.scrum.ui)
    kover(projects.feature.profile.ui)
    kover(projects.feature.profile.domain)
    kover(projects.feature.projectselector.ui)
    kover(projects.feature.teams.ui)
    kover(projects.feature.filters.data)
    kover(projects.feature.filters.domain)
    kover(projects.feature.filters.ui)
    kover(projects.feature.filters.mapper)
    kover(projects.feature.filters.dto)
    kover(projects.feature.swimlanes.data)
    kover(projects.feature.swimlanes.domain)
    kover(projects.feature.history.data)
    kover(projects.feature.history.domain)
    kover(projects.utils.formatter.decimal)
    kover(projects.utils.formatter.datetime)
    kover(projects.feature.workitem.ui)
    kover(projects.feature.workitem.domain)
    kover(projects.feature.workitem.data)
    kover(projects.feature.workitem.mapper)
    kover(projects.feature.workitem.dto)
    kover(projects.composeApp)
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

val syncIosVersion by tasks.registering {
    val versionCode = libs.versions.version.code.get()
    val versionName = libs.versions.version.name.get()
    val xcconfig = rootProject.file("iosApp/Configuration/Config.xcconfig")

    inputs.property("versionCode", versionCode)
    inputs.property("versionName", versionName)
    outputs.file(xcconfig)

    doLast {
        val updated = xcconfig.readText()
            .replace(Regex("CURRENT_PROJECT_VERSION=.*"), "CURRENT_PROJECT_VERSION=$versionCode")
            .replace(Regex("MARKETING_VERSION=.*"), "MARKETING_VERSION=$versionName")
        xcconfig.writeText(updated)
    }
}

project(":composeApp").tasks.matching { it.name.startsWith("assembleTaigaMobileNova") }.configureEach {
    dependsOn(syncIosVersion)
}

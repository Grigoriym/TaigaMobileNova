import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false

    alias(libs.plugins.jetbrains.compose.compiler) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.compose) apply false

    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.build.konfig) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false

    alias(libs.plugins.detekt)
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
                        // Architecture boilerplate. Deliberately *not* "Plugin" — the only
                        // classes matching that suffix are core/api's five Ktor plugins
                        // (TokenRefreshPlugin, ErrorMappingPlugin, ...), which are hand-written
                        // auth/error-mapping logic, not boilerplate. See docs/revisit.md #10.
                        "Delegate", "Module",
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
                    "**.*BuildConfig",
                    // The file-level `Foo_androidKt` facade of every `Foo.android.kt`. The root
                    // aggregation locates both the `jvm` target and the KMP Android library
                    // target as JVM origins (KotlinMultiPlatformLocator), so these are compiled
                    // into the report even though CI runs jvmTest only — permanently 0 %. See
                    // docs/revisit.md #23.
                    "**.*_androidKt",
                    "**.*_androidKt\$*"
                )

                // Compose Multiplatform generated string resources — large generated package
                // (~9 000 lines) that inflates the denominator but has no testable logic
                packages("com.grappim.taigamobile.strings.generated.resources")

                // Room / storage infrastructure — generated DAOs, DB wiring, cache, DI glue
                packages(
                    "com.grappim.taigamobile.core.storage.db",
                    "com.grappim.taigamobile.core.storage.db.dao",
                    "com.grappim.taigamobile.core.storage.db.wrapper",
                    "com.grappim.taigamobile.core.storage.di",
                    "com.grappim.taigamobile.core.storage.network",
                    "com.grappim.taigamobile.core.storage.cache",
                    // androidMain-only SharedPreferences delegates (StringPreference) — same
                    // Android-variant-in-jvmTest-only-CI unreachability as the _androidKt facades
                    // above. See docs/revisit.md #17 and #23.
                    "com.grappim.taigamobile.core.storage.utils"
                )
            }
        }
        total {
            xml { }
            html { }

            // Floors, not targets. Ratchet them up as coverage improves; never lower them to
            // make a build pass. The branch bound is the point of the exercise — it sits ~15
            // points under the line bound, and that gap is the untested error paths.
            //
            // Bounds are ~3 points under what koverVerify measured on 2026-08-08 (after
            // docs/revisit.md #23 excluded Android-variant-only classes — the *_androidKt
            // facades and core.storage.utils — that jvmTest-only CI can never cover): line
            // 95.4814 %, branch 81.3863 %. Previous reading (before #23): line 94.9199 %,
            // branch 80.2198 %.
            //
            // koverXmlReport and koverVerify agree to six significant figures *within a single
            // invocation* — VariantReportsSet hands both tasks the same filters and the same
            // artifacts. Across invocations they can differ, because the report counts whichever
            // compiler output happens to exist on disk, which an Android build or a KSP re-run
            // changes. So take the reading from a run that also produced the XML you compare it
            // against. See docs/issues/2026-08-07-kover-excludes-and-report-mode-flip.md.
            verify {
                rule("Line coverage") {
                    bound {
                        minValue = 92
                        coverageUnits = CoverageUnit.LINE
                    }
                }
                rule("Branch coverage") {
                    bound {
                        minValue = 78
                        coverageUnits = CoverageUnit.BRANCH
                    }
                }
            }
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
    kover(projects.core.crashApi)
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

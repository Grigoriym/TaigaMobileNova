package com.grappim.taigamobile.di

import org.koin.core.Koin
import org.koin.core.logger.Level
import org.koin.plugin.module.dsl.koinApplication

/**
 * The one [KoinApp] graph for this test JVM. The JVM DataStore backends (`StorageModule.jvm.kt`)
 * throw "multiple DataStores active for the same file" the moment a second `koinApplication<KoinApp>`
 * touches the same file in one process, so every jvmTest class in this package that needs the real
 * graph must resolve it through this single memoized instance rather than calling
 * `koinApplication<KoinApp>` itself (docs/revisit.md #24 — `KoinGraphTest` and the live-Taiga
 * `*IntegrationTest` classes used to each build their own, which crashed whichever one Gradle
 * happened to run second).
 */
internal val sharedTestKoinGraph: Koin by lazy {
    koinApplication<KoinApp> { printLogger(Level.NONE) }.koin
}

# Koin Annotations — KMP Tips & Tricks

Practical guidance for projects using koin-annotations in a Kotlin Multiplatform (KMP) setup.

---

## 1. KSP Setup for KMP

The most common mistake is wiring KSP incorrectly for multiplatform. You need separate `ksp` configurations per target **plus** `kspCommonMainMetadata` for shared code.

```kotlin
// build.gradle.kts
kotlin {
    sourceSets.named("commonMain").configure {
        // Required: point to KSP-generated common sources
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

dependencies {
    // Annotations go in commonMain
    commonMainImplementation(libs.koin.annotations)

    // Compiler runs on common metadata AND each platform
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid",            libs.koin.ksp.compiler)
    add("kspIosX64",             libs.koin.ksp.compiler)
    add("kspIosArm64",           libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64",  libs.koin.ksp.compiler)
    add("kspJvm",                libs.koin.ksp.compiler) // if desktop/server target
}
```

**Why:** KSP processes `commonMain` as metadata first, then each platform re-processes the output. Missing `kspCommonMainMetadata` means shared classes are ignored.

---

## 2. Enable Compile-Time Safety

Turn on `KOIN_CONFIG_CHECK` during development. It catches missing dependencies, circular dependencies, and misconfigured modules **at compile time** instead of runtime crashes.

```kotlin
ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}
```

Disable it in CI release builds if it becomes slow on large graphs.

---

## 3. Use @KoinViewModel (Not Android-Specific ViewModel)

Since 1.4.0, `@KoinViewModel` generates using `koin-core-viewmodel` — the unified cross-platform API. Make sure you have this KSP option enabled (it's the default since 2.2.0):

```kotlin
ksp {
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
}
```

This means your ViewModel definitions in `commonMain` work on Android, iOS, and Desktop without platform-specific wiring.

```kotlin
// commonMain — works everywhere
@KoinViewModel
class HomeViewModel(val repo: HomeRepository) : ViewModel()
```

---

## 4. Expect/Actual Modules for Platform Differences

Use `expect`/`actual` on `@Module` classes to handle platform-specific wiring cleanly. Keep the interface in `commonMain` and scan platform packages in each actual.

```kotlin
// commonMain
@Module
expect class PlatformModule()

// androidMain
@Module
@ComponentScan("com.example.android")
actual class PlatformModule()

// iosMain
@Module
actual class PlatformModule()

// desktopMain
@Module
@ComponentScan("com.example.desktop")
actual class PlatformModule()
```

Then include it in your shared app module:

```kotlin
// commonMain
@Module(includes = [PlatformModule::class])
@ComponentScan("com.example.shared")
class AppModule()
```

---

## 5. Expect/Actual Classes for Platform Components

Annotate the `expect` class — the compiler handles generating the right bindings per platform.

```kotlin
// commonMain
@Single
expect class Analytics() {
    fun track(event: String)
}

// androidMain
actual class Analytics {
    actual fun track(event: String) { /* Firebase, etc. */ }
}

// iosMain
actual class Analytics {
    actual fun track(event: String) { /* Firebase iOS SDK */ }
}
```

No extra module function needed — koin picks up the expect annotation and generates the binding.

---

## 6. Module Includes vs ComponentScan — When to Use Which

| Situation | Prefer |
|-----------|--------|
| Grouping platform-specific annotated classes | `@ComponentScan("com.example.platform")` |
| Combining logically separate modules | `@Module(includes = [A::class, B::class])` |
| Large feature boundaries | Both: scan within feature, include from root |
| Libraries / external definitions | `@Module` with explicit `@Single`/`@Factory` functions |

Avoid scanning overly broad packages — it slows down KSP and can pick up test classes.

---

## 7. Qualifier Patterns (@Named / @Qualifier)

**String qualifier** — quick and easy, but typo-prone:

```kotlin
@Single
@Named("remote")
class RemoteDataSource() : DataSource

@Single
@Named("local")
class LocalDataSource() : DataSource
```

**Type qualifier** — safer, rename-refactor friendly:

```kotlin
annotation class Remote
annotation class Local

@Single
@Remote
class RemoteDataSource() : DataSource

@Single
@Local
class LocalDataSource() : DataSource

// Injection site
@Factory
class Repository(@Remote val remote: DataSource, @Local val local: DataSource)
```

Use `@Qualifier` on your own annotation to make it a koin qualifier:

```kotlin
@Qualifier
annotation class Remote
```

---

## 8. Runtime Parameters with @InjectedParam

When a dependency can't be known at graph construction time (e.g., user ID after login), use `@InjectedParam`:

```kotlin
@Factory
class UserProfileLoader(@InjectedParam val userId: String, val api: Api)

// Caller site
val loader = koin.get<UserProfileLoader> { parametersOf(currentUserId) }
```

This generates `factory { params -> UserProfileLoader(params.get(), get()) }` — correct and type-safe.

---

## 9. Scope Management Patterns

**Custom scope tied to a domain object:**

```kotlin
class UserSession

@Scope(UserSession::class)
@Scoped
class CartService(val api: Api)

@Scope(UserSession::class)
@Scoped
class UserPreferences(val db: Db)
```

Create/close the scope in your session lifecycle:

```kotlin
// On login
val userScope = koin.createScope<UserSession>("user_${userId}")

// On logout
userScope.close()
```

**For Compose Multiplatform**, prefer `@ViewModelScope` as the lifecycle anchor — it survives recomposition without needing manual scope management.

---

## 10. Lazy Injection for Expensive Dependencies

If a dependency is heavy to construct but not always needed, declare it as `Lazy<T>` in the constructor:

```kotlin
@Single
class ReportService(val heavyExporter: Lazy<PdfExporter>)
```

Koin generates `inject<PdfExporter>()` — the `PdfExporter` is only constructed on first access.

---

## 11. List Injection for Plugin / Strategy Patterns

Inject all registered implementations of an interface at once:

```kotlin
interface EventHandler
@Single class AnalyticsHandler : EventHandler
@Single class LoggingHandler : EventHandler
@Single class CrashHandler : EventHandler

@Single
class EventBus(val handlers: List<EventHandler>)
```

Koin generates `getAll<EventHandler>()` — automatically collects every registered binding. Great for plugin registries, interceptor chains, etc.

---

## 12. @Property for External Configuration

Inject values from Koin's property system (loaded from `koin.properties` or passed programmatically):

```kotlin
@Single
class ApiClient(@Property("api.base_url") val baseUrl: String)
```

Set properties at startup:

```kotlin
startKoin {
    properties(mapOf("api.base_url" to BuildConfig.API_URL))
    modules(AppModule().module)
}
```

Keeps configuration injectable and testable without hard-coded constants.

---

## 13. ComponentScan Glob Patterns

`@ComponentScan` supports glob matching — useful for monorepos or feature-module layouts:

```kotlin
@ComponentScan("com.example.features")        // scans com.example.features and subpackages
@ComponentScan("com.example.**")              // all subpackages, excludes root
@ComponentScan("com.example.*.repository")    // single-level wildcard
@ComponentScan("com.**.data.*Repository")     // complex pattern
```

Use more specific patterns to keep scan scope small and build times fast.

---

## 14. @Monitor for Performance Insights (Kotzilla)

If you're diagnosing slow startup or identifying hot paths, add `@Monitor` to critical services:

```kotlin
@Single
@Monitor
class SyncService(val api: Api, val db: Db)
```

This generates a proxy class that traces method calls with timing and error data sent to the Kotzilla platform. Requires:

1. The `allOpen` compiler plugin (so the proxy can subclass)
2. `kotzilla-core` dependency
3. A Kotzilla API key

Remove `@Monitor` in production if you don't have Kotzilla configured — it adds proxy overhead.

---

## 15. JSR-330 Migration Path

If you're migrating from Hilt, Dagger, or Guice, `koin-jsr330` lets you keep Jakarta annotations during the transition:

```kotlin
// Still compiles and works with koin
import jakarta.inject.Singleton
import jakarta.inject.Inject

@Singleton
class MyService @Inject constructor(val dep: Dependency)
```

Migrate to native Koin annotations incrementally — `@Singleton` → `@Single`, `@Inject` constructor → standard constructor injection, etc.

---

## 16. KSP Diagnostic Options

Useful flags for debugging generation issues:

```kotlin
ksp {
    arg("KOIN_CONFIG_CHECK",       "true")  // validate dependency graph at compile time
    arg("KOIN_LOG_TIMES",          "true")  // print generation timing per module
    arg("KOIN_GENERATION_PACKAGE", "org.koin.ksp.generated")  // change output package if needed
    arg("KOIN_EXPORT_DEFINITIONS", "true")  // generate standalone definition properties
}
```

---

## 17. Startup Wiring for KMP

The simplest cross-platform startup pattern using generated modules:

```kotlin
// commonMain
fun initKoin(additionalModules: List<Module> = emptyList()) = startKoin {
    modules(AppModule().module + additionalModules)
}

// androidMain — in Application.onCreate()
initKoin(listOf(androidContext(this)))

// iosMain — in app entry point
initKoin()
```

If you use `@KoinApplication`, the generator creates `startKoin()` and `koinApplication()` helpers automatically — but manual setup gives you more control in KMP.

---

## 18. Testing with Koin Annotations

Override definitions in tests without changing production code:

```kotlin
@Test
fun `my test`() = runTest {
    val koin = koinApplication {
        modules(AppModule().module)
        modules(module {
            // Override specific definitions
            single<Api> { FakeApi() }
        })
    }.koin

    val service = koin.get<MyService>()
    // ...
    koin.close()
}
```

Use `KOIN_CONFIG_CHECK` so test configurations are also validated.

---

## Quick Reference: Annotation Cheat Sheet

| Goal | Annotation |
|------|-----------|
| Singleton (one instance forever) | `@Single` |
| New instance each injection | `@Factory` |
| Instance tied to a scope | `@Scoped` |
| ViewModel (all platforms) | `@KoinViewModel` |
| Android WorkManager worker | `@KoinWorker` |
| Auto-discover classes in package | `@ComponentScan` |
| Group definitions | `@Module` |
| Include sub-modules | `@Module(includes = [...])` |
| String qualifier | `@Named("key")` |
| Type qualifier | Custom annotation + `@Qualifier` |
| Runtime parameter | `@InjectedParam` |
| Config property | `@Property("key")` |
| Custom scope boundary | `@Scope(MyClass::class)` |
| Resolve from specific scope | `@ScopeId` |
| Performance monitoring | `@Monitor` |
| Eager initialization | `@Single(createdAtStart = true)` |
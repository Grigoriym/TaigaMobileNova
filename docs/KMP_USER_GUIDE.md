# Koin Compiler Plugin — KMP User Guide

A practical reference for developers using this plugin in Kotlin Multiplatform projects.

---

## Annotations at a Glance

### Definition Annotations

| Annotation | Applies To | Generated DSL |
|-----------|-----------|----------------|
| `@Singleton` / `@Single` | Class, module function, top-level function | `buildSingle { T(get(), ...) }` |
| `@Factory` | Class, module function, top-level function | `buildFactory { T(get(), ...) }` |
| `@Scoped` | Class, module function, top-level function | `buildScoped { T(get(), ...) }` |
| `@KoinViewModel` | Class, module function, top-level function | `buildViewModel { T(get(), ...) }` |
| `@KoinWorker` | Class only | `buildWorker { T(get(), ...) }.bind(ListenableWorker)` |

### Module Annotations

| Annotation | Purpose |
|-----------|---------|
| `@Module` | Marks class as a Koin module container — required for `.module` generation |
| `@ComponentScan` | Scans package for annotated classes and top-level functions |
| `@ComponentScan("pkg1", "pkg2")` | Scans specific packages only |
| `@Configuration` | Tags module for auto-discovery via hint functions |
| `@Configuration("label")` | Tagged discovery — filter by label in `@KoinApplication` |
| `@KoinApplication(modules = [...])` | Explicit module list for `startKoin<T>()` |
| `@KoinApplication(configurations = ["label"])` | Auto-discover `@Configuration("label")` modules |

---

## How the Plugin Processes Your Code

### Two Compiler Phases

**FIR Phase** (declaration generation):
1. Finds all `@Module` classes (skips `expect` — only processes `actual`)
2. Generates `val ModuleClass.module: Module` extension property (empty body)
3. Generates hint functions in `org.koin.plugin.hints` for `@Configuration` modules

**IR Phase** (code generation — 4 sub-phases):

| Sub-phase | What It Does |
|-----------|-------------|
| `KoinHintTransformer` | Fills hint function bodies; makes them metadata-visible for cross-module discovery |
| `KoinAnnotationProcessor` | Scans annotated classes/functions/top-levels; fills `.module` property body |
| `KoinDSLTransformer` | Transforms `single<T>()`, `factory<T>()`, etc. DSL calls |
| `KoinStartTransformer` | Transforms `startKoin<T>()` to inject discovered/explicit modules |

### What Generated Code Looks Like

**Input:**
```kotlin
@Module
@ComponentScan
class AppModule

@Singleton
class ServiceA(val b: ServiceB, val c: ServiceC?)

@Factory
class ServiceB
```

**Output (filled in by plugin):**
```kotlin
val AppModule.module: Module get() = module {
    buildSingle(ServiceA::class, null) { scope, params ->
        ServiceA(scope.get(), scope.getOrNull())  // nullable → getOrNull()
    }
    buildFactory(ServiceB::class, null) { scope, params ->
        ServiceB()
    }
}
```

---

## Parameter Injection Rules

The plugin inspects each constructor/function parameter and picks the injection call:

| Parameter Type | Annotation | Generated Call |
|---------------|-----------|----------------|
| `T` | none | `scope.get()` |
| `T?` | none | `scope.getOrNull()` |
| `T` | `@Named("x")` | `scope.get(named("x"))` |
| `T` | `@Qualifier(MyType::class)` | `scope.get(typeQualifier<MyType>())` |
| `T` | `@InjectedParam` | `params.get()` |
| `String`/`Int`/etc. | `@Property("key")` | `scope.getProperty("key")` |
| `String`/`Int`/etc. | `@Property("key")` + `@PropertyValue("default")` | `scope.getProperty("key", default)` |
| `Lazy<T>` | none | `scope.inject()` |
| `List<T>` | none | `scope.getAll()` |

---

## Qualifier System

### String qualifiers
```kotlin
@Singleton @Named("prod")
class ProdApiClient : ApiClient

@Factory
class App(@Named("prod") val client: ApiClient)
```

### Type qualifiers
```kotlin
@Target @Retention(RUNTIME) @Qualifier
annotation class Prod

@Singleton @Qualifier(Prod::class)
class ProdService : Service

@Factory
class App(@Qualifier(Prod::class) val service: Service)
```

---

## Scope Support

```kotlin
class SessionScope  // marker class

@Scoped @Scope(SessionScope::class)
class SessionData

@Scoped @Scope(SessionScope::class)
class SessionService(val data: SessionData)

// Generated:
scope<SessionScope> {
    buildScoped(SessionData::class, null) { ... }
    buildScoped(SessionService::class, null) { ... }
}
```

Android scope archetypes: `@ViewModelScope`, `@ActivityScope`, `@ActivityRetainedScope`, `@FragmentScope`.

---

## Top-Level Functions

Functions outside `@Module` classes are discovered if `@ComponentScan` covers their package:

```kotlin
// com/example/services.kt
@Singleton
fun provideDatabase(): DatabaseService = PostgresDatabase()

@Factory
fun provideCache(db: DatabaseService): CacheService = RedisCache(db)

@Single @Named("http")
fun provideHttpClient(): NetworkClient = OkHttpClient()

// Module
@Module @ComponentScan("com.example")
class AppModule
```

The function **return type** is the bound type; **parameters** are injected exactly like constructor parameters.

---

## Module Functions

Functions inside `@Module` classes don't need `@ComponentScan`:

```kotlin
@Module
class ApiModule {
    @Single
    fun provideApi(config: Config): ApiService = ApiServiceImpl(config.baseUrl)

    @Factory
    fun provideClient(api: ApiService): ApiClient = ApiClient(api)
}
```

---

## Auto-Discovery with @Configuration

```kotlin
// feature module
@Module @ComponentScan @Configuration
class FeatureModule

// app module — no explicit list needed
@KoinApplication
object MyApp

startKoin<MyApp>()  // auto-discovers FeatureModule via hint functions
```

**How hints work:** During compilation, the plugin generates functions in
`org.koin.plugin.hints`. Downstream compilations query these to find available
`@Configuration` modules and inject them into `startKoin<T>()`.

**Labeled discovery:**
```kotlin
@Configuration("test")
class TestModule

@KoinApplication(configurations = ["test"])
object TestApp
```

**Recommendation:** For production code, prefer explicit modules — auto-discovery
across separate Gradle modules is less reliable:
```kotlin
@KoinApplication(modules = [AppModule::class, FeatureModule::class])
object MyApp
```

---

## KMP-Specific Behavior

### Expect/Actual Classes
- `expect` classes: `.module` is **not** generated (no body)
- `actual` classes: `.module` **is** generated

```kotlin
// commonMain
@Module
expect class PlatformModule  // NO .module here

// jvmMain
@Module
actual class PlatformModule {
    @Singleton
    fun providePlatform(): Platform = JvmPlatform()
}  // .module generated here
```

### FIR Phases per Source Set
The compiler runs FIR separately for each source set. The plugin handles each
independently:
- commonMain → processes common `@Module` classes
- jvmMain / androidMain / iosMain → processes platform-specific `@Module` classes

Each source set's modules are generated independently and linked at runtime.

### K/Native (iOS, watchOS, tvOS, Linux, Windows)
The plugin skips synthetic file generation on K/Native targets to avoid ObjC
export crashes (`NotImplementedError` in source file lookup). Cross-module
discovery still works via JVM/common compilation. No action needed on your side.

---

## startKoin Transformation

```kotlin
// Input
@KoinApplication(modules = [AppModule::class, DataModule::class])
object MyApp

fun main() {
    startKoin<MyApp> { printLogger() }
}

// Output (after plugin)
fun main() {
    startKoinWith(listOf(AppModule().module, DataModule().module)) {
        printLogger()
    }
}
```

`koinApplication<MyApp>()` and `koinConfiguration<MyApp>()` are transformed the same way.

---

## Property Injection

```kotlin
startKoin {
    properties(mapOf("server.url" to "https://api.example.com", "timeout" to "30"))
    modules(AppModule().module)
}

@Singleton
class Config(
    @Property("server.url") val url: String,
    @Property("timeout") @PropertyValue("5000") val timeout: Int  // default if missing
)
```

---

## DSL Safety Checks

When `dslSafetyChecks = true` (default), `create(::T)` must be the **only** statement
in its lambda:

```kotlin
// OK
scoped { create(::MyService) }

// ERROR — extra statement not allowed
scoped {
    println("debug")
    create(::MyService)
}
```

Disable during migration:
```kotlin
koinCompiler { dslSafetyChecks = false }
```

---

## Gradle Configuration

```kotlin
// build.gradle.kts
plugins {
    id("io.insert-koin.compiler.plugin")
}

koinCompiler {
    userLogs = true    // log which components are detected
    debugLogs = true   // verbose internal processing
    dslSafetyChecks = true
}
```

---

## Limitations & Gotchas

| Issue | Details |
|-------|---------|
| Kotlin version lock | Plugin compiled for a specific Kotlin minor version; mismatches cause errors |
| Cross-Gradle-module discovery | `@Configuration` hints work within same compilation; across separate Gradle modules it can miss |
| No compile-time dependency validation | Missing deps crash at runtime, not compile time (on roadmap) |
| `singleOf(::T)` not supported | Use `single<T>()` instead |
| `create(::T)` scope only | Constructor references only work inside `Scope.create(::T)` calls |
| Dynamic runtime modules | Not scanned; register them manually in `startKoin { modules(...) }` |

---

## Migration from KSP Checklist

- [ ] Kotlin 2.3.x+ (K2 required)
- [ ] Koin 4.2.0-RC1+
- [ ] Remove KSP plugin and `koin-ksp-compiler` dependency
- [ ] Add `io.insert-koin.compiler.plugin` Gradle plugin
- [ ] `singleOf(::T)` → `single<T>()`, `factoryOf(::T)` → `factory<T>()`
- [ ] `@KoinViewModel` import: `org.koin.core.annotation.KoinViewModel`
- [ ] Delete `build/generated/ksp/`
- [ ] Test all targets (JVM, JS, iOS, etc.)

---

## JSR-330 Support

Standard Java DI annotations work as aliases:

| JSR-330 | Equivalent |
|---------|-----------|
| `@jakarta.inject.Singleton` | `@Single` |
| `@jakarta.inject.Inject` | `@Factory` |
| `@jakarta.inject.Named("x")` | `@Named("x")` |
| `@javax.inject.*` | same (legacy namespace) |

---

## Roadmap Items Relevant to KMP Users

| Item | Status |
|------|--------|
| Compile-time dependency validation | Planned |
| `@Monitor` annotation (function interception) | Planned |
| Precompiled module index (faster startup) | Planned |
| JSR-330 optional toggle | In progress |

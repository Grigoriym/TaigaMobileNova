# Koin Annotations in Compose Multiplatform (KMP)

This guide covers everything you need to add Koin with annotations to a Compose Multiplatform project targeting Android and iOS.

The reference implementation is in the [`compose-annotations/`](../../compose-annotations/) folder.

---

## Project structure

```
myapp/
├── composeApp/          # UI module (Android + iOS)
│   └── src/
│       ├── androidMain/ # Android-specific code
│       ├── commonMain/  # Shared Compose UI, ViewModels, DI wiring
│       ├── iosMain/     # iOS-specific code
│       └── nativeMain/  # iOS implementations
└── data/                # Shared data module (optional, recommended)
    └── src/commonMain/  # Models, API, repository
```

---

## 1. Gradle setup

### Root `build.gradle.kts`

Apply plugins without activation at the root level:

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application)  apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.compose)       apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.koin)                 apply false  // Koin compiler plugin
}
```

### `gradle/libs.versions.toml`

```toml
[versions]
kotlin      = "2.3.20"
koin        = "4.2.0"
koin-plugin = "0.3.0"
agp         = "8.13.0"
compose-multiplatform = "1.9.3"

[libraries]
koin-core              = { module = "io.insert-koin:koin-core",              version.ref = "koin" }
koin-annotations       = { module = "io.insert-koin:koin-annotations",       version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform",          version.ref = "kotlin" }
kotlin-compose       = { id = "org.jetbrains.kotlin.plugin.compose",         version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization",   version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose",                      version.ref = "compose-multiplatform" }
android-application  = { id = "com.android.application",                     version.ref = "agp" }
android-library      = { id = "com.android.library",                         version.ref = "agp" }
koin                 = { id = "io.insert-koin.compiler.plugin",              version.ref = "koin-plugin" }
```

### `composeApp/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.koin)                  // enables KSP annotation processing
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(project(":data"))          // your data module

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.runtime.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)  // koinViewModel()
            implementation(libs.koin.annotations)        // @Single, @KoinViewModel, etc.
        }
    }
}

// Configure the Koin compiler plugin
koinCompiler {
    userLogs = true          // print component detection logs at startup
    debugLogs = false        // verbose FIR/IR phase logs (for plugin debugging)
    dslSafetyChecks = true   // validate DSL lambda usage at compile time (default: true)
}
```

### `data/build.gradle.kts` (shared data module)

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.koin)                  // needed to process annotations in this module too
}

kotlin {
    // ... targets setup ...

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            api(libs.koin.annotations)        // api() so consuming modules see the annotations
        }
    }
}

koinCompiler {
    userLogs = true
    dslSafetyChecks = true
}
```

> **Why `api()` for `koin-annotations` in the data module?**
> Modules that scan sub-packages across Gradle boundaries need the annotations to be visible to the consuming module's KSP run.

> **KMP advantage**: The Koin compiler plugin handles all platforms automatically. No per-platform KSP configuration is needed — apply the plugin once and it works across Android, iOS, Desktop, and Web targets.

---

## 2. DI entry point — `@KoinApplication`

Declare a single `@KoinApplication` object in `commonMain`. This is the root configuration anchor that the Koin compiler plugin uses to generate a `koinConfiguration()` extension.

```kotlin
// commonMain/di/Koin.kt

// AppModule includes sub-modules that are not @Configuration themselves
@Module(includes = [ViewModelModule::class, NativeComponentModule::class, PlatformComponentModule::class])
@Configuration
class AppModule

// DataModule is a separate @Configuration — auto-discovered by @KoinApplication
// (alternative: add it to AppModule.includes and drop @Configuration from DataModule)

@KoinApplication
object KoinApp
```

| Annotation | Purpose |
|---|---|
| `@KoinApplication` | Marks the Koin entry point; generates `startKoin<T>()`, `koinApplication<T>()`, and `koinConfiguration<T>()` |
| `@Module` | Declares a Koin module |
| `@Configuration` | Marks a module as part of the **default** configuration — auto-loaded by `@KoinApplication`. Modules without this must be explicitly listed in `includes = [...]`. Supports named configs: `@Configuration("prod")` for environment-specific loading. |
| `includes = [...]` | Composes child modules that don't carry `@Configuration` themselves |

### Understanding `@Configuration`

`@Configuration` is the mechanism that lets `@KoinApplication` know which `@Module` classes to load **automatically**. Without it, a module is invisible to the auto-discovery scan and must be explicitly wired in.

#### The two ways a module gets loaded

**Path 1 — auto-loaded via `@Configuration`**

```kotlin
@Module
@ComponentScan("com.example.data")
@Configuration                        // ← discovered automatically by @KoinApplication
class DataModule
```

At startup, `@KoinApplication` scans the classpath for every `@Module` that carries `@Configuration` and includes them in the generated configuration. You never have to mention `DataModule` by name anywhere else.

**Path 2 — explicit inclusion via `includes`**

```kotlin
@Module
@ComponentScan                        // no @Configuration
class ViewModelModule                 // invisible to @KoinApplication on its own

@Module
@Configuration                        // this one IS auto-loaded
class AppModule(
    includes = [ViewModelModule::class]  // so it pulls in ViewModelModule manually
)
```

`ViewModelModule` becomes active only because `AppModule` (which is auto-loaded) declares it in `includes`. Use this pattern when you want one root module to own and compose its children explicitly.

#### Named configurations

The unlabelled `@Configuration` is shorthand for `@Configuration("")` — the **default** configuration, always loaded.

You can declare environment-specific variants:

```kotlin
@Module
@Configuration("prod")
class ProdModule          // only loaded when "prod" config is requested

@Module
@Configuration("dev")
class DevModule           // only loaded when "dev" config is requested

@Module
@Configuration            // always loaded
class CommonModule
```

Then at startup, select which named config to activate:

```kotlin
// Approach A (Compose)
KoinApplication(configuration = koinConfiguration<KoinApp>("prod")) { ... }

// Approach B (Application class)
startKoin<KoinApp>("prod") { ... }
```

Only `CommonModule` + `ProdModule` would be loaded; `DevModule` is ignored.

> **Practical use**: swap `NetworkModule` implementations between debug and release builds, or load a `MockModule` in tests without touching production code.

#### `@Configuration` vs `includes` — decision guide

| You want... | Solution |
|---|---|
| Module loaded automatically at startup | Add `@Configuration` to it |
| Module only active under a specific environment | `@Configuration("name")` + select it at startup |
| Module that's a sub-component of another | No `@Configuration`; add it to the parent's `includes` |
| A module to be reusable across multiple roots | No `@Configuration`; include it wherever needed |

#### Common mistake

Forgetting `@Configuration` on a module that should be auto-loaded:

```kotlin
@Module
@ComponentScan("com.example.data")
// missing @Configuration — DataModule will never be loaded!
class DataModule
```

Koin won't throw an error. It silently skips the module. If your dependencies are mysteriously unresolved at runtime, check that the module carrying them has `@Configuration`.

---

## 3. Module organisation with `@ComponentScan`

Instead of manually listing every binding, use `@ComponentScan` to auto-discover annotated classes within a package.

### Data module

```kotlin
// data/src/commonMain/di/DataModule.kt

@Module
@Configuration                         // auto-discovered by @KoinApplication (default config)
@ComponentScan("com.example.data")     // scans this package for @Single, etc.
class DataModule {

    // Manual binding when you need custom construction (e.g. HttpClient)
    @Single
    fun provideHttpClient(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }, contentType = ContentType.Any)
        }
    }
}
```

Classes in `com.example.data` only need a scope annotation:

```kotlin
@Single
class MyRepository(private val api: MyApi) : Repository

@Single
class MyApi(private val client: HttpClient) : Api
```

### ViewModel module

```kotlin
// commonMain/screens/ViewModelModule.kt

@Module
@ComponentScan    // no argument = scans the current package and sub-packages
class ViewModelModule
```

ViewModels in `com.example.screens.*` just add `@KoinViewModel`:

```kotlin
@KoinViewModel
class ListViewModel(private val repository: MyRepository) : ViewModel() { ... }

@KoinViewModel
class DetailViewModel(private val repository: MyRepository) : ViewModel() { ... }
```

### Understanding `@ComponentScan`

`@ComponentScan` tells KSP: *"look inside this package tree and automatically register every class annotated with `@Single`, `@Factory`, `@KoinViewModel`, etc. into this module."*

Without it, annotated classes are invisible to the module — you would have to declare every binding manually as a provider function inside the `@Module` class body.

#### The three forms

**`@ComponentScan("com.example.pkg")` — explicit path**

Scans the specified package and all its sub-packages, regardless of where the module class itself lives.

```
data/
└── src/commonMain/kotlin/
    ├── com/example/data/          ← classes live here (@Single MuseumApi, etc.)
    │   ├── MuseumApi.kt
    │   └── MuseumStorage.kt
    └── com/example/data/di/       ← module class lives here
        └── DataModule.kt
```

```kotlin
// DataModule is in com.example.data.di
// but the classes to discover are in com.example.data
@Module
@ComponentScan("com.example.data")   // explicit path — bridges the gap
class DataModule
```

If you used `@ComponentScan` with no argument here, KSP would only scan `com.example.data.di` — it would miss all the classes sitting one level up.

**`@ComponentScan` (empty) — scan the module's own package**

Scans the package the module class itself lives in, plus all sub-packages.

```
composeApp/
└── src/commonMain/kotlin/
    └── com/example/screens/        ← ViewModelModule lives here
        ├── ViewModelModule.kt
        ├── list/
        │   └── ListViewModel.kt    ← discovered automatically
        └── detail/
            └── DetailViewModel.kt  ← discovered automatically
```

```kotlin
// ViewModelModule is in com.example.screens
// ViewModels are in com.example.screens.list and com.example.screens.detail
@Module
@ComponentScan    // scans com.example.screens.** — finds all @KoinViewModel classes
class ViewModelModule
```

This is the most common form. Structure your code so the module class sits at the root of the package tree it is responsible for.

**No `@ComponentScan` — manual-only module**

Use when the module doesn't discover classes; instead it provides bindings via explicit `@Single fun provide...()` functions, or it only composes child modules via `includes`.

```kotlin
// Only wires up sub-modules, no class scanning needed
@Module(includes = [ViewModelModule::class, PlatformComponentModule::class])
@Configuration
class AppModule

// Only provides a manually constructed dependency
@Module
@Configuration
class NetworkModule {
    @Single
    fun provideHttpClient(): HttpClient = HttpClient { ... }
}
```

#### Cross-Gradle-module scanning

The explicit path form is especially important in multi-module Gradle projects. A module class in `:composeApp` can scan classes defined in `:data` by specifying their package:

```kotlin
// In :composeApp, but scanning :data's package
@Module
@ComponentScan("com.example.data")
class DataModule
```

> For this to work, the `:data` module must expose `koin-annotations` with `api()` (not `implementation()`), so that KSP in `:composeApp` can see the annotations at compile time. See the Gradle setup section.

#### Decision table

| Situation | Form |
|---|---|
| Module class and its classes share the same package root | `@ComponentScan` (empty) |
| Module class is in a different package than the classes it discovers | `@ComponentScan("com.example.pkg")` |
| Scanning classes across a Gradle module boundary | `@ComponentScan("com.example.pkg")` (explicit) |
| Module only provides manual bindings or composes sub-modules | No `@ComponentScan` |

#### What `@ComponentScan` does NOT do

- It does not scan classes that **lack a scope annotation**. A plain `class Foo` is ignored; only `@Single class Foo`, `@Factory class Foo`, `@KoinViewModel class Foo`, etc. are picked up.
- It does not recursively scan across Gradle modules unless the annotations are on the compile classpath (via `api()`).
- It does not validate whether discovered classes can actually be constructed — that is still a runtime check.

---

## 4. Platform-specific modules with `expect`/`actual`

Use an `expect` module class when different platforms provide different implementations.

### Common declaration

```kotlin
// commonMain/di/Koin.kt

@Module
expect class PlatformComponentModule
```

### Android actual

```kotlin
// androidMain/di/Koin.android.kt

@Module
@ComponentScan          // discovers AndroidPlatformComponent in the same package
actual class PlatformComponentModule

@Singleton
class AndroidPlatformComponent : PlatformComponent {
    override fun getInfo() = "Android"
}
```

### iOS actual

```kotlin
// iosMain/di/Koin.ios.kt

@Module
@ComponentScan
actual class PlatformComponentModule

@Singleton
class IOSPlatformComponent : PlatformComponent {
    override fun getInfo() = "iOS"
}
```

This pattern also works for platform-specific wrappers (e.g. Android `Context`):

```kotlin
// commonMain
interface ContextWrapper { fun getContextInfo(): String }

// androidMain
@Singleton
class AndroidContextWrapper(val context: Context) : ContextWrapper {
    override fun getContextInfo() = "Android Context: $context"
}

// nativeMain (iOS)
@Singleton
class IOSContextWrapper : ContextWrapper {
    override fun getContextInfo() = "iOS Context"
}
```

Koin injects `android.content.Context` automatically on Android when you start Koin with `androidContext(this)`.

> **Platform contracts with different constructors**: When platform implementations need *different constructor arguments* (not just different implementations), use the interface + expect/actual module pattern instead of expect/actual classes with `@ComponentScan`. This preserves compile-time safety without dynamic scope access.

---

## 5. Starting Koin — two approaches

The plugin generates three functions from your `@KoinApplication` object, each suited to a different startup strategy:

```kotlin
startKoin<KoinApp> { }         // for Application.onCreate() (Approach B)
koinApplication<KoinApp> { }   // returns a KoinApplication instance (useful in tests)
koinConfiguration<KoinApp>()   // for the KoinApplication composable (Approach A)
```

---

### Approach A — `KoinApplication` composable (pure Compose, no Application class needed)

Start Koin inside the root `@Composable`. Koin initialises when the first composable renders.

```kotlin
// commonMain/App.kt

@Composable
fun App() {
    KoinApplication(configuration = koinConfiguration<KoinApp>()) {
        MaterialTheme {
            Surface {
                AppNavHost()
            }
        }
    }
}
```

```kotlin
// androidMain/MainActivity.kt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

**Limitation**: Koin starts *after* `Application.onCreate()` has already run, so you cannot access any dependency from the `Application` class. Android `Context` is also not provided to the graph unless you add it manually.

### iOS — no extra setup (both approaches)

```kotlin
// iosMain/MainViewController.kt

fun MainViewController() = ComposeUIViewController { App() }
```

iOS needs no additional Koin startup code regardless of which approach you use.

---

### Approach B — `startKoin` in `Application` (required for Application-level access)

Use this when you need to:
- Access Koin dependencies from the `Application` class itself
- Inject Android `Context` into any dependency
- Use Koin from Services, WorkManager workers, or Broadcast Receivers

**1. Add `koin-android`** (provides `androidContext()` and `KoinAndroidContext`):

```toml
# gradle/libs.versions.toml
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
```

```kotlin
// composeApp/build.gradle.kts
androidMain.dependencies {
    implementation(libs.koin.android)
}
```

**2. Start Koin in `Application.onCreate()`** using the generated `startKoin<KoinApp>`:

```kotlin
// androidMain/MyApp.kt
import org.koin.plugin.module.dsl.startKoin  // generated by @KoinApplication
import org.koin.android.ext.koin.androidContext

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<KoinApp> {
            androidContext(this@MyApp)   // makes Context injectable throughout the graph
            printLogger()                // optional
        }
    }
}
```

**3. Access dependencies from the `Application` class** — two options:

Via `KoinComponent` interface (idiomatic):
```kotlin
class MyApp : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        startKoin<KoinApp> { androidContext(this@MyApp) }

        val repo: MuseumRepository by inject()
        repo.initialize()
    }
}
```

Via direct pull from the Koin instance (no interface needed):
```kotlin
override fun onCreate() {
    super.onCreate()
    startKoin<KoinApp> { androidContext(this@MyApp) }

    val repo = KoinPlatform.getKoin().get<MuseumRepository>()
}
```

**4. Update `App.kt`** — Koin is already running, so replace `KoinApplication` with `KoinContext` to connect the Compose tree to the existing instance without starting a second one:

```kotlin
// commonMain/App.kt
import org.koin.compose.KoinContext   // cross-platform, binds to the already-running instance

@Composable
fun App() {
    KoinContext {
        MaterialTheme {
            Surface {
                AppNavHost()
            }
        }
    }
}
```

> On Android only you can alternatively use `KoinAndroidContext` from `koin-androidx-compose`, which adds Android lifecycle awareness.

---

### Decision guide

| Scenario | Approach |
|---|---|
| Pure Compose app, no background work | **A** — `KoinApplication { koinConfiguration<KoinApp>() }` |
| Need deps in `Application`, Services, or WorkManager | **B** — `startKoin<KoinApp>` + `KoinContext {}` |
| Any dependency needs Android `Context` | **B** (requires `androidContext(this)`) |

---

## 6. Injecting ViewModels in Composables

Use `koinViewModel()` from `koin-compose-viewmodel`:

```kotlin
@Composable
fun ListScreen(onItemClick: (Int) -> Unit) {
    val viewModel = koinViewModel<ListViewModel>()
    val items by viewModel.items.collectAsStateWithLifecycle()
    // ...
}
```

No factory registration needed — `@KoinViewModel` on the class is sufficient.

---

## 7. Annotation reference

### Definition annotations

| Annotation | Generated DSL | Notes |
|---|---|---|
| `@Single` | `single { }` | One shared instance (Koin singleton) |
| `@Singleton` | `single { }` | Koin alias for `@Single` (`org.koin.core.annotation.Singleton`); also accepts `jakarta.inject.Singleton` for JSR-330 compat |
| `@Factory` | `factory { }` | New instance on every resolution |
| `@Scoped` | `scoped { }` | Lives within a declared scope (pair with `@Scope`) |
| `@KoinViewModel` | `viewModel { }` | Multiplatform ViewModel; use with `koinViewModel()` in Compose |
| `@KoinWorker` | `worker { }` | Android WorkManager `Worker` instances |

### Explicit binding with `binds`

By default, Koin inspects a class's interface list and registers it under every interface it implements — this is **implicit binding**. The `binds` parameter lets you **override** that list and choose exactly which types the container exposes.

#### Implicit binding (no `binds` needed)

```kotlin
@Single
class KtorMuseumApi(private val client: HttpClient) : MuseumApi
```

Generated equivalent:
```kotlin
single<MuseumApi> { KtorMuseumApi(get()) }
```

Works perfectly when a class has exactly one interface and you always want to inject by that interface.

#### When to use `binds` explicitly

**Bind to a subset of interfaces**

The class implements three interfaces but you only want two of them injectable:

```kotlin
interface Repository
interface Cacheable
interface Auditable   // internal — should not be resolvable via DI

@Single(binds = [Repository::class, Cacheable::class])
class UserRepo : Repository, Cacheable, Auditable
```

**Make both the interface and the concrete type injectable**

```kotlin
@Single(binds = [MuseumApi::class, KtorMuseumApi::class])
class KtorMuseumApi(...) : MuseumApi
```

Now both `get<MuseumApi>()` and `get<KtorMuseumApi>()` return the same singleton.

**Multiple implementations of the same interface**

Without `binds`, two classes implementing the same interface would create a conflict. Combine `binds` with `@Named` to disambiguate:

```kotlin
@Single(binds = [Cache::class])
@Named("memory")
class InMemoryCache : Cache

@Single(binds = [Cache::class])
@Named("disk")
class DiskCache : Cache
```

**Bind to a superclass**

```kotlin
@KoinViewModel(binds = [BaseViewModel::class])
class ListViewModel : BaseViewModel()
```

#### Decision table

| Situation | Use |
|---|---|
| One class, one interface | Implicit — omit `binds` |
| One class, many interfaces, bind to all | Implicit — omit `binds` |
| One class, many interfaces, bind to a subset | `binds = [A::class, B::class]` |
| Multiple classes, same interface | `binds = [I::class]` + `@Named(...)` on each |
| Want the concrete type injectable too | `binds = [Interface::class, Concrete::class]` |

---

### Qualifier & parameter annotations

| Annotation | Purpose |
|---|---|
| `@Named("id")` | Distinguishes multiple bindings of the same type by a string key |
| `@Qualifier` | Alternative qualifier (reversed parameter priority vs `@Named`) |
| `@InjectedParam` | Constructor parameter injected at call site via `parametersOf(...)` |
| `@Property("key")` | Resolves value from Koin properties (config/environment) |
| `@PropertyValue("key")` | Declares a default value for a `@Property` key (since v1.4) |

### Scope annotations

| Annotation | Scope |
|---|---|
| `@Scope` / `@Scope(name = "name")` | Declares a custom scope container |
| `@ViewModelScope` | ViewModel scope (works on all KMP targets) |
| `@ActivityScope` | Android Activity scope |
| `@ActivityRetainedScope` | Survives configuration changes |
| `@FragmentScope` | Android Fragment scope |
| `@ScopeId("id")` | Resolves a dependency from a specific scope instance by ID |

### Module & app annotations

| Annotation | Purpose |
|---|---|
| `@Module` | Declares a module class |
| `@ComponentScan` / `@ComponentScan("pkg")` | Auto-discovers annotated classes in a package (traverses across Gradle modules) |
| `@Configuration` / `@Configuration("name")` | Marks module as a named configuration; unlabelled = default config, auto-loaded by `@KoinApplication` |
| `@KoinApplication` | Entry point; generates `startKoin<T>()`, `koinApplication<T>()`, `koinConfiguration<T>()` |
| `@Monitor` | Enables automatic performance tracking via Kotzilla Platform |

---

## 8. Complete wiring summary

```
@KoinApplication (KoinApp)
    ├── @Module @Configuration (AppModule)          ← auto-loaded (default config)
    │       ├── includes ViewModelModule   → @ComponentScan (screens package)
    │       │       ├── @KoinViewModel ListViewModel
    │       │       └── @KoinViewModel DetailViewModel
    │       ├── includes NativeComponentModule → @ComponentScan (native package)
    │       └── includes PlatformComponentModule (expect/actual per platform)
    │               ├── Android: @Singleton AndroidPlatformComponent
    │               └── iOS:     @Singleton IOSPlatformComponent
    │
    └── @Module @Configuration (DataModule)         ← auto-loaded (default config)
            @ComponentScan("com.example.data")
            ├── @Single HttpClient           (manual factory in DataModule class)
            ├── @Single MyApi
            ├── @Single MyStorage
            └── @Single MyRepository
```

**Compose startup:**
```
App() { KoinApplication(koinConfiguration<KoinApp>()) { ... } }
```

**ViewModel injection:**
```
koinViewModel<ListViewModel>()   // inside any @Composable
```

---

## Compile-time safety: what annotations do and don't guarantee

### What KSP actually does at build time

Koin annotations are processed by KSP during compilation. KSP reads your `@Single`, `@Module`, `@KoinViewModel`, etc. and **generates Koin DSL code** — it does not validate the dependency graph, it just writes the wiring for you.

```kotlin
// You write:
@Single
class MuseumRepository(private val api: MuseumApi, private val storage: MuseumStorage)

// KSP generates (roughly):
val museumRepositoryModule = module {
    single { MuseumRepository(get(), get()) }
}
```

That generated code is what actually runs. Koin's container resolves each `get()` call at **runtime** when something first asks for the dependency.

### What you DO get from annotations

| Benefit | How |
|---|---|
| No manual DSL to maintain | KSP writes `single { }`, `factory { }`, `viewModel { }` for you |
| Refactoring safe | Rename a class or add a constructor parameter → generated module updates automatically on next build |
| `dslSafetyChecks = true` | The compiler plugin validates DSL lambda patterns and flags obvious misuse at compile time |
| KSP errors for annotation misuse | Applying `@Single` directly to an abstract class or interface causes a compile error |

### What you do NOT get

**Missing bindings are still runtime failures.** If you inject `MuseumApi` but forget to annotate `KtorMuseumApi` with `@Single`, you get:

```
NoBeanDefFoundException: No definition found for type 'MuseumApi'
```

...at runtime, not at build time. Koin won't refuse to compile; it simply won't know how to satisfy the dependency when it's first requested.

This is the core difference from **Dagger/Hilt**, which validates the entire dependency graph during annotation processing and refuses to compile if any binding is missing.

### How to catch missing bindings before shipping: `verify()`

Koin provides a `checkModules()` / `verify()` API you run in a unit test. This moves graph validation from runtime (crash in production) to test time (caught in CI):

```kotlin
class KoinModuleTest {
    @Test
    fun verifyKoinModules() {
        koinApplication {
            modules(appModule, dataModule, viewModelModule)
        }.checkModules()
    }
}
```

If any `get()` in the generated DSL cannot be resolved — wrong type, missing binding, unregistered qualifier — the test fails with a clear error message pointing to the unresolved dependency.

### Koin vs Dagger: the tradeoff

```
Dagger / Hilt
  ├── Full compile-time graph validation
  ├── Build fails immediately on missing binding
  └── Cost: steep learning curve, complex setup, slow incremental builds

Koin (classic DSL or annotations)
  ├── Runtime graph resolution
  ├── Missing binding = crash at first use (or test failure with verify())
  └── Cost: none — simple API, fast builds, same behaviour with or without annotations

Koin annotations specifically
  ├── Same runtime model as classic DSL
  ├── Benefit: eliminates boilerplate, safer refactoring
  └── Not a safety upgrade — a productivity upgrade
```

Koin annotations are primarily a **developer experience** improvement (less code to write and maintain, safer refactoring). Use `checkModules()` in tests if you want graph completeness verified automatically before shipping.

---

## Key dependencies reference

| Dependency | What it provides |
|---|---|
| `koin-core` | Core DI container |
| `koin-annotations` | `@Single`, `@KoinViewModel`, `@KoinApplication`, `@Module`, etc. |
| `koin-compose-viewmodel` | `koinViewModel()`, `KoinApplication` composable |
| Koin compiler plugin (`io.insert-koin.compiler.plugin`) | KSP-based code generation; required for annotations to work |
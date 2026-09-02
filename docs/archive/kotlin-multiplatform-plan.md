# Kotlin Multiplatform Migration Plan

> **ARCHIVED — historical only.** This migration is complete: the app ships on Android, iOS and
> Desktop, Hilt→Koin and Retrofit→Ktor are done, and the UI is Compose Multiplatform. The unticked
> checkboxes below describe work that has since shipped, and the "Current State" stack list is from
> before the migration. Kept for the reasoning and the module classification, not as a plan.
> For the current architecture see `CLAUDE.md`.

## Some suggestions

- Don't try to move files, changing imports, and all that stuff that is spread across the project, it is better to do it manually, so just tell what needs to be done

## Found issues

- By moving core:domain module into KMP somehow if I receive an API error the app is not handling the Exception, though I catch it, e.g. in WorkItemRemoteMediator or the place where we call teh API IssuesRepositoryImpl, the error in logs always lead to ErrorMappingInterceptor where we throw NetworkException(errorCode = networkErrorCode, request = requestUrl, taigaError = taigaError)

## Decisions

- **Targets**: Android (priority), iOS, Desktop (JVM) — all three
- **Sharing scope**: All-in — share everything including UI via Compose Multiplatform
- **DI**: Koin (replacing Hilt)
- **Approach**: Incremental, step-by-step. Android must keep working at all times.
- **Date/Time**: Use `kotlinx-datetime` (replacing `java.time`) for multiplatform date/time handling
- **Platform issues**: Handle per-platform issues once setup is ready, not upfront
- **Team**: Solo developer, no iOS team

## Current State

**Stack**: Kotlin 2.3.10, Jetpack Compose, Hilt 2.59, Retrofit 3.0.0, OkHttp 5.3.2, Room 2.8.4, DataStore 1.2.0, Navigation Compose 2.9.7, Coil 3.3.0, Paging 3.4.1, Timber 5.0.1

**Module count**: ~60 Gradle modules across `app/`, `core/`, `feature/`, `utils/`, `uikit/`, `strings/`, `testing/`

**Architecture**: MVVM + Clean Architecture with domain (pure Kotlin), data (Android library + Retrofit/Hilt), UI (Compose + Hilt) layers per feature.

## Library Migration Map

### Already KMP-compatible (no migration needed)

| Library | Version | Notes |
|---------|---------|-------|
| kotlinx-serialization-json | 1.10.0 | Works in commonMain |
| kotlinx-coroutines | 1.10.2 | Works in commonMain |
| kotlinx-collections-immutable | 0.4.0 | Works in commonMain |
| Ktor | 3.4.0 | Already in project (tools/seed) |
| Room | 2.8.4 | KMP support since 2.7.0 |
| DataStore | 1.2.0 | KMP support since 1.1.0 |
| Paging (paging-common) | 3.4.1 | KMP since 3.3.0 |
| Coil | 3.3.0 | KMP (swap coil-network-okhttp → coil-network-ktor3) |
| multiplatform-markdown-renderer | 0.39.2 | Already multiplatform |
| Turbine | 1.2.1 | KMP |

### Need replacement

| Current | Replacement | When |
|---------|-------------|------|
| Retrofit 3.0.0 + OkHttp 5.3.2 | Ktor Client 3.4.0 (engine per platform: okhttp/Android, darwin/iOS, cio/Desktop) | Phase 2 |
| Hilt/Dagger 2.59 | Koin 4.1.1 + koin-annotations 2.3.1 (annotation-based, same as MealieMobile) | Phase 2 |
| Timber 5.0.1 | Kermit (Touchlab) | Phase 1 |
| MockK 1.14.9 | Keep for Android tests; use kotlin-test + Turbine for commonTest | Phase 1 |
| Jetpack Navigation Compose | JetBrains KMP Navigation Compose (`org.jetbrains.androidx.navigation:navigation-compose`) | Phase 3 |
| Android string resources | Compose Multiplatform resources (`compose.resources` Gradle plugin, `Res.string`) | Phase 3 |

### Keep as-is (Android-only debug tooling)

- Chucker (debug HTTP inspector) — Android-only, behind build flavor
- Robolectric (Android-specific tests)

## Convention Plugins Plan

### New plugins to create

| Plugin | Purpose |
|--------|---------|
| `taigamobile.kmp.library` | KMP library with Android + iOS + Desktop targets |
| `taigamobile.kmp.library.compose` | KMP library + Compose Multiplatform |
| `taigamobile.android.koin` | Koin DI for Android modules (replaces `taigamobile.android.hilt`) |
| `taigamobile.kmp.koin` | Koin DI for KMP modules (replaces `taigamobile.kotlin.hilt`) |

### Source set layout change

Modules migrated to KMP move from:
```
src/main/kotlin/
```
to:
```
src/commonMain/kotlin/       # shared code (all platforms)
src/androidMain/kotlin/      # Android-specific expect/actual
src/iosMain/kotlin/          # iOS-specific expect/actual
src/desktopMain/kotlin/      # Desktop-specific expect/actual
```

## Module Classification

### Tier 1 — Pure Kotlin, trivial to convert (swap `kotlin.library` → `kmp.library`)

These modules have **zero Android imports** and use `taigamobile.kotlin.library`:

- `core/domain`
- `core/async`
- `core/appinfo-api`
- `core/serialization`
- `utils/formatter/decimal`
- `utils/formatter/datetime`
- All `feature/*/domain/` modules (~12 modules)

### Tier 2 — DTOs and mappers (need Android import audit)

Currently use `taigamobile.android.library` but likely contain only kotlinx-serialization data classes:

- `feature/epics/dto`, `feature/epics/mapper`
- `feature/issues/dto`, `feature/issues/mapper`
- `feature/userstories/dto`, `feature/userstories/mapper`
- `feature/tasks/mapper`
- `feature/projects/dto`, `feature/projects/mapper`
- `feature/users/dto`, `feature/users/mapper`
- `feature/filters/dto`, `feature/filters/mapper`
- `feature/workitem/dto`, `feature/workitem/mapper`

**Action**: Audit for Android imports. If clean, convert to `kmp.library`.

### Tier 3 — Data modules (Retrofit → Ktor, Hilt → Koin)

These require actual library replacement work:

- `core/api` — Retrofit interfaces, OkHttp interceptors, auth logic
- `core/storage` — Room database, DataStore, DAOs
- All `feature/*/data/` modules (~12 modules)

### Tier 4 — UI modules (Compose Multiplatform)

- `uikit`
- `utils/ui`
- `strings`
- `core/navigation`
- All `feature/*/ui/` modules

### Stays platform-specific

- `app/` — Android entry point (will create iOS and Desktop entry points separately)
- `core/async-android` — merges into `core/async` as `androidMain` source set
- `testing/` — stays JVM (can be extended for commonTest later)
- `tools/seed/` — developer tooling, no need to migrate

---

## Migration Phases

### Phase 0: Preparation

**Goal**: Set up KMP infrastructure without changing any existing modules.

- [x] Create `taigamobile.kmp.library` convention plugin (Android + iOS + Desktop targets)
  - `KmpLibraryConventionPlugin.kt` — applies `kotlin.multiplatform` + `com.android.library`, configures compileSdk/minSdk/flavors
  - `KmpConfiguration.kt` — declares targets (androidTarget, iosArm64/iosSimulatorArm64, desktop JVM), sets JVM 21, adds coroutines + collections to commonMain (iosX64 removed — deprecated by JetBrains)
- [x] Create `taigamobile.kmp.library.compose` convention plugin
  - `KmpLibraryComposeConventionPlugin.kt` — applies `taigamobile.kmp.library` + Compose Multiplatform setup
  - `KmpCompose.kt` — applies `org.jetbrains.compose` + `kotlin.plugin.compose`, adds material3, compose resources, ui tooling preview to commonMain
- [x] Add `kotlin("multiplatform")` plugin dependency in `build-logic/convention/build.gradle.kts`
- [x] Add KMP entries to `libs.versions.toml`: `kotlin-multiplatform` plugin, `taigamobile-kmp-library` plugin
- [x] Add remaining KMP entries to `libs.versions.toml`:
  - Ktor engines: `ktor-client-okhttp` (Android), `ktor-client-darwin` (iOS)
  - Koin 4.1.1: `koin-bom`, `koin-core`, `koin-android`, `koin-compose`, `koin-compose-viewmodel`, `koin-annotations` 2.3.1, `koin-ksp-compiler` 2.3.1
  - Kermit 2.0.8 (Touchlab)
  - Compose Multiplatform 1.10.1 plugin (`org.jetbrains.compose`) + gradle plugin for build-logic
  - JetBrains Navigation Compose KMP 2.9.2 (`org.jetbrains.androidx.navigation:navigation-compose`)
- [ ] Set up CI to build all targets (deferred to Phase 1 — no KMP modules exist yet)
- [x] **Verify**: convention plugins compile, existing Android build unaffected

### Phase 1: Domain / DTO / Mapper modules → KMP

**Goal**: Convert all pure-logic modules to KMP. Android app continues to work identically.

- [x] Convert first batch of pure-logic modules to KMP (14 modules):
  - **Core (3):** `core/appinfo-api`, `core/domain`, `core/serialization` (partial — `ImmutableListSerializer` in commonMain, date serializers in androidMain+desktopMain)
  - **Feature domain (5):** `feature/login/domain`, `feature/swimlanes/domain`, `feature/users/domain`, `feature/filters/domain`, `feature/projects/domain`
  - **Feature DTOs (6):** `feature/epics/dto`, `feature/filters/dto`, `feature/issues/dto`, `feature/projects/dto`, `feature/users/dto`, `feature/userstories/dto`
  - Source change: `NetworkException` in `core/domain` changed from `IOException` → `Exception` (no code caught `IOException`)
  - Removed deprecated `iosX64()` target from KMP config (deprecated by JetBrains, removed from paging-common)
  - For KMP modules using serialization: apply `kotlin.serialization` plugin directly + explicit `kotlinx.serialization.json` dep in `commonMain.dependencies` (convention `taigamobile.kotlin.serialization` uses JVM-style `dependencies {}` which bypasses KMP source sets)
- [x] **Verified**: Android app builds, all unit tests pass, iOS (iosArm64) + Desktop targets compile
- [x] Convert `java.time` → `kotlinx-datetime` batch (4 modules to KMP + cascading consumer updates):
  - **Converted to KMP (4):** `feature/workitem/domain`, `feature/workitem/dto`, `feature/sprint/domain`, `feature/history/domain`
  - **Updated to kotlinx-datetime types (stays JVM):** `utils/formatter/datetime` — uses `toJavaLocalDate()`/`toJavaLocalDateTime()` bridges for locale-aware formatting
  - **Cascading consumer updates (~50 files):** All modules referencing `LocalDate`/`LocalDateTime` from domain models updated imports to `kotlinx.datetime.*`; UI files using `java.time.format.DateTimeFormatter` use bridge functions (`toJavaLocalDate()`, `toJavaLocalDateTime()`)
  - **`core/serialization`:** androidMain serializers (`LocalDateSerializer`, `LocalDateTimeSerializer`) no longer referenced; commonMain versions (`CommonLocalDateSerializer`, `CommonLocalDateTimeSerializer`) used everywhere. Cleanup rename deferred.
- [ ] Convert remaining Tier 1 modules:
  - `core/async` + `core/async-android` — merge with `expect`/`actual` for `Dispatchers.Main` (has Hilt, Phase 2c)
  - `utils/formatter/decimal` — has `java.text` + Hilt (Phase 2)
  - `utils/formatter/datetime` — already uses kotlinx-datetime types, stays JVM due to Hilt + `java.time.format` (Phase 2c for DI)
  - Domain modules with `@Inject` use cases (dashboard, epics, issues, kanban, profile, tasks, userstories, wiki) — needs DI migration (Phase 2c)
- [ ] Audit and convert remaining Tier 2 modules (mappers) — blocked by `core/storage`, `utils/ui`
- [ ] Replace Timber with Kermit in all converted modules (none of the 14 converted modules use Timber)

### Phase 2: Data / Networking modules → KMP

**Goal**: Shared networking, storage, and DI.

#### 2a: Networking (Retrofit → Ktor)
- [ ] Create shared Ktor client setup in `core/api` commonMain
  - Platform engines: `ktor-client-okhttp` (Android), `ktor-client-darwin` (iOS), `ktor-client-cio` (Desktop)
  - Content negotiation with kotlinx-serialization
  - Auth interceptor → Ktor `HttpSend` plugin or custom auth plugin
  - Error mapping → Ktor response validation plugin
- [ ] Migrate feature API interfaces one by one (start with simplest, e.g., AuthApi)
- [ ] Keep old Retrofit code alongside Ktor during transition
- [ ] Remove Retrofit + OkHttp dependencies once all APIs migrated

#### 2b: Storage (Room KMP + DataStore KMP)
- [ ] Convert `core/storage` to KMP module
  - Room entities, DAOs → `commonMain`
  - Database builder → `androidMain` / `iosMain` / `desktopMain`
  - Migrations → verify they work with KMP Room compiler
  - DataStore preferences → `commonMain`, platform-specific file paths via `expect`/`actual`

#### 2c: DI (Hilt → Koin annotations)
- [ ] Add Koin dependencies to `libs.versions.toml` (koin 4.1.1, koin-annotations 2.3.1, koin-ksp — same versions as MealieMobile)
- [ ] Create `taigamobile.android.koin` convention plugin (KSP + koin-bom + koin-core/android/compose/viewmodel/annotations/ksp)
- [ ] Create `taigamobile.kmp.koin` convention plugin (KSP + koin-bom + koin-core/annotations/ksp)
- [ ] Migrate modules one by one:
  - Replace `taigamobile.android.hilt` → `taigamobile.android.koin` (or `kmp.koin` for shared modules)
  - Replace `@HiltViewModel` → `@KoinViewModel`
  - Replace `hiltViewModel()` → `koinViewModel()`
  - Replace `@Inject constructor(...)` → regular constructor (Koin resolves via KSP)
  - Replace `@Singleton @Binds` → `@Single` on implementation class
  - Replace Hilt `@Module @InstallIn(...)` → Koin `@Module` + `@ComponentScan`
  - Create `AppModule` aggregating all feature modules via `@Module(includes = [...])`
- [ ] Remove Hilt/Dagger dependencies entirely once all modules migrated

#### Verify
- All existing Android features work identically
- iOS + Desktop shared code compiles
- Existing tests pass

### Phase 3: Shared UI via Compose Multiplatform

**Goal**: Full app runs on Android, iOS, and Desktop.

#### 3a: Infrastructure
- [ ] Add Compose Multiplatform plugin (`org.jetbrains.compose`)
- [ ] Convert `strings/` → Compose Multiplatform resources (`Res.string` via `compose.resources`)
- [ ] Convert `core/navigation` to JetBrains KMP Navigation Compose
  - `org.jetbrains.androidx.navigation:navigation-compose`
  - Type-safe routes with kotlinx-serialization should transfer directly
- [ ] Convert `utils/ui` to KMP
  - `NativeText` → rethink for multiplatform resource access (use `Res.string` or `StringResource`)
  - `ObserveAsEvents` → should work with CMP Lifecycle
  - `SnackbarDelegate` → should work as-is (pure Kotlin + Compose)

#### 3b: UI Components
- [ ] Convert `uikit/` to Compose Multiplatform
  - Theme, reusable widgets → should mostly work (Material 3 is KMP)
  - `LocalOfflineState`, `LocalScreenReadySignal` → CompositionLocals work in CMP
  - File picker → `expect`/`actual` per platform
- [ ] Replace `coil-network-okhttp` with `coil-network-ktor3`

#### 3c: Feature UI modules (incremental)
- [ ] Convert feature UI modules one at a time, simplest first
  - Replace `@HiltViewModel` with Koin (already done in Phase 2c)
  - Replace `hiltViewModel()` with `koinViewModel()`
  - Move Composables to `commonMain`
  - Any Android-specific APIs → `expect`/`actual`

#### 3d: Platform entry points
- [ ] Create iOS entry point (Xcode project or CMP iOS application)
- [ ] Create Desktop entry point (JVM `main()` function with `Window { App() }`)
- [ ] Android `app/` module stays as-is but now depends on shared UI modules

#### Verify
- Android app works identically to before
- iOS app launches and basic navigation works
- Desktop app launches and basic navigation works
- Handle platform-specific issues as they surface

---

## Key Risks and Considerations

### Koin migration (annotation-based, same as MealieMobile)
Using Koin with annotations + KSP (not DSL). The pattern change from Hilt is:

```kotlin
// Hilt:
@Module
@InstallIn(SingletonComponent::class)
interface LoginModule {
    @Singleton @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

// Koin annotations:
@Module
class LoginDataModule {
    // auto-scanned via @ComponentScan, or use @Single/@Factory on classes directly
}

@Single
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    ...
) : AuthRepository
```

- `@HiltViewModel` → `@KoinViewModel` + `koinViewModel()` in Composables
- `@Inject constructor(...)` → just regular constructor (Koin resolves via KSP-generated code)
- `@Module @InstallIn(SingletonComponent::class)` → `@Module` + `@ComponentScan`
- `@Singleton @Binds` → `@Single` on the implementation class
- AppModule aggregates all feature modules via `@Module(includes = [...])`

Convention plugins:
- `taigamobile.android.koin` — adds koin-bom, koin-core, koin-android, koin-compose, koin-compose-viewmodel, koin-annotations, koin-ksp
- `taigamobile.kmp.koin` — adds koin-bom, koin-core, koin-annotations, koin-ksp (no Android deps)

Since it's a solo project, can be done module-by-module without coordination overhead.

### Room KMP database migrations
Existing Room databases on user devices need migration paths that work on the KMP version. Test thoroughly — schema compatibility between the Android Room compiler and KMP Room compiler.

### Retrofit → Ktor is tedious but low risk
Every API interface needs rewriting. DTOs stay the same (kotlinx-serialization), and Ktor patterns are already established in the project via `tools/seed`.

### Compose Multiplatform on iOS
Currently Beta. Expect rough edges: keyboard handling, text input, platform navigation gestures, status bar, etc. These will be dealt with as they surface — not a blocker for the migration architecture.

### Desktop considerations
Desktop is nearly free once KMP + CMP is set up. Main differences: window management, system tray, file dialogs. Can use `expect`/`actual` for these.

### Strings / Resources
Moving from Android `R.string` to Compose Multiplatform `Res.string` is a significant but mechanical change. The `compose.resources` Gradle plugin generates type-safe accessors from XML or TOML resource files.

### Incremental is key
Every phase leaves the Android app fully functional. Never break the existing app to make KMP work. Platform-specific issues (iOS rendering, Desktop windowing) are handled after the architecture is in place.

---

## Rough Module Migration Order

For each phase, work bottom-up (leaf modules first):

```
Phase 1 (pure Kotlin → KMP):
  core/domain → core/async → core/appinfo-api → core/serialization
  → utils/formatter/* → feature/*/domain/ → feature/*/dto/ → feature/*/mapper/

Phase 2 (data layer → KMP):
  core/api (Retrofit→Ktor) → core/storage (Room+DataStore KMP)
  → feature/*/data/ (one by one)
  → Hilt→Koin across all modules

Phase 3 (UI → CMP):
  strings/ → utils/ui → uikit → core/navigation
  → feature/*/ui/ (one by one)
  → Platform entry points (iOS app, Desktop app)
```

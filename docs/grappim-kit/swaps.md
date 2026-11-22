# grappim-kit swaps in this app

`core/*` modules (and a few `androidApp`/`uikit` pieces) are progressively being extracted into
`grappim-kit` (sibling repo, shared across this app, wallosmobile, wayprint, HateItOrRateIt) and
swapped for the published Maven artifact. This is the per-swap record: what each swap changed here
and what it turned up. The general rules distilled from it stay in CLAUDE.md's "grappim-kit Modules"
section; `grappim-kit/CONSUMING.md` holds the kit-side writeup for each module.

Moved out of CLAUDE.md on 2026-11-22 (`docs/revisit.md` entry 55), unchanged apart from this header.

`core/*` modules are progressively being extracted into `grappim-kit` (sibling repo, shared
across this app, wallosmobile, wayprint, HateItOrRateIt) and swapped for the published Maven
artifact — `core/navigation` → `io.github.grigoriym:grappim-kit-navigation` was the first
(2026-09-08, PR #393). Before wiring a swap like this, diff the published module's actual source
against this repo's current HEAD rather than trusting an extraction's "canonical, mechanical
swap" verdict — an extraction commit can predate a fix that lands here afterward and still get
published stale (confirmed for `navigation` 0.1.0 vs. this repo's `e78fe61b`). See
`grappim-kit/CONSUMING.md`'s `navigation` section for the general gotcha. When the swap deletes
a `core/*` module outright, also drop its `kover(projects.X)` line
from the root `build.gradle.kts` aggregation and re-run `koverXmlReport`/`:koverVerify` — the
floor is enforced against whatever remains aggregated.

`uikit`'s theme/top-bar scaffolding and `NativeText` → `io.github.grigoriym:grappim-kit-uikit`
was the second swap (2026-09-09, PR #394) — unlike `navigation`, no local module was deleted
(`:uikit`/`:utils:ui` keep existing with reduced content), so add the new artifact as `api(...)`
on whichever local module's consumers reach the moved type through same-module resolution, not
`implementation(...)`. **A type moving out of a module breaks any file that referenced it via
same-package implicit resolution with no explicit import** — a repo-wide import-line rename
alone misses those; the build has to actually run to surface them (confirmed:
`utils/ui/PagingUtils.kt` and `SnackbarDelegate.kt` both referenced `NativeText` with no import
since it used to live in their own package). See `grappim-kit/CONSUMING.md`'s `uikit` section
for what diverged from the extraction's own writeup (an added `Surface` wrap in `KitTheme`, an
added top-bar slide animation, and the adapter pattern for a platform-varying `ColorScheme` like
Android's dynamic color, which `KitTheme` has no accommodation for on its own).

`core/logger`'s `Logcat`/`LogPriority`/`TaigaLogger`/`TimberLogger`/`NSLogLogger`/`FileLogger` →
`io.github.grigoriym:grappim-kit-logger` was the third swap (2026-09-09, PR #413) — the local
module was deleted, same as `navigation`. **Unlike `navigation`/`uikit`, check whether the
consuming app wires the old local module in centrally before assuming it's a per-module
`build.gradle.kts` line**: this repo's `build-logic/convention/.../KmpConfiguration.kt` hardcoded
`implementation(project(":core:logger"))` once inside `configureKmp()` (applied to every KMP
library module), not 40-odd separate `build.gradle.kts` additions — the swap there was a single
line to `libs.grappim.kit.logger`. `androidApp` (a plain Android application module that never
goes through `configureKmp()`) still needed its own explicit dependency line update, same as any
consumer.

`core/async-kmp`'s `ThreadSafeMap`/`ApplicationScope` provider machinery →
`io.github.grigoriym:grappim-kit-coroutines` was the fourth swap (2026-09-11, PR #421) — the
local module was kept (it still hosts this app's own Koin qualifier annotations and provider
functions, which just re-source from the kit's `KitDispatchers`/`applicationScope()` instead of
raw `kotlinx.coroutines`), so this was a per-module `build.gradle.kts` addition, not a
`build-logic` change. `ThreadSafeMap` needed `api(...)` rather than `implementation(...)` since
`WorkItemEditStateRepository` consumes the type one hop away from `core/async-kmp` — see
`grappim-kit/CONSUMING.md`'s `coroutines` section for the full writeup, including a real (not
just mechanical) behavior change: `applicationScope()` installs a `CoroutineExceptionHandler`
this app's own hand-rolled version never had.

`core/crash-api`'s `CrashReporter` → `io.github.grigoriym:grappim-kit-crash` and
`core/appinfo-api`'s `AppInfoProvider` → `io.github.grigoriym:grappim-kit-appinfo` were the fifth
swap (2026-09-11, PR #422). `crash` was byte-identical and `core/crash-api` was deleted outright,
same shape as `navigation`/`logger`. `appinfo` went the **opposite** direction from every prior
swap here: this app's own `AppInfoProvider` was a strict *superset* of the kit's, not a subset —
`getAppInfo()` (dropped as a pure formatting concern, now composed in
`SettingsAboutScreenViewModel` from `versionName()`/`versionCode()`/`buildType()`, which trims the
Android build's flavor suffix off the About screen) and `getDebugLocalHost()` (dropped as
app-specific; this app's local-dev network-debugging feature has no equivalent in the kit).
`core/appinfo-api` survives, narrowed to a single-method `DebugLocalHostProvider` interface;
every platform `AppInfoProviderImpl` implements both it and the kit's `AppInfoProvider` via
`@Single(binds = [AppInfoProvider::class, DebugLocalHostProvider::class])` — koin-annotations'
multi-type `binds` syntax, confirmed working here for the first time (see the **koin-expert**
subagent). See `grappim-kit/CONSUMING.md`'s `crash`/`appinfo` sections for the full writeup.

`core/domain`'s `PendingCertTrust`/`UntrustedCertificateException`/
`CertificateHostnameMismatchException`/`resultOf`/`mapResult` → `io.github.grigoriym:grappim-kit-domain`
was the sixth swap (2026-09-11, PR #423) — **not mechanical**: this app's exceptions extended
`java.security.cert.CertificateException` directly and were thrown straight out of
`CompositeTrustManager.checkServerTrusted`, but the kit's versions are plain commonMain
`Exception`s (portable, but no longer catchable by JSSE's handshake code, which only recognizes
`CertificateException`). Fixed by wrapping both throws at the JSSE boundary —
`throw CertificateException(UntrustedCertificateException(...))`, same pattern wallosmobile's
own swap already uses, extended here to also cover `CertificateHostnameMismatchException` (a
branch wallosmobile's `CompositeTrustManager` doesn't have). This pushes the kit exception one
level deeper in the cause chain, so `PlatformNetworkErrorMapper` (jvm/android `actual`) switched
from a one-level `exception.cause is X` check to the kit's `findPendingCertTrust()` (walks the
whole chain) plus a small local chain-walk for the hostname-mismatch case, which has no
kit-level helper. Existing tests that asserted `assertFailsWith<UntrustedCertificateException>`
directly on `checkServerTrusted`'s throw became **compile errors**, not just wrong assertions
(`Check for instance is always 'false'` — the kit type is no longer assignable to what the
method actually throws) — reasserted against the wrapper `CertificateException` and its `.cause`
instead. `core/domain` survives narrowed (`TaskIdentifier`/`CommonTaskType`/`NetworkException`/
`PlatformIOException`/`PlatformNetworkError`/`UntrustedCertificateNetworkException` stay local),
depending on the kit via `api(...)` since `PendingCertTrust` leaks through its own public types
to ~15 consumer modules. Added `RealTlsHandshakeJvmTest` (`core/api/src/jvmTest/`) — a real
self-signed HTTPS server (`keytool`-generated, no Docker) driven through the actual
`createPlatformHttpClientEngine`/`CompositeTrustManager` wiring over a genuine JDK TLS
handshake — to prove the wrap survives real JSSE, not just the hand-built exception chains
`CompositeTrustManagerTest`/`NetworkErrorMapperJvmTest` construct. See
`grappim-kit/CONSUMING.md`'s `domain` section for the full writeup.

`core/storage`'s `NetworkMonitor`(+impls)/`TrustedCertStorage`/`TokenCipher` →
`io.github.grigoriym:grappim-kit-storage`, and `core/api`'s `CompositeTrustManager` (jvm+android)
→ `io.github.grigoriym:grappim-kit-trustmanager`, were the seventh swap (2026-09-11, PR #424,
one combined PR since `grappim-kit-trustmanager`'s `CompositeTrustManager` takes the kit's own
`TrustedCertStorage` type directly). `NetworkMonitor`/`TrustedCertStorage`/`CompositeTrustManager`
were all mechanical — this app's own pre-swap classes were essentially byte-identical to what got
extracted, including the `CertificateHostnameMismatchException` wiring the `domain` swap had
already landed in `CompositeTrustManager` before `trustmanager` existed as its own kit module.
`TokenCipher`/`NoopTokenCipher`/`AndroidKeystoreTokenCipher` are replaced by the kit's
`SecretCipher`/`NoopSecretCipher`/`KeystoreSecretCipher` — the Android Keystore alias is kept as
`"taiga_auth_token_key"` so existing installs keep decrypting their stored tokens through the same
underlying key, and `decrypt()` is now nullable (`AuthStorageImpl` already handled that case via
`.orEmpty()`, so no behavior change). **Confirmed this app is not exposed to a real migration bug
wallosmobile found in the same swap**: `KeystoreSecretCipher`'s `"v1:"`-prefix passthrough treats
any *unprefixed* stored ciphertext as legacy plaintext rather than failing to decrypt, silently
corrupting a pre-existing encrypted value on upgrade for an app whose old cipher had no such
prefix — this app's deleted `AndroidKeystoreTokenCipher` already used the identical `"v1:"` prefix,
IV length, GCM tag length and cipher transformation, verified with a throwaway JVM test (not
committed) that round-trips the old ciphertext byte layout through the new cipher's decode path.
`core/storage`/`core/api` both survive, narrowed. `NetworkMonitorImpl`/`CompositeTrustManager`
aren't Koin-annotated in the kit, so each platform gained explicit `@Single` providers — the jvm
`NetworkMonitor` provider explicitly injects this app's existing singleton
`@IoDispatcher`/`@ApplicationScope` rather than the kit class's own bare defaults (`=
applicationScope()`), since that factory function builds a **new**, independent `CoroutineScope`
on every call rather than returning this app's canonical one. Local `CompositeTrustManagerTest`
(plus its `FakeX509Certificate`/`FakeX509TrustManager` fakes) was deleted rather than reworked —
it called the class's `internal` 3-arg `checkServerTrusted` overload, which stops compiling once
the class is external (Kotlin `internal` is compilation-unit-scoped); `RealTlsHandshakeJvmTest`
(added in the `domain` swap) already exercises the same real-JSSE-handshake path end-to-end and
needed no changes beyond a stale KDoc reference. The bare root coordinates
(`grappim-kit-storage`/`grappim-kit-trustmanager:0.1.4`) publish only `commonMain` + shared
intermediates (this is KGP's umbrella/metadata publication, not a `grappim-kit` defect —
`trustmanager` has no `commonMain` at all, so its root sources jar is empty) — diff-the-sources-jar
verification needs the per-target coordinates instead: `grappim-kit-storage-android`,
`grappim-kit-storage-jvm`, `grappim-kit-trustmanager-android`, `grappim-kit-trustmanager-jvm`, each
carrying a complete sources jar (confirmed by downloading and unzipping all four from Maven
Central). Same split-artifact shape the `logger` swap already documented above. See
`grappim-kit/CONSUMING.md`'s `storage`/`trustmanager` sections (TaigaMobileNova subsections) for
the full writeup.

`:testing`'s six hand-written fakes (`MainDispatcherRule`, `FakeNetworkMonitor`,
`FakeAppInfoProvider`, `FakeSecretCipher`, `FakeCrashReporter`, `FakeTrustedCertStorage`) →
`io.github.grigoriym:grappim-kit-testing` was the eighth swap (2026-09-11, PR #426) — all six were
functionally identical to this app's own versions (diffed against the local `grappim-kit`
checkout) modulo cosmetic formatting/doc-comment differences. `:testing/build.gradle.kts` dropped
its direct `api(grappim.kit.appinfo)`/`api(grappim.kit.crash)` lines, which existed only to
support the two deleted fakes — `grappim-kit-testing` re-exposes both transitively.

`androidApp`'s `AppUpdateChecker`/`AppUpdateCheckerImpl` (Play In-App Update wrapper) →
`io.github.grigoriym:grappim-kit-appupdate`/`-gplay`/`-fdroid` was the ninth swap (2026-09-12, PR
#427) — byte-identical apart from the package rename and dropped Koin annotation, confirmed by
downloading the published `0.1.4` sources jars from Maven Central and diffing them against the
local `grappim-kit` checkout (itself confirmed to match the published jars) and then against this
app's pre-swap code. Unlike every other swap so far, this kit module ships as **three separate
artifacts, not one** — `grappim-kit-appupdate` (interface + `UpdateState`), `-gplay` and `-fdroid`
(each `api`-depends on the base module, mirroring this app's own `gplayImplementation`/
`fdroidImplementation` split as separate Maven coordinates instead of separate source sets). Since
neither impl carries a Koin annotation and the gplay/fdroid constructors genuinely differ
(`Context` vs. no-arg — no single shared provider function can cover both, unlike `storage`'s
`NetworkMonitor`/`SecretCipher`/`TrustedCertStorage` providers), each flavor got its own small
`AppUpdateModule.kt` (`@Module` class with one `@Single` provider function) in
`androidApp/src/{gplay,fdroid}/.../data/`, included into `AndroidModule` via
`@Module(includes = [AppUpdateModule::class])`. **A DI change entirely inside `androidApp` is
invisible to `KoinGraphTest`** (which only verifies `composeApp`'s graph) — compiling both flavors
proves the impl classes satisfy the interface but not that Koin can resolve the binding at
runtime, so this was verified with a real cold start on `Medium_Phone_API_36.1` for both flavors:
no crash on either, and the gplay flavor's logcat showed PlayCore's real `AppUpdateService`
actually bind and run `requestUpdateInfo`/`registerListener` from `MainActivity.onCreate`/
`onResume`. See `grappim-kit/CONSUMING.md`'s `appupdate` section (TaigaMobileNova subsection) for
the full writeup.

`TaigaDrawerWidget`/`TaigaNavigationSuiteWidget` → `com.grappim.kit.uikit.widgets.drawer`'s
`DrawerWidget`/`NavigationSuiteWidget` was the tenth swap (2026-09-12, PR pending) — this app was
the extraction source, so the port was mechanical: `DrawerItem`/`IconSource` deleted locally,
`DrawerItemsBuilder`'s ~15 construction sites switched `label = RString.x` to
`NativeText.Resource(RString.x)`, `MainViewModel`'s `drawerItems` type gained the `DrawerDestination`
type argument, and `MainScreen.kt`'s two call sites now pass `headerTitle` explicitly instead of the
widget reading `app_name` internally. `DrawerDestination` itself stays local as the generic `T`. See
`grappim-kit/CONSUMING.md`'s `widgets/drawer` section (TaigaMobileNova subsection) for the full
writeup, including why the two local drawer tests were updated rather than deleted despite the kit
now having its own.

The custom request headers core (`CustomHeader`/`CustomHeaderValidator`/`CustomHeadersStorage`(+`Impl`),
the `CustomHeaders` Ktor plugin, `sanitizeCustomHeaders`, and `FakeCustomHeadersStorage` via
`grappim-kit-testing`) → `io.github.grigoriym:grappim-kit-customheaders` was the eleventh swap
(2026-11-14, PR #477, `grappimKit` 0.1.7 → 0.1.8). **The kit's `CustomHeaderValidator` is a class whose
default reserved names lack `Authorization`/`User-Agent`** — `StorageModule.provideCustomHeaderValidator()`
binds it with both added, and `CustomHeadersViewModel` injects it; constructing the validator with no
argument would silently let a user header override the Bearer token. Storage key, DataStore files and
Keystore alias are unchanged — emulator-confirmed by saving a header on the pre-swap build and
reading it back after installing the swapped one over it. See `grappim-kit/CONSUMING.md`'s `customheaders` section.

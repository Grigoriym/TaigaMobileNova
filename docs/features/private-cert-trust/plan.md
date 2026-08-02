# Private CA / Self-Signed Certificate Trust — Implementation Plan

See `research.md` in this folder for the survey this plan is based on (Nextcloud, Jellyfin,
Syncthing, Home Assistant).

## Progress

- [x] **Phase 1 — Storage**
  - [x] `TrustedCertStorage` interface + `DataStore`-backed impl (`core/storage/.../cert/TrustedCertStorage.kt`)
  - [x] Per-platform DataStore provider wiring in `StorageModule.android.kt` / `.jvm.kt` / `.ios.kt`
  - [x] `FakeTrustedCertStorage` in `testing/`
  - [x] Unit tests (`core/storage/src/jvmTest/.../cert/TrustedCertStorageImplTest.kt`) — real `DataStore` round-trip, covers host-scoping specifically
- [x] **Phase 2 — Trust manager + OkHttp engine wiring**
  - [x] Composite `X509TrustManager` (Android/JVM), checks `TrustedCertStorage` before falling back to the platform default
  - [x] expect/actual platform `HttpClientEngine` provider hook (`core/api`)
  - [x] `KmpNetworkModule.kt` updated to use the new engine hook instead of bare `HttpClient { }`
  - [x] Unit tests (`core/api/src/jvmTest/.../CompositeTrustManagerTest.kt`) — pin hit/miss, host-scoping, cert-scoping, expiry-on-pin-hit, null-host fallback
- [x] **Phase 3 — Exception plumbing**
  - [x] `PendingCertTrust` portable model (`core/domain`)
  - [x] Chain-carrying `CertificateException` subtype (`UntrustedCertificateException`, Android/JVM-shared, lives in `core/domain` not `core/api` — see note below)
  - [x] `NetworkErrorMapper` detects and passes it through distinctly instead of collapsing to a generic code
  - [x] Unit tests (`core/api/src/jvmTest/.../CompositeTrustManagerTest.kt` + `.../errors/NetworkErrorMapperJvmTest.kt`) — wrapping, unwrapping, and the plain-SSL-failure-still-falls-back-to-generic-code case
- [x] **Phase 4 — Login UI**
  - [x] `LoginViewModel` state + retry-on-accept flow (shared `handleFailure` helper used by all three failure sites: password/LDAP login, GitHub client-id fetch, GitHub code exchange)
  - [x] `LoginScreen` — second `ConfirmActionDialog` instance for cert trust (reused, no new uikit component)
  - [x] New strings (`cert_trust_dialog_title`, `cert_trust_dialog_description`)
  - [x] Unit tests (`LoginViewModelTest.kt`) — dialog shown on failure, dismiss clears without retry/trust, confirm persists the pin and retries
- [x] **Phase 5 — Tests + manual QA**
  - [x] Unit tests for `TrustedCertStorage`, composite trust manager, `NetworkErrorMapper`, `LoginViewModel` — all done incrementally in Phases 1–4
  - [x] Manual repro against a local self-signed instance (see `server-setup.md`) — done against a throwaway nginx TLS proxy in front of the local docker-compose Taiga instance. Confirmed: dialog shows correct issuer/subject/validity/fingerprint, dismiss doesn't persist anything, accept pins and retries, pin survives app restart.
  - [x] **QA finding (not a bug, confirmed by design):** logging out, logging into a different server, then logging back into the previously-trusted untrusted-cert host does **not** re-show the dialog. This is correct — the pin is keyed by `(host, fingerprint)` in `TrustedCertStorage`, independent of login session state (`AuthStorage` is untouched by this feature). Re-prompting on every login would defeat the purpose of TOFU pinning and would train users to reflexively accept dialogs. The dialog correctly reappears only if the host's fingerprint actually changes (rotation or MITM swap attempt) — see `server-setup.md` step 6 for the test that proves this.
- [x] **Phase 6 — Settings UI to view/revoke trusted certs**
  - Promoted from "Out of Scope" below, triggered by the Phase 5 QA finding above: there was no way for a user to un-pin a host short of clearing app data. Added a "Trusted Certificates" screen under Settings listing pinned entries with a revoke action per row.
  - **Scope decision (revised):** initially built as host + fingerprint only (matching what was persisted at the time), then explicitly revisited — since this feature hasn't shipped yet, there was no migration cost to widening the storage shape now rather than living with a fingerprint-only revoke screen long-term. Went with the full `PendingCertTrust` (host, subject, issuer, notBefore, notAfter, sha256Fingerprint) instead, so the revoke screen can show *why* a cert was trusted (issuer, expiry), not just an opaque hash.
  - `PendingCertTrust` (`core/domain`) is now `@Serializable` and reused directly as the persisted entry type — no separate storage-layer model. `TrustedCertStorage` (`core/storage/.../cert/TrustedCertStorage.kt`) signature changed: `trust(host, fingerprint)` → `trust(pendingCertTrust: PendingCertTrust)`, added `getAllFlow(): Flow<List<PendingCertTrust>>` and `suspend fun untrust(host, sha256Fingerprint)`. `isTrusted(host, fingerprint)` unchanged. Storage format changed from a `stringSetPreferencesKey` of delimited `"host|fingerprint"` strings to a single `stringPreferencesKey` holding a JSON-encoded `List<PendingCertTrust>` (via the existing `@StorageJsonQualifier` `Json` instance, same convention `FiltersStorageImpl` already uses) — trusting an already-pinned `(host, fingerprint)` again replaces the stored entry rather than duplicating it. `FakeTrustedCertStorage` (`testing/`) and `LoginViewModel.onConfirmCertTrust()` (now passes the whole `pendingCertTrust` instead of just two of its fields) updated to match.
  - New screen: `feature/settings/ui/.../trustedcerts/` (`TrustedCertificatesScreen.kt`, `TrustedCertificatesState.kt`, `TrustedCertificatesViewModel.kt`, `TrustedCertificatesNavDestination.kt`) — modeled on the existing `attributes/tags/TagsScreen*` list-with-delete pattern (`LazyColumn` + `ListItem` rows + trailing delete `IconButton` + `ConfirmActionDialog` confirm-before-revoke), but simpler: no loading/error state needed since reads/writes are local `DataStore` calls, not network. `TrustedCertificatesViewModel` collects `getAllFlow()` directly into state, so revoking an entry updates the list reactively with no manual list-filtering. Each row shows host (headline) + issuer, valid-until date, and fingerprint (supporting content). Empty state reuses uikit's existing `EmptyStateWidget` with its default message.
  - Wired into `SettingsScreen.kt` (new `ListItem` + `goToTrustedCertificatesScreen` callback, unconditional — not gated behind `canSeeAttributes` since TLS trust is device-level, not project-permission-based) and `composeApp/.../nav/SettingsNavGraph.kt` (new `composable<TrustedCertificatesNavDestination>` block).
  - New strings: `settings_trusted_certificates`, `trusted_cert_issuer`, `trusted_cert_valid_until`, `trusted_cert_fingerprint`, `revoke_cert_title`, `revoke_cert_text`.
  - Unit tests: `core/storage/src/jvmTest/.../cert/TrustedCertStorageImplTest.kt` (real-DataStore `trust`/`getAllFlow`/`untrust` round-trip, including re-trusting an existing pin replaces rather than duplicates) + `feature/settings/ui/src/commonTest/.../trustedcerts/TrustedCertificatesViewModelTest.kt` (init population, revoke-click/confirm/dismiss flow) + `CompositeTrustManagerTest.kt` updated for the new `trust()` signature (via a test-local `FakeTrustedCertStorage.trust(host, fingerprint)` convenience overload, since those tests only care about the pin, not the full cert metadata) — all passing.
- [x] **Phase 7 — Hostname/certificate mismatch handling**
  - **Bug found via manual QA (2026-07-24):** a self-signed cert issued for `192.168.0.241` was reachable at `192.168.0.248` (wrong IP typed / server moved between test runs). Two related gaps surfaced:
    1. Connecting to the wrong host with an otherwise-fine (or already-pinned) cert threw `SSLPeerUnverifiedException` — a *different* exception than the `SSLHandshakeException`/`CertificateException` path `NetworkErrorMapper` already handled — so it fell all the way through to the generic `ERROR_NETWORK_IO` fallback instead of any SSL-specific message.
    2. Worse: connecting to `.248` with the *untrusted* `.241` cert still triggered the untrusted-cert TOFU dialog (chain-trust validation runs before hostname verification, and our trust manager only cares about chain trust). Accepting it persisted a pin for `(192.168.0.248, that fingerprint)` — a pin that can never actually let a connection succeed, since hostname verification is a separate OkHttp step untouched by our trust manager and will keep rejecting the SAN/CN mismatch regardless of the pin.
  - Fix, part 1 — generic mismatch message: `NetworkException.ERROR_SSL_HOSTNAME_MISMATCH` (new code) + `error_ssl_hostname_mismatch` string. `mapPlatformNetworkError` (both platform actuals) now maps `SSLPeerUnverifiedException` to it directly.
  - Fix, part 2 — stop offering TOFU for a cert that can never work: `CompositeTrustManager.checkServerTrusted` (`core/api/{android,jvm}Main`) now checks whether the presented leaf cert's SAN entries (DNS name type 2 / IP address type 7, falling back to the subject CN if no SANs are present) actually cover the connecting host *before* wrapping the failure as an offer to trust. On mismatch it throws a new `CertificateHostnameMismatchException` (`core/domain/{android,jvm}Main`, same placement/reasoning as `UntrustedCertificateException`) instead of `UntrustedCertificateException` — no dialog shown, since pinning would be pointless. `mapPlatformNetworkError` routes this to the same `ERROR_SSL_HOSTNAME_MISMATCH` code as part 1, so both "chain untrusted and wrong host" and "chain trusted but wrong host" show the identical clear message instead of a confusing trust prompt.
  - This check only runs on the *offer-TOFU* path (unpinned cert, default trust manager rejected it) — a hostname match is **not** re-verified on every pin hit, since that would re-litigate a decision the user already made for an already-pinned exact `(host, fingerprint)`.
  - `FakeX509Certificate` (`core/api/jvmTest`) gained a `commonName` (default `"taiga.example.com"`, chosen to keep every pre-existing test passing unmodified) and `subjectAlternativeNames` constructor param to make this testable.
  - Unit tests: `CompositeTrustManagerTest.kt` (CN mismatch rejected, SAN-IP match still offers TOFU, SAN-IP mismatch rejected — reproducing the exact `.241`/`.248` scenario) + `NetworkErrorMapperJvmTest.kt` (`SSLPeerUnverifiedException` and `CertificateHostnameMismatchException` both → `ERROR_SSL_HOSTNAME_MISMATCH`) — all passing.

## Goal

Let a user connect to a self-hosted Taiga instance whose TLS certificate isn't trusted by
Android's default CA store (self-signed, or issued by a private CA) — currently this fails with
no way to proceed at all, as reported in
[#322](https://github.com/Grigoriym/TaigaMobileNova/issues/322).

**Scope: Android only.** The mechanics below (OkHttp engine, `X509TrustManager`) are also valid on
JVM Desktop since it shares the OkHttp engine, so the storage/trust-manager layer will work there
for free — but the Login UI flow described here is only being built for Android right now. iOS
(Darwin engine) is untouched.

## Decision: pattern C (trust-on-first-use per-host pinning), not B

Rejected app-wide `network_security_config.xml` trusting user CAs (pattern B, used by Jellyfin and
Home Assistant) because both surveyed implementations share the same weakness: trust is global to
the device and every domain the app talks to, with zero user-facing visibility into *which*
certificate was actually accepted. Building pattern C instead, modeled on Nextcloud's approach
(the only surveyed app that does this for real), but fixing two flaws found in their
implementation:

1. **Pin `(host, fingerprint)`, not fingerprint alone.** Nextcloud's `isKnownServer()` checks the
   certificate only — once accepted for any host, that exact cert is silently trusted for *any*
   other host that presents it. We scope the stored pin to the host it was accepted for.
2. **Still validate expiry on every connection**, even for a previously-pinned cert. Nextcloud
   skips `checkValidity()` entirely once a cert is in the known-servers store, so an
   expired-but-previously-accepted cert is trusted forever with no re-check.

Also explicitly **not** copying Syncthing's disabled hostname verification (only safe there
because its target is hardcoded to loopback) or Nextcloud's plaintext-file `KeyStore` with a
hardcoded password (`"password"`) — using `DataStore<Preferences>` instead, consistent with how
`AuthStorage` already persists auth tokens in this codebase.

## Flow

1. User enters a server URL, taps Continue/Login.
2. `AuthRepository` request fails at the TLS handshake because the cert isn't in the system trust
   store and isn't yet pinned for this host.
3. The composite trust manager (installed in the OkHttp engine) has already seen the full
   presented chain during `checkServerTrusted` — on rejection it throws a `CertificateException`
   subtype carrying the chain details (subject, issuer, validity, SHA-256 fingerprint), not just a
   bare failure.
4. This propagates up through Ktor as an `SSLHandshakeException` wrapping that exception.
   `NetworkErrorMapper` (via the existing `mapPlatformNetworkErrorCode` platform hook) recognizes
   it specifically and it reaches `LoginViewModel` as a distinct, inspectable failure — not
   collapsed into the generic `ERROR_SSL_CERTIFICATE` message we already ship.
5. `LoginViewModel` shows a confirm dialog (reusing uikit's existing `ConfirmActionDialog` — same
   component already used for the "unencrypted connection" warning) with the cert's issuer,
   subject, validity dates, and SHA-256 fingerprint formatted into the description text.
6. **Accept** → `TrustedCertStorage.trust(host, fingerprint)` → retry the original login request
   (now the trust manager's pin check on step 3 succeeds).
   **Reject** → dismiss, login fails as it does today.
7. Any *later* request to a host with a rotated/different cert (pin no longer matches) fails the
   same way it does today (generic `ERROR_SSL_CERTIFICATE` message) — no re-prompt outside the
   login flow. See Out of Scope.

## Architecture

### `core/storage` — `TrustedCertStorage`

New interface + `DataStore<Preferences>`-backed impl, same shape as `AuthStorage`:

```kotlin
// core/storage/src/commonMain/.../cert/TrustedCertStorage.kt
interface TrustedCertStorage {
    suspend fun isTrusted(host: String, sha256Fingerprint: String): Boolean
    suspend fun trust(host: String, sha256Fingerprint: String)
}
```

Stored as a `Set<String>` of `"$host|$sha256Fingerprint"` entries in its own DataStore file
(`trusted_certs`), following the exact pattern `PlatformStorageModule`/`provideAuthStorage` already
use per platform in `StorageModule.android.kt` / `.jvm.kt` / `.ios.kt`. **Done** — see
`core/storage/src/commonMain/.../cert/TrustedCertStorage.kt`.

### `core/api` — composite trust manager + OkHttp engine wiring (done)

Built as planned below, with one scope decision made during implementation: **Phase 2 does not yet
change the error surfaced for an unpinned/rejected cert.** `CompositeTrustManager.checkServerTrusted`
falls back to the real default `X509TrustManager` and lets whatever it throws propagate completely
unchanged — same `CertificateException` → `SSLHandshakeException` → generic `ERROR_SSL_CERTIFICATE`
path as before this feature existed. Phase 2 only adds the *capability* to succeed on a pinned
`(host, fingerprint)`; it deliberately doesn't touch the failure path yet. That's what Phase 3 is for
(see below — the chain-carrying exception is still not built).

- `core/api/src/androidMain/.../CompositeTrustManager.kt` (+ identical copy in `jvmMain`, same
  reasoning as `PlatformNetworkErrorMapper`'s Android/JVM duplication — both share the OkHttp
  engine): wraps a real default `X509TrustManager` (`TrustManagerFactory` with a `null` `KeyStore`,
  i.e. the system trust store). `checkServerTrusted` extracts the host from the `SSLSocket`/
  `SSLEngine` overload (`handshakeSession.peerHost` / `engine.peerHost`), checks
  `TrustedCertStorage.isTrusted(host, sha256Fingerprint(leaf))` — **still calling
  `leaf.checkValidity()` even on a pin hit**, fixing the flaw flagged in Nextcloud's implementation
  — and only falls back to the default manager if unpinned. Hostname verification is untouched
  (OkHttp's default), not disabled à la Syncthing.
- `core/api/src/commonMain/.../PlatformHttpClientEngine.kt`: `expect fun
  createPlatformHttpClientEngine(trustedCertStorage: TrustedCertStorage): HttpClientEngine`. Android/
  JVM actual builds an `SSLContext` around the composite trust manager and passes it to
  `OkHttp.create { config { sslSocketFactory(...) } } }`. iOS actual is `Darwin.create()`, ignoring
  the parameter — unchanged behavior, matches the "Android only for now" scope.
- `KmpNetworkModule.kt`'s two `@Single HttpClient` providers now take `TrustedCertStorage` as an
  injected parameter and construct via `HttpClient(createPlatformHttpClientEngine(trustedCertStorage))
  { ... }` instead of the old engine-agnostic `HttpClient { ... }`.

### `core/domain` — portable cert-info model + exception (done)

```kotlin
// core/domain/src/commonMain/.../PendingCertTrust.kt
data class PendingCertTrust(
    val host: String,
    val subject: String,
    val issuer: String,
    val notBefore: String,
    val notAfter: String,
    val sha256Fingerprint: String
)
```

Kept fully portable (no `java.security.cert.X509Certificate`) so `LoginViewModel`/UI code in
commonMain can consume it directly.

**Module-placement correction made during implementation:** the original sketch assumed the
chain-carrying `CertificateException` subtype would live in `core/api` (next to
`CompositeTrustManager`). That's backwards — `core/api` depends on `core/domain`, not the other way
around, and the Phase 3 unwrapping logic (`mapPlatformNetworkError`, an expect/actual) has to live
in `core/domain` too since that's where `PlatformIOException`'s existing platform-detection
machinery already lives. So `UntrustedCertificateException` (Android/JVM-shared, `CertificateException`
subtype, carries a `PendingCertTrust`) was placed in `core/domain/src/androidMain` +
`core/domain/src/jvmMain` instead — `core/api`'s `CompositeTrustManager` just imports it from there,
which is the correct dependency direction.

Two distinct exception types ended up necessary, at two different layers:

- **`UntrustedCertificateException`** (`core/domain`, Android/JVM-only, `CertificateException`
  subtype) — internal plumbing. Thrown by `CompositeTrustManager` to satisfy JSSE's contract; never
  leaves the Android/JVM boundary except being unwrapped by the platform actual below.
- **`UntrustedCertificateNetworkException`** (`core/domain`, commonMain, `PlatformIOException`
  subtype, carries a `PendingCertTrust`) — the portable type that actually propagates up through
  Ktor → `ErrorMappingPlugin` → `AuthRepository` → `LoginViewModel`, where Phase 4 will catch it
  specifically.

`mapPlatformNetworkErrorCode(exception): Int?` (the existing expect/actual hook) was widened into
`mapPlatformNetworkError(exception): PlatformNetworkError?`, where `PlatformNetworkError` is a new
commonMain sealed class (`Code(errorCode: Int)` | `UntrustedCertificate(pendingCertTrust:
PendingCertTrust)`). The Android/JVM actual checks whether an `SSLHandshakeException`'s `cause` is
an `UntrustedCertificateException` first (→ `UntrustedCertificate`), otherwise falls back to the
same `SSLHandshakeException`/`UnknownHostException` → `Code(...)` mapping as before. iOS actual
unchanged, still always `null`.

`NetworkErrorMapper.mapToNetworkException` now branches on `PlatformNetworkError.UntrustedCertificate`
→ constructs `UntrustedCertificateNetworkException`, vs. `PlatformNetworkError.Code` → constructs
`NetworkException(errorCode)` as before.

`NativeText.getErrorMessage` gained a case for `UntrustedCertificateNetworkException` → the existing
`error_ssl_certificate` string, as the fallback for any caller that doesn't specifically intercept
it (i.e. everything except the Phase 4 login flow) — matches the "Out of Scope" decision that
non-login requests hitting an untrusted cert keep showing the generic message.

### `core/api` — composite trust manager + OkHttp engine wiring (done)

Built as planned above (see the Phase 2 note higher up in this doc) — `CompositeTrustManager` was
revisited in Phase 3 to add the try/catch around `defaultTrustManager.checkServerTrusted`: on
failure, if `host != null` it now throws `UntrustedCertificateException(pendingCertTrust, cause)`
instead of letting the bare `CertificateException` through; if `host == null` (no way to offer TOFU)
the original exception still propagates unchanged. `pendingCertTrust(...)` extracts subject/issuer
via `certificate.subjectX500Principal.name`/`issuerX500Principal.name` and formats validity dates as
`yyyy-MM-dd`.

### `feature/login/ui` — the dialog + retry (done)

**Scope decision made during implementation:** the original sketch only mentioned wiring this into
the main password/LDAP `login()` path. In practice `LoginViewModel` has three separate places that
call the auth repo and handle failure identically (`login(authData)`, `startGithubOAuth()`,
`authWithGithub(code)`) — GitHub login goes through the same `HttpClient` and can hit the exact same
untrusted-cert failure. Leaving two of the three paths showing a raw fallback message while only one
got the dialog would've been an inconsistent, confusing UX for no real savings, so all three route
through one shared helper instead:

```kotlin
private fun handleFailure(error: Throwable, logMessage: String, retry: () -> Unit) {
    logcat(throwable = error) { logMessage }
    isLoading(false)
    if (error is UntrustedCertificateNetworkException) {
        pendingRetry = retry
        _state.update { it.copy(pendingCertTrust = error.pendingCertTrust, isCertTrustDialogVisible = true) }
    } else {
        _state.update { it.copy(error = getErrorMessage(error)) }
    }
}
```

Each call site passes its own retry lambda (`{ login(authData) }` / `{ startGithubOAuth() }` /
`{ authWithGithub(code) }`) and its original log tag (`"Login error"` / `"GitHub OAuth error"` /
`"GitHub auth error"`), so the existing per-path log distinction wasn't lost. `pendingRetry` is a
plain `private var` on the ViewModel (not part of `LoginState`, since it's a function, not display
state) — `onConfirmCertTrust()` persists the pin via `TrustedCertStorage.trust(...)`, clears the
dialog state, then invokes it; `onDismissCertTrust()` just clears state without touching storage or
retrying.

`LoginScreen.kt` reuses `ConfirmActionDialog` (already used for the insecure-connection warning) —
no new uikit component needed. `description` is built via
`stringResource(RString.cert_trust_dialog_description, host, issuer, subject, notBefore, notAfter,
sha256Fingerprint)`, guarded by `state.pendingCertTrust?.let { ... }` since the dialog param is
nullable and only ever populated together with `isCertTrustDialogVisible = true`.

One dependency fix needed along the way: `feature/login/ui` didn't have a direct `core.domain`
dependency (only transitively through `core.api`'s `implementation`, which Gradle doesn't expose
downstream) — added `implementation(projects.core.domain)` to its `build.gradle.kts` so
`PendingCertTrust`/`UntrustedCertificateNetworkException` resolve.

## Files Created (Phases 1–3)

| File | Description |
|------|--------------|
| `core/storage/src/commonMain/.../cert/TrustedCertStorage.kt` | Interface + `DataStore`-backed impl |
| `core/storage/src/{android,jvm,ios}Main/.../di/StorageModule.*.kt` | Per-platform DataStore provider additions, mirroring `provideAuthStorage` |
| `core/storage/src/jvmTest/.../cert/TrustedCertStorageImplTest.kt` | Real-`DataStore` round-trip tests |
| `testing/.../storage/FakeTrustedCertStorage.kt` | Fake for ViewModel tests (Phase 4) |
| `core/domain/src/commonMain/.../PendingCertTrust.kt` | Portable cert-info model |
| `core/domain/src/commonMain/.../UntrustedCertificateNetworkException.kt` | Portable, propagates up through Ktor to the ViewModel |
| `core/domain/src/{android,jvm}Main/.../UntrustedCertificateException.kt` | Internal, `CertificateException` subtype for the JSSE contract |
| `core/domain/src/commonMain/.../PlatformNetworkErrorMapper.kt` | Widened to `PlatformNetworkError` sealed class + `mapPlatformNetworkError` (was `mapPlatformNetworkErrorCode: Int?`) |
| `core/api/src/commonMain/.../PlatformHttpClientEngine.kt` (+ `android`/`jvm`/`ios` actuals) | expect/actual engine provider hook |
| `core/api/src/{android,jvm}Main/.../CompositeTrustManager.kt` | Composite `X509TrustManager` |
| `core/api/src/jvmTest/.../CompositeTrustManagerTest.kt` + `FakeX509Certificate.kt` + `FakeX509TrustManager.kt` | Unit tests + hand-written doubles |
| `core/api/src/jvmTest/.../errors/NetworkErrorMapperJvmTest.kt` | JVM-only tests (`SSLHandshakeException`/`UnknownHostException` aren't available to `commonTest`) |

## Files Modified (Phases 1–3)

| File | Change |
|------|--------|
| `core/api/.../errors/NetworkErrorMapper.kt` | Branches on `PlatformNetworkError` instead of a bare `Int?` |
| `core/api/.../KmpNetworkModule.kt` | Both `HttpClient` providers take `TrustedCertStorage` and build via `createPlatformHttpClientEngine(...)` |
| `utils/ui/.../NativeText.kt` | `getErrorMessage` gained an `UntrustedCertificateNetworkException` → `error_ssl_certificate` fallback case |

## Files Still To Create/Modify (Phase 4)

| File | Change |
|------|--------|
| `feature/login/ui/.../LoginViewModel.kt` | New state + retry-on-accept flow |
| `feature/login/ui/.../LoginScreen.kt` | Second `ConfirmActionDialog` instance for cert trust |
| `strings.xml` | New dialog title/description strings |

## What Stays the Same

- `ERROR_SSL_CERTIFICATE` / `error_ssl_certificate` message already shipped — stays as the
  fallback shown for any request that hits an untrusted cert outside the login flow (see Out of
  Scope).
- `AuthHeaderPlugin`, `TokenRefreshPlugin`, `HostSelectionPlugin` — untouched.
- Existing cleartext-HTTP warning dialog/flow — untouched, unrelated code path.

## Out of Scope (for now)

- Re-prompting mid-session if a previously-pinned cert rotates on a background request — falls
  back to the existing generic error message, pushing the user to re-login to re-trust.
- iOS (Darwin engine) support.
- ~~A UI to view/revoke previously-trusted certs (e.g. in Settings).~~ — promoted to Phase 6 above
  after manual QA (2026-07-24) raised the question of un-pinning a host without clearing app data.
- Certificate pinning to a fixed set of known-good CAs (unrelated feature, not what's being asked
  for here).
- ~~Showing subject/issuer/validity dates in the Phase 6 revoke screen.~~ — done; revisited the
  scope decision before shipping and widened storage to the full `PendingCertTrust` (see Phase 6
  note above) rather than leaving a fingerprint-only screen as permanent scope.

## Open Questions

- ~~Exact return type change needed on `mapPlatformNetworkErrorCode`...~~ — resolved in Phase 3:
  widened to `PlatformNetworkError` sealed class, see Architecture section above.
- ~~Whether `TrustedCertStorage` should live under `core/storage/.../cert/` or alongside
  `core/storage/.../auth/`~~ — resolved in Phase 1: went with `.../cert/`, kept as its own file
  rather than folded into `AuthStorage`, since the two are conceptually separate (login credentials
  vs. TLS trust decisions) even though both persist via `DataStore`.

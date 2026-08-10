# MASVS register

Profile: Android / iOS (JVM/desktop is outside MASVS — noted where relevant, not reviewed as a
MASVS target) · self-hosted, user-supplied server · reviewed 2026-08-09/2026-08-10, STORAGE, CRYPTO,
NETWORK, AUTH, PLATFORM, CODE, PRIVACY and RESILIENCE (scope decision only).

Out of scope: **MASVS-RESILIENCE.** Anti-tamper/anti-reverse-engineering controls defend a
vendor's asset against the device's own owner; this app has none. Confirmed, not assumed, for
this specific app: it embeds no secret worth protecting from reverse engineering — the GitHub
OAuth `client_id` is fetched at runtime from the server's `taiga-conf.json` (not bundled in the
binary, and a `client_id` isn't sensitive regardless), the OAuth `code`→token exchange (the step
that needs a `client_secret`) happens server-side, not in the app, and no other API key/secret is
embedded anywhere (`grep -rln 'client_secret\|CLIENT_SECRET\|clientSecret'` across the repo,
outside `build/`, is empty). Server credentials belong to the user pointing the app at their own
Taiga instance, and the source is public, so there is no vendor asset for tamper-detection or
obfuscation to defend. Reproducible builds — the thing that would actually matter for a FOSS
client in this space — are outside what MASVS covers. Formal decision, task 7 of
`docs/security/masvs-review-plan.md`.

## Accepted deviations

| Control | What we do instead | Bound | Why |
|---|---|---|---|
| MASVS-STORAGE-2 | Cached server URL (`DataStoreServerStorage`, `core/storage/.../server/DataStoreServerStorage.kt`) stored in plaintext DataStore | Value is a bare base URL (e.g. `https://api.taiga.io` or a self-hosted host) — no userinfo/credential embedded in it, confirmed by reading the only write path (`defineServer`) and its default (`getServerDefaultValue`) | Reveals which Taiga instance the user talks to, not a credential; unencrypted storage is proportionate to that sensitivity |
| MASVS-NETWORK-1 | Custom TOFU `X509TrustManager` on Android/JVM-desktop (`CompositeTrustManager`, `core/api/src/androidMain\|jvmMain/.../CompositeTrustManager.kt`) | Falls through to the platform default trust manager first — TOFU is only reached from the `catch (e: CertificateException)` arm after `defaultTrustManager.checkServerTrusted` has already rejected the chain (`:63-79`). Before offering trust it requires the presented leaf's SAN (or CN fallback) to match the connecting host (`hostMatchesCertificate`, `:95-111`) — a host/cert mismatch throws `CertificateHostnameMismatchException` instead of offering TOFU. The pin key is `(host, sha256Fingerprint)` (`TrustedCertStorageImpl.matches`, `core/storage/.../cert/TrustedCertStorage.kt:53-54`) — per-**certificate**, not per-host: a regenerated cert on an already-trusted host does not match the stored entry and re-triggers TOFU rather than being silently accepted. Backed by a dedicated unit test, `CompositeTrustManagerTest.\`pin for a host does not trust a different certificate presented by that same host\``. A pinned-but-expired cert still throws (`leaf.checkValidity()` at `:59`, also tested). Ships a user-facing revoke UI (`feature/settings/ui/.../trustedcerts/TrustedCertificatesScreen.kt`) | Self-hosted servers commonly run self-signed certs; this is a bounded TOFU implementation with an explicit per-certificate pin and a revoke path, not a naive trust-everything override |
| MASVS-NETWORK-1 | `android:usesCleartextTraffic="true"` (`androidApp/src/main/AndroidManifest.xml:20`), no `android:networkSecurityConfig` scoping it | Applies to every host, not restricted to LAN/dev addresses — confirmed no `network_security_config.xml` exists anywhere in the repo. `AuthHeaderPlugin` (`core/api/.../AuthHeaderPlugin.kt:31-35`) attaches the bearer token to every request regardless of `URLProtocol`, so the token is sent in the clear if the user configures an `http://` server. `LoginViewModel` warns once at login time (see MASVS-AUTH-1 row below), but nothing warns again for the ongoing bearer-token traffic that follows — tracked as a small follow-up, `docs/revisit.md` #32, not fixed in this review | Self-hosted LAN Taiga instances commonly run plain HTTP; scoping cleartext off entirely would break that use case |
| MASVS-NETWORK-2 | No identity pinning for "endpoints under the developer's control" | N/A by construction — the server is user-supplied, not developer-operated (the same TOFU/pinning mechanism above exists, but for a different reason: user-approved trust, not developer-mandated pinning) | Per the control's own qualifier |
| MASVS-AUTH-1 | Primary username/password and LDAP login send the credential via a plain Ktor `POST auth` (`AuthApiImpl.kt:26-28`, called from `AuthRepositoryImpl.auth`, `AuthRepositoryImpl.kt:26-43`) over the same client/channel MASVS-NETWORK already characterized | Same bound as the MASVS-NETWORK-1 cleartext row above — the credential rides whatever scheme the configured server uses. Additionally bounded here: `LoginViewModel` shows a one-time "Unencrypted connection" confirmation (`login_alert_title`/`login_alert_text`) before the *first* login/GitHub-OAuth-start action when `server.startsWith(ApiConstants.HTTP_SCHEME)` (`LoginViewModel.kt:122-127,135-140`; dialog in `LoginScreen.kt:114-121`) — the password send over `http://` is a user choice, not silent. Traffic *after* that point (ongoing bearer-token requests, background token refresh) has no equivalent warning — tracked separately, `docs/revisit.md` #32 | Not a distinct concern from the already-accepted NETWORK cleartext deviation; the login-time warning is a real, if partial, bound worth recording precisely rather than assuming "no warning exists" |
| MASVS-AUTH-2 / MASVS-AUTH-3 | No local authentication (app lock / biometric gate) and no step-up auth for sensitive operations anywhere in the codebase | N/A by construction — confirmed by `grep -rln 'biometric\|Biometric\|BiometricPrompt\|androidx.biometric'` across all source sets and `gradle/libs.versions.toml`, all empty | Both controls only apply if such a mechanism exists; this app has none, and nothing in scope calls for one |
| MASVS-PLATFORM-1 | `MainActivity` (`androidApp/src/main/AndroidManifest.xml:22-30`) is the only `exported="true"` component in the merged manifest set | Confirmed by grepping every `AndroidManifest.xml` in the repo (`androidApp`, `composeApp`, `core/logger`) for `exported`/`intent-filter`/`<provider>`/`<service>`/`<receiver>` — only `androidApp/src/main/AndroidManifest.xml` has any, and `MainActivity`'s only `intent-filter` is the plain `MAIN`/`LAUNCHER` launch pair, no deep-link scheme/host. `MainActivity.kt` never reads `intent.extras` or `intent.data`, so even the launch intent carries no attacker-controlled input into the app. The manifest's `FileProvider` (`:34-40`, `exported="false"`, `grantUriPermissions="true"`) is not IPC-reachable by another app without an explicit granted URI, and `grep -rln 'FileProvider\|getUriForFile'` across all Kotlin source finds no call site at all — it is declared but never invoked | Minimal IPC surface by construction, not by omission — nothing to lock down further |
| MASVS-CODE-1 | `minSdk = 24` (`gradle/libs.versions.toml:22`, Android 7.0/2016) | No documented rationale anywhere in the repo — `git log --follow -p` on `libs.versions.toml` shows the value has never changed since the project's current form (`1ae21af2`, 2025-06-09); no README/docs line explains the floor. Confirmed a deliberate reach decision by its stability, not an oversight caught mid-change | A wide device-support floor is a reasonable product choice for a FOSS client with no attacker-relevant platform-security gap named against it; recorded so the floor is a decision on record, not silence |
| MASVS-CODE-2 | No forced update on either flavour; **Gplay** prompts a Play In-App Update (`AppUpdateCheckerImpl.kt`, `androidApp/src/gplay/...`) using `AppUpdateType.FLEXIBLE` — a dismissible nudge, not a blocking `IMMEDIATE` update; **F-Droid** has no update-check mechanism at all (confirmed: no equivalent file under `androidApp/src/fdroid/`) | Gplay: `checkAndRequestUpdate`/`checkUpdateStateOnResume` only ever request `FLEXIBLE`, never `IMMEDIATE` — a user can keep using an outdated build indefinitely. F-Droid: update delivery is entirely up to the F-Droid client/repo, outside this app's control | Standard for both distribution channels — F-Droid has no update-enforcement API to call, and a self-hosted client forcing updates against a Play flexible-update dismiss would be unusual UX; no vendor asset here that a stale client puts at risk beyond the user's own account |
| MASVS-CODE-3 | `renovate.json` — Gradle-ecosystem dependency updates via Renovate (active: `git log` shows regular `renovate[bot]` commits) | **Fixed this task**: added `"osvVulnerabilityAlerts": true` to `renovate.json` — Renovate now opens PRs when a dependency in the version catalogue has a known OSV.dev advisory, independent of GitHub's own Dependabot alerts. See Notes for why a `.github/dependabot.yml` (the plan's original assumption) was not added instead | Renovate was already handling version-bump PRs before this task (missed by the plan's own scoping grep, which didn't include `renovate`); adding Dependabot version-update config alongside it would create duplicate/conflicting PRs for the same bumps. `osvVulnerabilityAlerts` is the self-contained fix for the actual gap (nothing checked the catalogue against an advisory feed) without that conflict |
| MASVS-CODE-4 | Ktor JSON deserialization (`KmpNetworkModule.kt:39-44`): `isLenient = true`, `ignoreUnknownKeys = true`, `explicitNulls = false` on the app's real HTTP client (not just `tools/seed`'s) | A malformed or evolving server response (missing field, extra field, unexpected null) fails soft rather than crashing deserialization. Content itself (task descriptions, wiki pages, comments) renders through `com.mikepenz:multiplatform-markdown-renderer` (`MarkdownTextWidget.kt`) — a Compose-native CommonMark renderer, not a WebView/HTML sink — so there is no script-execution/XSS surface from server-supplied markdown the way there would be with `WebView.loadData` | Standard, safe deserialization posture; markdown-to-Compose rendering has no HTML/JS execution path to sandbox |
| MASVS-PRIVACY-1 | Manifest declares only `INTERNET` and `ACCESS_NETWORK_STATE` (`androidApp/src/main/AndroidManifest.xml:5,8`) | Both confirmed actually used, not dead declarations: `INTERNET` by the Ktor/OkHttp client (every API call) and, on Gplay, by Crashlytics' own upload path; `ACCESS_NETWORK_STATE` by `ConnectivityManagerNetworkMonitor` (`androidApp/.../data/ConnectivityManagerNetworkMonitor.kt`) and its JVM counterpart (`NetworkMonitorImpl.jvm.kt`) for the offline-state banner. No unused permission, no location/contacts/storage/camera permission requested anywhere | Minimal permission surface by construction |
| MASVS-PRIVACY-2 | No advertising ID, analytics SDK, or device-fingerprinting call anywhere | Confirmed by grep: `grep -rln 'AdvertisingId\|ad_id\|GoogleAdId\|Installations\.getId\|FirebaseAnalytics\|firebase-analytics'` across all source sets and `gradle/libs.versions.toml` returns nothing. Crashlytics' own internal installation ID (see MASVS-PRIVACY-3 row) is not a user-identifying ID and is scoped to crash-diagnostic use only, per `PRIVACY_POLICY_GPLAY.md` | Nothing in the app attempts to identify or fingerprint the user beyond the Taiga account itself |
| MASVS-PRIVACY-3 | Crash reporting differs by build flavour and is disclosed. **Gplay**: real Firebase Crashlytics (`androidApp/src/gplay/.../CrashReporterImpl.kt`), collection **on by default** — `TaigaSessionStorage.crashReportingEnabled` defaults to `true` when the DataStore key is unset (`TaigaSessionStorage.kt:148`, `prefs[CRASH_REPORTING_ENABLED_KEY] ?: true`) — with a real toggle in Settings → Interface → Privacy (`SettingsInterfaceViewModel.kt:67-71`) wired to `Crashlytics.isCrashlyticsCollectionEnabled` via `TaigaApp.kt:59-61`. **F-Droid, JVM/desktop, and iOS** all use a pure no-op `CrashReporterImpl` (`isAvailable = false`, every method `= Unit`) — confirmed no crash data leaves the device on those three | Disclosed in `PRIVACY_POLICY_GPLAY.md` ("Firebase Crashlytics" section): names what's collected (stack traces, device model/OS/app version, approximate timestamp, an installation ID not linked to the Taiga account), states credentials/tokens/project content are excluded, links Firebase's own privacy docs, and names the in-app opt-out path by its exact menu location. `PRIVACY_POLICY.md` (F-Droid/base) has no Crashlytics section at all, matching the real no-op build. The default-on posture is opt-out, not opt-in — there is no first-run consent prompt gating the first crash upload — but this is the standard Crashlytics/Play posture and is stated plainly in the policy rather than hidden | A disclosed, flavour-scoped, revocable collection with a stated bound is a deviation from "no telemetry," not an unbounded one |

## Open

| Control | Finding | Where | Severity |
|---|---|---|---|
| MASVS-STORAGE-1 / MASVS-CRYPTO-2 | **iOS only** (Android fixed this task, see Notes; JVM/desktop is outside MASVS). Session `token` and `refresh_token` are stored as plaintext `stringPreferencesKey`s in an unencrypted DataStore `Preferences` file at `NSDocumentDirectory/auth_storage.preferences_pb` — no Keychain-backed wrapper. `PlatformStorageModule`'s iOS actual still wires a plain `AuthStorageImpl(createAuthDataStore(), NoopTokenCipher())`, same as JVM/desktop. The fix shape is the same one Android just got, ported to Keychain instead of `Cipher`/`KeyGenParameterSpec`: store the token as a Keychain item (`kSecAttrAccessibleWhenUnlockedThisDeviceOnly`) instead of inside the DataStore file, with a plaintext-fallback read for the migration window. | `core/storage/src/iosMain/kotlin/com/grappim/taigamobile/core/storage/di/StorageModule.ios.kt:31-34`; `core/storage/src/commonMain/.../auth/TokenCipher.kt` (the `TokenCipher` seam Android now uses) | Medium-High — bearer token grants full account access until it expires/is revoked server-side; refresh_token extends that. Deliberately scoped out of this task (user chose "Android only, iOS deferred" over a three-platform crypto change in one review task) — a real, sized fix, not a leftover. |
| MASVS-AUTH-1 | **Android only** (iOS/JVM never reach this code — `isGithubOAuthSupported()` returns `false` there, button hidden). GitHub OAuth runs inside an in-app `WebView` (`GithubOAuthWebViewDialog.android.kt:22-38`) with `javaScriptEnabled = true` and `domStorageEnabled = true`, hosting GitHub's real login form under full app control — the RFC 8252 "embedded user-agent" anti-pattern, which fails regardless of configuration. Two bounds exist (no `addJavascriptInterface`/JS bridge anywhere in the codebase; the flow only executes on Android), and two gaps beyond what the review plan assumed: navigation is not host-restricted (`shouldOverrideUrlLoading` only inspects for `code`/`error` query params, any other URL falls through to `return false` and loads), and the `WebView` never clears cookies/is never destroyed on dismiss, so GitHub's session cookie persists in the app's shared `CookieManager` store after the dialog closes (not tied to app logout). This is a deliberate, documented tradeoff, not an oversight — a Custom Tabs + loopback-redirect version was built and reverted in the same PR (commit `4236a2ef`) because GitHub OAuth Apps support exactly one registered callback URL, already used by Taiga's web app; repointing it would break the web login, and a second OAuth App is a server-admin change outside this codebase. | `feature/login/ui/src/androidMain/kotlin/com/grappim/taigamobile/feature/login/ui/GithubOAuthWebViewDialog.android.kt:22-38`; reverted alternative documented (now marked superseded) in `docs/features/github-auth/plan.md` | Medium — full GitHub credentials are entered into a form the app controls; exploiting it needs the app itself compromised (malicious update) since there's no JS bridge today. Proposed near-term fix (host-allowlist the WebView navigation, clear cookies on dismiss) tracked in `docs/revisit.md` #34, not fixed inline — deferred because correctly scoping a host allowlist for GitHub's SSO/2FA chain isn't safely verifiable from source alone in a repo with no Android unit-test source set. Full RFC 8252 compliance is blocked on the external OAuth App constraint, not something this task can close. |
| MASVS-PLATFORM-2 | Same `GithubOAuthWebViewDialog.android.kt:22-38` code as the MASVS-AUTH-1 row above — this is the WebView-mechanics half of that finding (JS enabled, no navigation host-restriction, cookies never cleared), reviewed here as its own control rather than restated. No new gap found beyond what AUTH-1 already names. | See MASVS-AUTH-1 row above | Medium — same as MASVS-AUTH-1; tracked as one finding, not duplicated, fix tracked in `docs/revisit.md` #34 |
| MASVS-PLATFORM-3 | No screen sets `FLAG_SECURE` anywhere in the codebase (`grep -rn 'FLAG_SECURE' --include=*.kt .` empty; `MainActivity.kt` never calls `window.setFlags`), so the whole single-`Activity` app can appear in the recents-list thumbnail. The concrete instance that matters: `LoginScreen.kt`'s password field has a show/hide toggle (`state.isPasswordVisible`, `VisualTransformation.None` vs. `PasswordVisualTransformation()`, `LoginScreen.kt:190-219`) — if the user taps "show password" (`Icons.Filled.VisibilityOff`, `:211-218`) and then backgrounds the app (switches apps, receives a call) while it's visible, Android's recents snapshot captures the plaintext password on-screen. No other credential-reveal UI exists to check against — `grep -rln 'PasswordVisualTransformation'` finds only `LoginScreen.kt`. | `feature/login/ui/src/commonMain/kotlin/com/grappim/taigamobile/feature/login/ui/LoginScreen.kt:190-219` (password field); no `FLAG_SECURE` call site anywhere to point at | Low-Medium — requires the user to actively reveal the password *and* background the app at that exact moment; the recents thumbnail is local to the device (not synced/uploaded), so the practical exploit needs physical/local access to an unlocked device. A one-line fix (`window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, ...)` in `MainActivity.onCreate`, app-wide since it's a single-Activity app) was not applied inline — it's a UX tradeoff (blocks all screenshots/screen-recording app-wide, not just the login screen) worth a deliberate choice rather than a silent default change; written up in `docs/revisit.md` #35 |
| MASVS-PRIVACY-4 | **iOS only** (Android and JVM/desktop both confirmed correct — see Notes). `AuthStateManager.logoutSuspend()` (`core/storage/.../auth/AuthStateManager.kt:26-32`) clears filters, `TaigaSessionStorage`, and `AuthStorage` unconditionally, then calls `databaseWrapper.clearAllTables()` → `TaigaDB.clearAllTablesKmp()` (`DatabaseWrapperImpl.kt:14-16`). iOS's `actual` for that function is still `actual suspend fun TaigaDB.clearAllTablesKmp() = Unit` (`core/storage/src/iosMain/.../db/TaigaDBExt.ios.kt:3`) — a no-op stub, deliberately left alone by `docs/desktop/linux-release-plan.md` task 7, which fixed the identical bug for JVM/desktop but explicitly scoped iOS out. Effect: on iOS, logging out clears the session token and app preferences but leaves the Room cache (projects, sprints, work items) fully populated — the next account to log in on that device sees the previous account's cached project data rendered until each screen's own cache is overwritten by a fresh fetch. | `core/storage/src/iosMain/kotlin/com/grappim/taigamobile/core/storage/db/TaigaDBExt.ios.kt:3`; correct pattern already proven on JVM at `core/storage/src/jvmMain/kotlin/com/grappim/taigamobile/core/storage/db/TaigaDBExt.jvm.kt:3-7` (three-DAO `deleteAll()`) | Medium — needs a shared/multi-account device (the profile this control exists for); no credential is exposed (that's cleared correctly), only cached project/task content, but that content can itself be sensitive (task titles, comments, assignee names). Fix is a straight port of the JVM fix: implement the same three `deleteAll()` DAO calls in the iOS actual — no new abstraction needed, the `expect`/DAO plumbing already exists. Not fixed inline (this task is a documentation review, no Room-backed iOS test infra in this repo to verify it beyond `jvmTest`/`compileKotlinIosArm64`) — written up in `docs/revisit.md` #37 |
| MASVS-CODE-4 | Markdown-rendered server content (task descriptions, comments, wiki pages, and the "URL"-type custom field) can embed a link whose scheme is not `http`/`https`. Confirmed and **fixed this task** for the one directly-owned call site: `CustomFieldUrlItemWidget` (`CustomFieldsWidget.kt`, was `:452-464`) passed `item.currentValue` — free text any project collaborator with edit permission can set — straight to `uriHandler.openUri()` behind only a try/catch for `IllegalArgumentException`. On Android, `LocalUriHandler.openUri` launches an implicit `ACTION_VIEW` intent with the string as-is, so an untrusted collaborator (not the server admin — any teammate with edit rights on the task) could set a custom field to a crafted `intent://…`/other-scheme URI and get it launched with one tap from a teammate viewing the task. Fix: added an `http://`/`https://` scheme allowlist before calling `openUri`; anything else is refused and logged (`LogPriority.WARN`), not launched. **Not fixed, still open**: the same class of risk for links *inside* rendered markdown (task descriptions/comments/wiki) — `com.mikepenz:multiplatform-markdown-renderer`'s link-click handling was not decompiled/confirmed in this task (only its compiled classes were inspected, no `LinkClickListener`/override class found, consistent with — but not proof of — it delegating to Compose's default `LinkAnnotation.Url` behaviour, which is `LocalUriHandler.openUri` with no scheme check); and `AttachmentsWidget.kt:160`'s `uriHandler.openUri(attachment.url)`, lower risk since that URL is server/API-constructed rather than free text a collaborator types, but not scheme-checked either. | Fixed: `feature/workitem/ui/.../customfields/CustomFieldsWidget.kt` (URL custom field). Open: markdown link-click path inside `com.mikepenz:multiplatform-markdown-renderer` (used from `MarkdownTextWidget.kt`); `feature/workitem/ui/.../AttachmentsWidget.kt:160` | Low-Medium — same profile as the fixed instance: needs a malicious/compromised project collaborator (not the server operator) plus one tap from the viewing user; Android's implicit-intent dispatch is a real escalation path (deep-linking into another installed app) beyond what a plain browser hyperlink can do. A correct app-wide fix (wrap `LocalUriHandler` once with a scheme-validating decorator, rather than patching each call site) is a cross-cutting change bigger than this task's scope — written up in `docs/revisit.md` |

## Needs a device or an APK

| Control | Check | Why source can't answer it |
|---|---|---|
| MASVS-STORAGE-2 | Whether the release APK's backup exclusions (`data_extraction_rules.xml` / `backup_rules.xml`, added task 0) actually keep the auth DataStore file out of a real `adb backup` / cloud backup / D2D transfer | Needs a built release APK and a device to run `adb backup` / trigger Android's backup agent and inspect the resulting archive |
| MASVS-STORAGE-1 / MASVS-CRYPTO-2 | Whether `AndroidKeystoreTokenCipher`'s AES key is actually hardware-backed (TEE/StrongBox) as requested via `KeyGenParameterSpec`, not just requested | Needs a device — hardware enforcement isn't verifiable from source; also, there is no Android unit-test source set in this repo (CLAUDE.md, by design), so this class has no automated test at all, only the manual review below |
| MASVS-CRYPTO-2 | Whether the plaintext→ciphertext migration (existing installs' unprefixed token gets re-encrypted on next `setAuthCredentials`) actually fires for real installed users, vs. some cohort staying on plaintext indefinitely | Needs telemetry or a real upgrade test from a pre-cipher build — the migration is exercised in `AuthStorageImplTest` (jvmTest, `NoopTokenCipher`/`FakeTokenCipher`) but that proves the code path, not real-world convergence time |
| MASVS-NETWORK-1 | Whether `CompositeTrustManager`'s per-certificate pin actually holds at the TLS layer against a live regenerated leaf (regenerate the cert on an already-trusted host, restart, confirm the app objects and re-offers TOFU rather than connecting silently) | Source confirms the pin *key* is `(host, sha256Fingerprint)` and a unit test proves the storage-layer lookup rejects a fingerprint mismatch, but the full handshake-level behaviour — does OkHttp/JSSE actually re-invoke `checkServerTrusted` and surface `UntrustedCertificateException` correctly through to the UI on a live regenerated cert — needs a device or a throwaway TLS front for the server |
| MASVS-AUTH-1 | Whether a real GitHub OAuth flow (including org SSO/2FA redirects) ever navigates the `WebView` to a host outside `github.com` before the `code`/`error` param appears — needed to scope a correct host allowlist for `docs/revisit.md` #34's proposed fix | Needs a live GitHub OAuth login on a device against real org SSO configurations; not enumerable from source since GitHub controls the redirect chain |
| MASVS-PLATFORM-3 | Whether the revealed password on `LoginScreen` actually shows up in a real recents-list screenshot when the app is backgrounded mid-reveal | Needs a device: reveal the password, switch apps via the recents button, inspect the thumbnail — not verifiable from source, only inferable from the absence of `FLAG_SECURE` |
| MASVS-CODE-3 | Whether Renovate's newly-enabled `osvVulnerabilityAlerts` actually opens a PR against a real known-vulnerable dependency in the catalogue | Needs a live Renovate run against this repo (or a deliberately-pinned vulnerable test dependency) — not verifiable from a static config read. Separately confirmed via `gh api repos/Grigoriym/TaigaMobileNova/vulnerability-alerts` → 404 "Vulnerability alerts are disabled": GitHub's own native Dependabot vulnerability alerts are OFF at the repo-settings level. That's an optional, independent lever (repo Settings → Code security) the user could also flip, not required now that OSV coverage is configured, and not something this task changed |
| MASVS-CODE-4 | Whether `com.mikepenz:multiplatform-markdown-renderer`'s link-click handling actually calls `LocalUriHandler.openUri()` with no scheme check, as suspected but not confirmed (see `docs/revisit.md` #36) | Needs either decompiling the library's Android/JVM classes past what class-name inspection showed, or a device test: render a markdown link with a non-http(s) scheme, tap it, observe whether an intent launches |

## Notes

- **Logs**: `grep -rnE '(logcat|Timber|println|NSLog).{0,120}(token|apiKey|password|secret|cookie|Authorization)'`
  across all source sets returns nothing — no call site logs the session token, refresh token, or
  password. Verified statically.
- **`allowBackup` inversion — fixed, not left as a finding.** The release manifest
  (`androidApp/src/main/AndroidManifest.xml`) set `android:allowBackup="true"` with no
  `dataExtractionRules`/`fullBackupContent` at all, while the **debug** manifest
  (`androidApp/src/debug/AndroidManifest.xml:6-8`) was the one that turned backup off entirely —
  backwards, since debug is the build with nothing worth taking. Fixed this task: release now keeps
  `allowBackup="true"` (other cached data still benefits from backup/restore and device transfer)
  but adds `android:dataExtractionRules="@xml/data_extraction_rules"` (API 31+) and
  `android:fullBackupContent="@xml/backup_rules"` (API 24-30, matching `minSdk`) excluding the auth
  DataStore file and its legacy `SharedPreferencesMigration` source (`auth_storage.xml`) from cloud
  backup and device transfer by name. Verified the merged release manifest picks up both attributes
  (`processFdroidReleaseMainManifest` output). Per the skill's Step 4, a fixed finding leaves the
  register — this note exists only to explain why MASVS-STORAGE-2 has no Open row for backup.
- **No application-level cryptography existed before this task.** `grep -rl
  'Keystore\|SecretKey\|Cipher\.getInstance\|KeyGenParameterSpec'` and `grep -rl
  'Keychain\|kSecAttr\|SecItem'` across all source sets both returned nothing — no key material in
  source, build config, or `gradle/libs.versions.toml`. The `sha256Fingerprint` calls in
  `CompositeTrustManager` (`core/api`) are `MessageDigest`-based cert-pin hashing, a MASVS-NETWORK
  concern (task 2), not application data encryption — noted, not counted as a CRYPTO finding.
- **MASVS-STORAGE-1's plaintext-token finding was fixed for Android this task**, not left open. A
  `TokenCipher` seam was added (`core/storage/.../auth/TokenCipher.kt`, interface + `NoopTokenCipher`
  passthrough) and `AuthStorageImpl` now runs every read/write through it
  (`core/storage/.../auth/AuthStorage.kt`). Android's actual is `AndroidKeystoreTokenCipher`
  (`core/storage/src/androidMain/.../auth/AndroidKeystoreTokenCipher.kt`): AES/GCM with a
  Keystore-resident key (`KeyGenParameterSpec`, `BLOCK_MODE_GCM`, no padding, a fresh random IV per
  encryption via `cipher.iv` — never reused), ciphertext stored as `"v1:" + base64(iv + ciphertext)`.
  **Production migration**: `decrypt()` returns any value without the `"v1:"` prefix unchanged
  (a pre-cipher plaintext token from an already-installed user), so existing sessions keep working
  without a forced logout; the value gets encrypted in place the next time `setAuthCredentials` runs
  (i.e. the next token refresh), not on every read, to avoid a write-on-read side effect across the
  `tokenFlow`/`refreshTokenFlow` `Flow.map` collectors. A `"v1:"`-prefixed value that fails to decrypt
  (corrupt data, or a Keystore key invalidated by e.g. biometric enrollment change) returns `""`
  rather than propagating garbage as a bearer token — the user is treated as logged out instead of
  the app sending a broken `Authorization` header. **JVM/desktop and iOS both still use
  `NoopTokenCipher`** (identity passthrough) — JVM/desktop because it's outside MASVS scope by the
  skill's own Step 0, iOS by explicit choice this task (see the Open row above) rather than by
  oversight. Verified: `./gradlew :core:storage:jvmTest`, `ktlintCheck`, `koverXmlReport`/`:koverVerify`
  all green; `AndroidKeystoreTokenCipher` itself has no automated coverage (no Android unit-test
  source set in this repo) and is listed in "Needs a device" above.
- **iOS has no trust-manager equivalent at all — a functional gap, not a MASVS-NETWORK finding.**
  `PlatformHttpClientEngine.ios.kt:7` is `Darwin.create()`, ignoring the `trustedCertStorage`
  parameter it's handed — no `CompositeTrustManager` port, no Keychain-backed cert store wired in.
  Consequence: an iOS user pointed at a self-signed/self-hosted server fails closed (`NSURLSession`'s
  default TLS validation rejects the cert; no silent bypass) rather than being offered TOFU — this is
  the *safe* direction, so it is not a security violation, and MASVS-NETWORK-2's pinning control is
  already N/A here (user-supplied server). It is a real feature-parity gap: the TOFU-with-revoke flow
  only exists on Android/JVM. A side effect worth naming plainly: `TrustedCertificatesScreen`
  (`feature/settings/ui/.../trustedcerts/`) lives in `commonMain` and is reachable on iOS, but nothing
  ever writes to `TrustedCertStorage` there, so the screen is permanently empty on that platform —
  not misleading, just dead UI. Tracked as a follow-up (`docs/revisit.md` #33), not fixed in this
  review.
- **Cleartext token exposure has a one-time warning at login, not an ongoing one.** `AuthHeaderPlugin`
  attaches the bearer token to every request regardless of `URLProtocol`, and cleartext is permitted
  app-wide with no `network_security_config.xml` scoping. The MASVS-AUTH review (task 3) found
  `LoginViewModel` does show an "Unencrypted connection" confirmation before the first credential
  submission over `http://` — so this isn't silent at the point of login — but nothing warns again for
  the bearer-token traffic that follows. Accepted as a deviation (self-hosted LAN instances speak plain
  HTTP), with the remaining gap tracked as a follow-up (`docs/revisit.md` #32, corrected this task)
  rather than fixed in this review.
- **MASVS-AUTH-1, primary login channel**: confirmed username/password and LDAP login both go through
  `AuthRepositoryImpl.auth` → `AuthApiImpl.auth` (`POST auth`) — the same Ktor client/channel already
  characterized under MASVS-NETWORK, not a separate credential-transport mechanism. Recorded as an
  Accepted deviation cross-referencing the NETWORK section rather than a new finding.
- **MASVS-AUTH-1, GitHub OAuth WebView**: this is the finding named by the review plan's task 3 "Why."
  Confirmed with `file:line` (`GithubOAuthWebViewDialog.android.kt:22-38`) and resolved as an Open
  finding rather than an Accepted deviation, because the two gaps beyond the plan's framing (navigation
  not host-restricted; `WebView` cookies never cleared on dismiss) are real and not yet bounded, even
  though the choice of `WebView` over Custom Tabs itself is a documented, deliberate tradeoff (GitHub
  OAuth Apps' single-callback-URL limit — see the Open table and `docs/revisit.md` #34 for the full
  history, including the reverted Custom Tabs/loopback implementation from the same original PR). The
  stale plan for that reverted approach, `docs/features/github-auth/plan.md`, is marked Superseded this
  task rather than left to be mistaken for the current design.
- **MASVS-AUTH-2/MASVS-AUTH-3**: confirmed N/A, not skipped — `grep -rln
  'biometric\|Biometric\|BiometricPrompt\|androidx.biometric'` across all source sets and the version
  catalogue returns nothing. No app lock, no step-up auth anywhere in the codebase.
- **`FileProvider` is declared but unused.** `androidApp/src/main/AndroidManifest.xml:34-40`
  registers `androidx.core.content.FileProvider` (`exported="false"`, `grantUriPermissions="true"`,
  paths `.` for both `files-path` and `external-files-path` in `my_paths.xml` — the whole app-private
  directory) but `grep -rln 'FileProvider\|getUriForFile'` across all Kotlin source finds no call
  site anywhere. Not a MASVS-PLATFORM-1 finding — `exported="false"` means it isn't IPC-reachable by
  another app without an explicit granted URI, and nothing in this app ever grants one — just dead
  manifest config, noted for hygiene rather than as a security row.
- **MASVS-PLATFORM-2, GitHub OAuth WebView**: same finding as MASVS-AUTH-1's Open row
  (`GithubOAuthWebViewDialog.android.kt:22-38`) — reviewed here for the WebView-mechanics control
  specifically (JS/DOM storage enabled, no host-restricted navigation, cookies never cleared), cross-
  referenced rather than duplicated. See the Open table and `docs/revisit.md` #34 for the fix.
- **MASVS-CODE-3, why `.github/dependabot.yml` was not added.** The review plan's task 5 assumed a
  plain gap (no dependency-catalogue vulnerability check at all) and proposed a Gradle-ecosystem
  `dependabot.yml` as the fix. Scoping this task found that assumption was stale: `renovate.json`
  already exists and Renovate is actively running (`git log` shows regular `renovate[bot]` version-bump
  commits) — missed by the plan's own scoping grep, which checked for `dependabot`/`osv`/`snyk` but not
  `renovate`. Adding Dependabot version-update config alongside an already-active Renovate would create
  duplicate, conflicting PRs proposing the same bumps. The actual gap — nothing checks the catalogue
  against a vulnerability advisory feed — was closed instead by adding `"osvVulnerabilityAlerts": true`
  to the existing `renovate.json`, which makes Renovate itself open PRs against OSV.dev advisories, with
  no dependency on GitHub's native Dependabot alerts (confirmed separately OFF at the repo-settings
  level — see the Needs-a-device row).
- **MASVS-PRIVACY-4, logout data clearing — confirmed per platform, not assumed.** Android:
  `core/storage/src/androidMain/.../db/TaigaDBExt.android.kt` is
  `actual suspend fun TaigaDB.clearAllTablesKmp() = clearAllTables()`, a real Room call — correct, and
  was already correct before `docs/desktop/linux-release-plan.md` task 7 touched anything. JVM/desktop:
  that task 7 found and fixed the identical `= Unit` stub bug, replacing it with three DAO
  `deleteAll()` calls (`core/storage/src/jvmMain/.../db/TaigaDBExt.jvm.kt:3-7`) — outside MASVS scope
  (no MASVS target), confirmed fixed anyway since the register should say what's actually true.
  **iOS still has the `= Unit` stub** (`core/storage/src/iosMain/.../db/TaigaDBExt.ios.kt:3`) — that
  task's own Result note says the signature changed (`suspend` added, to keep the `expect`/`actual`
  match) but the body was deliberately left alone, out of that task's scope. Recorded as an Open
  finding here (see Open table) and written up in `docs/revisit.md` #37, since the fix is a proven,
  three-line port of the JVM version, not a design question.
- **MASVS-PRIVACY-3, crash-reporting posture confirmed per flavour, not assumed uniform.** Gplay's
  `CrashReporterImpl` (`androidApp/src/gplay/.../data/CrashReporterImpl.kt`) wraps real
  `Firebase.crashlytics`; F-Droid, JVM/desktop, and iOS all use an identical-shaped pure no-op
  (`isAvailable = false`, every method `= Unit`) — read all four files directly rather than trusting
  the plan's framing. Collection is on by default (`TaigaSessionStorage.crashReportingEnabled` defaults
  to `true` when unset) but disclosed in `PRIVACY_POLICY_GPLAY.md` with a named opt-out path (Settings →
  Interface → Privacy), which `SettingsInterfaceViewModel`/`TaigaApp.kt` confirm is wired to
  `Crashlytics.isCrashlyticsCollectionEnabled` for real, not just a UI toggle with no backing effect.
  Recorded as an Accepted deviation, not a finding — default-on-but-disclosed-and-revocable is a
  documented bound, not silence.
- **MASVS-PRIVACY-1/2 both confirmed, not assumed trivial.** Permissions: `INTERNET` and
  `ACCESS_NETWORK_STATE` are the only two declared, and both have a real call site (grep-confirmed, see
  Accepted table). Identification: grepped for advertising-ID/analytics-SDK/fingerprinting call sites
  across all source sets and the version catalogue — none exist.
- **MASVS-CODE-4, `CustomFieldsWidget.kt` URL-open finding — fixed this task, not left open.** The
  "URL"-type custom field's open action (`CustomFieldUrlItemWidget`, was `CustomFieldsWidget.kt:452-464`)
  passed free-text collaborator input straight to `uriHandler.openUri()` with no scheme check — an
  implicit Android `ACTION_VIEW` intent with attacker-chosen data. Fixed by allowlisting `http://`/
  `https://` before the call; anything else is logged and refused. The same risk class in two other
  call sites (`AttachmentsWidget.kt:160`, and markdown-rendered links in task descriptions/comments/
  wiki) was found but not fixed — see the Open table and `docs/revisit.md` #36 for why (needs an
  app-wide `LocalUriHandler` wrapper, not a per-call-site patch, and the markdown renderer's actual
  link-click wiring wasn't confirmed).

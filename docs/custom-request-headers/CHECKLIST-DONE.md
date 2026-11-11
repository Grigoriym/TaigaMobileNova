# Custom request headers — done steps

## 1. ✅ Storage + validator

`core:storage` `customheaders/`: `CustomHeader`, `CustomHeaderError`, `CustomHeaderValidator`,
`CustomHeadersStorage` + DataStore impl (encrypted blob, cached `currentHeaders`), per-platform
`@Single` providers (Android Keystore alias of its own, Noop on JVM/iOS). `FakeCustomHeadersStorage`
in `:testing`. Tests: validator rules incl. `Authorization`/`User-Agent` reserved; storage round
trip, empty list removes the key, bad blob reads as none, survives logout.
Verify: `./gradlew :core:storage:jvmTest :testing:compileKotlinJvm`
Note: validator is a plain `object` (stateless, no DI needed). Separate DataStore file
(`custom_headers_storage`), so `AuthStateManager`'s logout can't reach it — "survives logout" holds
by construction, no test. Kit `FakeSecretCipher` only prefixes `ENC:`; the test asserts that
prefix. Next: step 2.


## 2. ✅ `CustomHeaders` Ktor plugin

`core:api`: plugin on `SendingRequest`, `isServerOrigin`, `sanitizeCustomHeaders` on both
`Logging` installs, installed on both clients in `KmpNetworkModule`. Tests with `MockEngine`:
sent on server origin, not on other host/scheme/port, stripped on a cross-host redirect hop,
values sanitized in logs.
Verify: `./gradlew :core:api:jvmTest` + `KoinGraphTest`
Note: the plugin reads the server from `BaseUrlProvider` (already injected into both clients), not
`ServerStorage` — same origin. Debug-only gap: with `DebugLocalhostPlugin` rewriting `localhost`
to the debug host, the request origin no longer matches the stored `localhost` server, so headers
aren't sent in that mode. Next: step 3.


## 3. ✅ Proxy rejection

`isProxyRejection(status, wwwAuthenticate, location, requestHost)` in `core:api`;
`TokenRefreshPlugin` skips refresh/logout for it; `ErrorMappingPlugin` throws
`NetworkException(ERROR_PROXY_AUTH)`; `getErrorMessage` maps it to a new string. Tests for the
predicate (Taiga's `Bearer` 401 is *not* a proxy rejection), the refresh plugin not logging out,
and the mapping.
Verify: `./gradlew :core:api:jvmTest :utils:ui:jvmTest`
Note: `HttpRedirect` is installed before user plugins (Ktor 3.6.1 `HttpClient.kt`), so it is the
**outermost** `HttpSend` interceptor and every redirect hop re-enters `ErrorMappingPlugin` and
`HostSelectionPlugin` — an off-host GET redirect would be rewritten back onto the server and loop.
`ErrorMappingPlugin` therefore checks each hop's response and throws `ERROR_PROXY_AUTH` before the
redirect is followed. `TokenRefreshPlugin` also keeps the session when the *refresh call itself*
is stopped by the proxy (`ERROR_PROXY_AUTH` from the auth client). Existing refresh tests faked a
bare 401; their helper now sends Taiga's real `Bearer realm="api"` challenge. `CustomHeadersConfig`
moved to its own file for detekt's `MatchingDeclarationName`. Next: step 4.


## 4. ✅ Images

Android `ImageLoaderProvider`: OkHttp interceptor adding origin-scoped custom headers. Desktop and
iOS: install a singleton image loader whose fetcher does the same (find where Coil's default is
picked up first). Off-origin images get nothing.
Verify: both Android flavors compile, desktop `:composeApp:run` shows avatars against the step 7
fixture (or defer the visual check to step 7).
Note: one shared fetcher instead of an OkHttp interceptor on Android —
`customHeadersNetworkFetcher` (`composeApp` commonMain, `images/`) is Coil's Ktor fetcher with the
step 2 `CustomHeaders` plugin installed, so the per-hop origin check is the already-tested one.
Android's `ImageLoaderProvider` always adds it (passing an OkHttp engine carrying
`DebugLocalHostImageManager` when the debug host is set; `coil-network-okhttp` dropped from
`androidApp`); desktop sets it via `SingletonImageLoader.setSafe` after `startKoin`, iOS via
`setSingletonImageLoaderFactory`. Both flavors, desktop and iOS simulator compile; `KoinGraphTest`,
ktlint, detekt green; desktop launches cleanly. Avatar check deferred to step 7 (needs a logged-in
session and the proxy fixture). Next: step 5.


## 5. ✅ Custom headers screen + Settings entry

`feature:settings:ui` `customheaders/`: NavDestination, State, ViewModel (rows, per-row error,
Save enabled only when valid and changed, disabled offline), Screen; Settings row; nav graph entry.
ViewModel tests incl. failure path.
Verify: `./gradlew :feature:settings:ui:jvmTest ktlintCheck`
Note: ported from wallosmobile's `customheaders/` screen. Save is a top-bar `TopBarActionTextButton`
(this app's edit-screen convention, as in `ProjectDetailsScreen`), enabled only when
`isSaveEnabled && !LocalOfflineState.current`. Unlike wallosmobile a failed save shows a snackbar
(`custom_headers_save_failed`) via `SnackbarDelegate`, logged at ERROR; rows stay as typed. The
Authorization caveat lives in the screen's description text, not a value hint. The Settings row is
shown on every platform. Driven on `Medium_Phone_API_36.1` (fdroid debug): Save
greyed with no change; `Host` shows the reserved-name error and keeps Save off; a valid header
enables Save, saving turns it off and the row survives a force-stop; airplane mode disables Save;
removing the row and saving persists an empty list.
Full `jvmTest`, detekt and ktlint green. Next: step 6.


## 6. ✅ Login entry

"Custom headers (N)" button on `LoginScreen` → `onCustomHeadersClick` → same destination, wired in
`composeApp`. Count from `CustomHeadersStorage.headers`. Tests for the count.
Verify: `./gradlew :feature:login:ui:jvmTest`
Note: `LoginViewModel` takes `CustomHeadersStorage` and folds `headers` into
`LoginState.customHeadersCount`; the screen shows a `TextButton` under the login buttons —
"Custom headers" at zero, "Custom headers (N)" otherwise (wallosmobile's two strings), disabled
while a login is in flight. `MainNavHost`'s `LoginScreen` call wires it to
`goToCustomHeadersScreen()`, which pushes onto Login's own sub-stack, so plain back returns.
Driven on `Medium_Phone_API_36.1` (fdroid debug, cleared data): label "Custom headers", tap opens
the editor, saving one header and backing out shows "Custom headers (1)", removing it and the
top-bar back arrow return to "Custom headers". Full `jvmTest`, detekt and ktlint green. Next: step 7.


## 7. ✅ Device check against a header-auth proxy

Stand up the fixture from the plan (nginx in front of `localhost:9000` demanding a custom header).
On the emulator and desktop: login fails with the proxy message and does **not** log out an
existing session; add the header from Login → login works; avatars load; header removed in
Settings → next refresh shows the proxy message. Record the fixture command in the plan.
Verify: the above, screenshots.
Note: fixture is `proxy-fixture.conf` (nginx, port 9100), command in the plan; it also rewrites
Taiga's `localhost:9000` media URLs onto the proxy host, otherwise debug Android builds fetch images
from the unproxied port. Driven on `Medium_Phone_API_36.1` (fdroid debug, cleared data, server
`http://10.0.2.2:9100`): login without the header shows the proxy message (proxy log: `/auth` 401);
"Custom headers" → add `X-Proxy-Token: secret` → back shows "Custom headers (1)" and login succeeds;
`admin`'s uploaded avatar loads on Team through the proxy (200 from Coil's Ktor fetcher), Gravatars
load off-origin; header removed in Settings → Issues shows the proxy message with Retry (401s), and
a force-stop + relaunch lands on Dashboard, still logged in. Desktop not GUI-driven (no Xvfb here;
xdotool on the live display was declined) — gregory accepted the emulator run as sufficient, since
desktop shares the same common code and only its loader install differs (step 4: launches cleanly).
Next: step 8.

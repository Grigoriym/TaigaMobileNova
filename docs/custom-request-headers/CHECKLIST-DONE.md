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


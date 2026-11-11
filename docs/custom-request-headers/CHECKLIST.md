# Custom request headers — checklist

**Progress:** 2 / 8 · **Current step:** 3

Plan and rationale: [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Finished steps move to
`CHECKLIST-DONE.md`. All steps commit to `feat/android17-local-network-permission` (PR 468).

## 3. Proxy rejection

`isProxyRejection(status, wwwAuthenticate, location, requestHost)` in `core:api`;
`TokenRefreshPlugin` skips refresh/logout for it; `ErrorMappingPlugin` throws
`NetworkException(ERROR_PROXY_AUTH)`; `getErrorMessage` maps it to a new string. Tests for the
predicate (Taiga's `Bearer` 401 is *not* a proxy rejection), the refresh plugin not logging out,
and the mapping.
Verify: `./gradlew :core:api:jvmTest :utils:ui:jvmTest`

## 4. Images

Android `ImageLoaderProvider`: OkHttp interceptor adding origin-scoped custom headers. Desktop and
iOS: install a singleton image loader whose fetcher does the same (find where Coil's default is
picked up first). Off-origin images get nothing.
Verify: both Android flavors compile, desktop `:composeApp:run` shows avatars against the step 7
fixture (or defer the visual check to step 7).

## 5. Custom headers screen + Settings entry

`feature:settings:ui` `customheaders/`: NavDestination, State, ViewModel (rows, per-row error,
Save enabled only when valid and changed, disabled offline), Screen; Settings row; nav graph entry.
ViewModel tests incl. failure path.
Verify: `./gradlew :feature:settings:ui:jvmTest ktlintCheck`

## 6. Login entry

"Custom headers (N)" button on `LoginScreen` → `onCustomHeadersClick` → same destination, wired in
`composeApp`. Count from `CustomHeadersStorage.headers`. Tests for the count.
Verify: `./gradlew :feature:login:ui:jvmTest`

## 7. Device check against a header-auth proxy

Stand up the fixture from the plan (nginx in front of `localhost:9000` demanding a custom header).
On the emulator and desktop: login fails with the proxy message and does **not** log out an
existing session; add the header from Login → login works; avatars load; header removed in
Settings → next refresh shows the proxy message. Record the fixture command in the plan.
Verify: the above, screenshots.

## 8. Close-out

Changelog stubs if a release is open, `docs/security/masvs.md` note (new stored secret), CLAUDE.md
pointer if a convention emerged, PR 468 title/body updated to cover both features, `finalize`.
Verify: full `./gradlew jvmTest koverXmlReport :koverVerify ktlintCheck detekt`

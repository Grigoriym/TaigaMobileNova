# GitHub OAuth — Loopback Implementation Plan

**Status: SUPERSEDED.** Implemented and then reverted in the same PR that shipped GitHub login
(commit `4236a2ef`, "feat: tg-108 replace loopback with WebView for GitHub OAuth") — GitHub OAuth
Apps support exactly one registered callback URL, and that URL is already the Taiga web app's; the
loopback redirect this doc describes would either break the web login or need a second, separately
registered OAuth App (a server-admin change outside this codebase's control). The shipped
implementation is the `WebView`-based one in `GithubOAuthWebViewDialog.android.kt` instead. Kept here
as a historical record of why the loopback approach doesn't work for this app; see the MASVS-AUTH
section of `docs/security/masvs.md` and `docs/revisit.md` #34 for the current security review of the
`WebView` approach.

## Overview

Replace the current custom-scheme deep-link approach (`taigamobile://oauth/callback`) with an
RFC 8252-compliant loopback redirect. Zero server admin configuration required.

**Why this works:**
- GitHub treats `http://127.0.0.1` as a loopback address and allows it without registration in the OAuth app.
- Taiga's backend (`connector.py`) does **not** send `redirect_uri` when exchanging the code with GitHub, so GitHub never validates it during token exchange.
- The only Taiga endpoint needed is `POST /api/v1/auth {"type":"github","code":"xxx"}`.

See `flow.puml` for the sequence diagram.

---

## New OAuth Flow

1. User enters server URL, taps **Continue with GitHub**.
2. ViewModel validates server URL, fetches `GET <server>/conf.json` → extracts `gitHubClientId`.
3. ViewModel calls `GithubOAuthLauncher.launch(baseAuthUrl)` (suspend).
4. Launcher starts a `ServerSocket(0)` — OS picks a free port.
5. Launcher opens the GitHub URL in an in-app browser with `redirect_uri=http://127.0.0.1:PORT/callback`.
6. User authenticates on GitHub.
7. GitHub redirects to `http://127.0.0.1:PORT/callback?code=xxx`.
8. Launcher reads the code, sends a minimal HTML response, closes the socket.
9. Launcher closes the browser / brings app to foreground.
10. `launch()` returns the code to the ViewModel.
11. ViewModel calls `POST /api/v1/auth {"type":"github","code":"xxx"}`.
12. On success → store credentials → navigate to project selector.

---

## Architecture

### New interface — `GithubOAuthLauncher`

```kotlin
// feature/login/domain — commonMain
interface GithubOAuthLauncher {
    // Suspends until code received or throws on cancel/timeout.
    suspend fun launch(baseAuthUrl: String): String
}
```

The ViewModel injects this interface. Only the Android implementation exists for now;
iOS/Desktop can be added later without touching the ViewModel.

The `baseAuthUrl` is the GitHub OAuth URL **without** `redirect_uri`. The launcher appends
`&redirect_uri=http://127.0.0.1:PORT/callback` after picking the port.

### GITHUB_OAUTH_URL constant (in `LoginViewModel`)

Remove `redirect_uri` from the constant — it becomes:
```
https://github.com/login/oauth/authorize?client_id=%CLIENT_ID%&state=github&scope=user:email
```

### ViewModel change

`startGithubOAuth()` stops emitting to `_openGithubOAuth` channel and instead:
```kotlin
private fun startGithubOAuth() {
    viewModelScope.launch {
        isLoading(true)
        _state.update { it.copy(error = NativeText.Empty) }
        authRepository.getGithubClientId(_state.value.server.trim())
            .onSuccess { clientId ->
                val url = GITHUB_OAUTH_URL.replace("%CLIENT_ID%", clientId)
                runCatching { githubOAuthLauncher.launch(url) }
                    .onSuccess { code -> authWithGithub(code) }
                    .onFailure { error ->
                        isLoading(false)
                        _state.update { it.copy(error = getErrorMessage(error)) }
                    }
            }
            .onFailure { error ->
                isLoading(false)
                _state.update { it.copy(error = getErrorMessage(error)) }
            }
    }
}
```

---

## Platform Implementation

### Android — `GithubOAuthLauncherAndroid`
- **Server**: `java.net.ServerSocket(0)` on `Dispatchers.IO`, 5-minute `soTimeout`.
- **Browser**: `CustomTabsIntent` launched with `applicationContext` + `FLAG_ACTIVITY_NEW_TASK`.
- **Foreground**: `Intent(context, MainActivity::class.java)` with `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK` after code received.
- **Context**: Inject `@ApplicationContext`.
- Add dependency: `implementation(libs.androidx.browser)` to `feature/login/ui`.

---

## Files to Delete

| File | Reason |
|------|--------|
| `feature/login/domain/.../model/GithubAuthCallbackHandler.kt` | Replaced by suspend return from launcher |
| `testing/.../FakeGithubAuthCallbackHandler.kt` | No longer needed |

---

## Files to Modify

| File | Change |
|------|--------|
| `androidApp/.../AndroidManifest.xml` | Remove `taigamobile://` intent filter and `singleTop` launchMode (if added only for OAuth) |
| `androidApp/.../MainActivity.kt` | Remove `handleOAuthIntent`, remove `githubAuthCallbackHandler` injection |
| `feature/login/ui/.../LoginViewModel.kt` | Inject `GithubOAuthLauncher`; replace `_openGithubOAuth` channel + `githubAuthCallbackHandler` subscription with direct `launcher.launch()` call; remove `githubAuthCallbackHandler` injection |
| `feature/login/ui/.../LoginScreen.kt` | Remove `ObserveAsEvents(viewModel.openGithubOAuth)` and `LocalUriHandler` usage |
| `feature/login/ui/build.gradle.kts` | Add `androidx.browser` for Android |
| `testing/.../FakeAuthRepository.kt` | Verify — no GitHub-specific changes expected |

---

## Files to Create

| File | Description |
|------|--------|
| `feature/login/domain/.../launcher/GithubOAuthLauncher.kt` | Interface (commonMain) |
| `feature/login/ui/src/androidMain/.../GithubOAuthLauncherAndroid.kt` | Chrome Custom Tab + ServerSocket |
| `testing/.../FakeGithubOAuthLauncher.kt` | Fake for LoginViewModel tests (returns configurable code or throws) |

---

## What Stays the Same

- `AuthApi.githubAuth()` → `POST /api/v1/auth` — unchanged
- `AuthRepositoryImpl.authWithGithub(code)` — unchanged
- `AuthRepositoryImpl.getGithubClientId(server)` — unchanged
- `LoginState.onGithubLoginClick` — unchanged
- HTTP warning dialog (insecure server) — unchanged
- All password / LDAP login flows — untouched

---

## Out of Scope

- iOS / Desktop OAuth
- GitLab / other social providers
- Taiga Cloud (uses different auth endpoint)
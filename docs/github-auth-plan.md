# GitHub Login Implementation Plan

## Overview

Add GitHub OAuth login to the Android app. No backend changes required.
The Taiga frontend serves a public `conf.json` at `<server>/conf.json` that contains
`gitHubClientId` — the same value the web frontend uses to open the GitHub OAuth URL.

---

## OAuth flow (Android)

1. User enters Taiga server URL.
2. User taps "Continue with GitHub".
   - Server field empty → show validation error, stop.
   - Server field not empty → fetch `GET <server>/conf.json`.
     - `gitHubClientId` missing or empty → show error "GitHub auth is not configured on this server", stop.
     - Has a value → proceed.
3. App opens browser:
   `https://github.com/login/oauth/authorize?client_id=<id>&redirect_uri=taigamobile://oauth/callback&state=github&scope=user:email`
4. GitHub redirects to `taigamobile://oauth/callback?code=<code>&state=github`.
5. `MainActivity` intercepts the deep link (`onNewIntent` / `onCreate`).
6. `GithubAuthCallbackHandler.onCodeReceived(code)` is called.
7. `LoginViewModel` (subscribed in `init`) picks up the code.
8. ViewModel calls `POST /api/v1/auth {"type":"github","code":"<code>"}`.
9. On success → navigate to project selector.

> **conf.json note:** The Client ID is a public value — it identifies the GitHub OAuth
> app but is not a secret. The web frontend already embeds it in JavaScript.

> **Admin requirement:** The Taiga server's GitHub OAuth app must include
> `taigamobile://oauth/callback` as an allowed callback URL.

---

## HTTP warning dialog

Same behaviour as the normal login: if the server URL uses `http://`, the existing
"Unencrypted connection" dialog is shown before proceeding. `onActionDialogConfirm`
dispatches to `startGithubOAuth()` when `authType == GITHUB`.

---

## conf.json fetching

- `AuthApi` gets a new `suspend fun getConfJson(url: String): TaigaConfJson`.
- Uses the existing `authClient` with a full URL `<server>/conf.json`.
  `serverStorage.defineServer(server)` is called first so `HostSelectionPlugin`
  resolves to the right host.
- `TaigaConfJson` is a DTO that only deserialises `gitHubClientId`.

---

## GithubAuthCallbackHandler (domain module)

A Kotlin `object` with a `MutableStateFlow<String?>(null)`.

- `MainActivity` calls `onCodeReceived(code)` — can be called before the ViewModel
  exists; the value is held in the StateFlow until consumed.
- `LoginViewModel.init` subscribes via `filterNotNull()`, calls `clear()` immediately
  after reading to prevent reprocessing on ViewModel recreation.

---

## Files to create

| File | Description |
|------|-------------|
| `feature/login/dto/.../TaigaConfJson.kt` | `@Serializable data class TaigaConfJson(@SerialName("gitHubClientId") val gitHubClientId: String? = null)` |
| `feature/login/dto/.../GithubAuthRequest.kt` | `@Serializable data class GithubAuthRequest(val code: String, val type: String = "github")` |
| `feature/login/domain/.../model/GithubAuthCallbackHandler.kt` | Singleton `MutableStateFlow<String?>` bridge between MainActivity and LoginViewModel |

## Files to modify

| File | Change |
|------|--------|
| `strings/.../strings.xml` | Add `login_github` string |
| `feature/login/domain/.../model/AuthType.kt` | Add `GITHUB("github")` |
| `feature/login/data/.../AuthApi.kt` | Add `getConfJson(url)` and `githubAuth(request)` |
| `feature/login/domain/.../repo/AuthRepository.kt` | Add `getGithubClientId(server)` and `authWithGithub(code)` |
| `feature/login/data/.../AuthRepositoryImpl.kt` | Implement new methods |
| `feature/login/ui/.../LoginState.kt` | Add `onGithubLoginClick: () -> Unit` |
| `feature/login/ui/.../LoginViewModel.kt` | Add GitHub flow; subscribe to handler in `init` |
| `feature/login/ui/.../LoginScreen.kt` | Add GitHub button; open OAuth URL via `LocalUriHandler` |
| `androidApp/src/main/AndroidManifest.xml` | Add `launchMode="singleTop"` + deep link intent filter |
| `androidApp/.../MainActivity.kt` | Handle OAuth deep link in `onCreate` + `onNewIntent` |

---

## What is NOT in scope
- Backend changes
- iOS / Desktop OAuth flow
- GitLab / other social providers
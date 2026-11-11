# Custom request headers — implementation plan

Port of wallosmobile's M28 ("custom request headers for reverse-proxy auth", wallosmobile PR 107 —
`~/proj/grappim/wallosmobile/docs/IMPLEMENTATION_PLAN.md` §4.5b). Read that section for the
original reasoning; this file records only what holds for Taiga and where Taiga differs.

## Why

A self-hosted Taiga exposed through an authenticating reverse proxy (Cloudflare Access's
`CF-Access-Client-Id`/`-Secret`, an `X-…` shared-secret header, …) rejects every request the app
makes, because the app never sends what the proxy asks for. The fix is **user-defined name/value
headers sent on every request to the server's own origin**, not a proxy-specific field.

## Decisions (2026-11-11)

- **`Authorization` is a reserved name.** Taiga authenticates with `Authorization: Bearer <token>`
  (`AuthHeaderPlugin`, `TokenRefreshPlugin`), and a request carries one `Authorization`. So proxies
  that authenticate with `Authorization: Basic …` — Pangolin's Header Auth, nginx `auth_basic` —
  **cannot** front Taiga through this feature; the header editor says so in its hint. This is the
  main divergence from wallosmobile, which has no `Authorization` header of its own.
- **One PR.** Lands on the same branch/PR as the Android 17 local-network permission
  (`feat/android17-local-network-permission`, PR 468), one commit per step.

## Shape

- **Storage** — `core:storage` `customheaders/`: `CustomHeader(name, value)` (`@Serializable`),
  `CustomHeadersStorage` (`headers: Flow`, non-suspend cached `currentHeaders` for the per-request
  plugin, `suspend setHeaders`), DataStore impl storing the whole list as one JSON blob through
  `SecretCipher`. Android uses its own `KeystoreSecretCipher` alias (not the auth token's, so
  neither key's lifecycle drags the other); JVM/iOS use `NoopSecretCipher`, same as `AuthStorage`.
  Undecryptable/undecodable blob → no headers, logged WARN without the throwable (its message
  carries the decrypted JSON). Headers belong to the **server**, not the account: they survive
  logout (`AuthStateManager`).
- **Validation** — `CustomHeaderValidator` beside the model: OkHttp's rule (name `0x21..0x7E`
  minus delimiters, value `0x20..0x7E` or tab), case-insensitive duplicates, reserved names
  `Host`, `Content-Length`, `Content-Type`, `Transfer-Encoding`, `Upgrade`, `Connection`, `Cookie`,
  **`Authorization`**, `User-Agent` (set by `AuthHeaderPlugin`). A row with both fields blank is
  unfilled, never sent, not an error.
- **Sending** — one `createClientPlugin`, `CustomHeaders` (`core:api`), hooked on `SendingRequest`
  (once per hop; `onRequest` runs for the first hop only and `HttpRedirect` copies headers onto
  every hop). Each hop removes the configured names, then re-adds them only when the hop's
  scheme + host + port equal `ServerStorage.server`'s (`isServerOrigin`). Installed on **both**
  clients in `KmpNetworkModule` (`@AuthHttpClient` — login, refresh — and the common one).
  `SendingRequest` runs inside `execute()`, i.e. after `HostSelectionPlugin`/`DebugLocalhostPlugin`
  have rewritten the URL, so the comparison sees the final host.
- **Never logged** — both `Logging` installs get `sanitizeHeader { }` matching configured names
  `ignoreCase`, read at log time.
- **Images** — Coil does not go through the app's API clients. Every platform's loader uses
  `customHeadersNetworkFetcher` (`composeApp` `images/`): Coil's Ktor fetcher with `CustomHeaders`
  installed, so the same origin rule applies per redirect hop and off-origin images (Gravatar) get
  nothing. Android passes it an OkHttp engine carrying `DebugLocalHostImageManager` in debug;
  desktop/iOS install the loader as Coil's singleton at startup, before any `AsyncImage` would make
  Coil build its default.
- **Proxy rejection gets its own error, and must not log the user out.** Taiga, unlike Wallos,
  answers `401` itself — always with `WWW-Authenticate: Bearer realm="api"` (checked against the
  local instance 2026-11-11, both `/users/me` without a token and a bad-password `/auth`). So a
  response is a proxy rejection when it is `407`, or `401` **without** a `Bearer` challenge, or a
  `3xx` whose `Location` is absolute and on another host. Today such a `401` would hit
  `TokenRefreshPlugin`, fail the refresh (the refresh call is rejected too) and **log the user
  out** — the plugin must pass proxy rejections through untouched. `ErrorMappingPlugin` maps them
  to a new `NetworkException.ERROR_PROXY_AUTH` → "the server's proxy rejected the app — check
  custom headers". `HttpRedirect` is the outermost `HttpSend` interceptor (installed before user
  plugins), so each redirect hop passes through `ErrorMappingPlugin` and `HostSelectionPlugin`;
  the off-host check has to throw on the hop itself, or `HostSelectionPlugin` rewrites the
  redirect back onto the server. A refresh call stopped by the proxy keeps the session too.
- **UI** — `CustomHeadersScreen` + ViewModel in `feature:settings:ui` (`customheaders/`, beside
  `trustedcerts/`), explicit Save offered only while all rows are valid and differ from storage.
  Save sits in the top bar and is disabled offline; a failed save keeps the rows and shows a
  snackbar. The `Authorization` caveat is in the screen's description text.
  Reached from a Settings row and from a "Custom headers (N)" button on Login. Login is an ordinary
  Nav3 destination here (unlike wallosmobile's state-swapped login branch), so both entry points
  just `navigator.navigate(CustomHeadersNavDestination)`; `LoginScreen` takes an
  `onCustomHeadersClick` callback wired in `composeApp`'s nav graph, since `feature:login:ui` can't
  depend on `feature:settings:ui`.

## Out of scope

- mTLS / client certificates (wallosmobile declined it too).
- Anything opened outside the app's HTTP stack: attachments/links opened in the browser, the
  GitHub OAuth WebView. Note in the hint text if it turns out to matter.

## Verification fixture

Proxy that demands a non-`Authorization` header in front of the local Taiga (`localhost:9000`),
e.g. nginx with `if ($http_x_proxy_token != "secret") { return 401; }` on a separate port. Exact
command and port recorded in step 7 once it's built.

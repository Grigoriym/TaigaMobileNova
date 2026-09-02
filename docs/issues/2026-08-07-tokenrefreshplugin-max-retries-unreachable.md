# TokenRefreshPlugin's MAX_RETRIES guard is unreachable

**Status:** Done
**Link:** `docs/revisit.md` #11 (found while writing `TokenRefreshPluginTest`, testing improvement-plan task 9a)
**Updated:** 2026-08-07

## Report

This is not a user bug report — it's an internally-found defect, recorded in `docs/revisit.md` #11
while writing test coverage for `core/api`'s auth plugins. The revisit entry's claim:

> `retryCountKey` is only ever written at the end of the same interceptor invocation, and
> `execute(request)` inside an `HttpSend.intercept` block dispatches to the next sender in the
> chain — it does not re-enter the interceptor that called it. So `retries` is 0 on every real
> request and the guard never fires.

Treated as a hypothesis to verify, not a premise.

## Findings

**The plugin's code** (`core/api/src/commonMain/.../TokenRefreshPlugin.kt:46-127`) registers one
`HttpSend` interceptor. On a 401 it reads a `retryCountKey` attribute off the request (default `0`),
compares it to `MAX_RETRIES = 3` (`:56-63`), and — if under the cap — retries by calling
`execute(request)` after bumping the attribute, from three call sites:

- `:83` — another coroutine already refreshed the token; retry with the stored token.
- `:97` — same, but discovered inside the mutex's double-check.
- `:124` — this coroutine performed the refresh itself; retry with the new token.

**Ktor's dispatch semantics** (`ktor-client-core-jvm:3.5.0`, sources jar,
`commonMain/io/ktor/client/plugins/HttpSend.kt:104-121`):

```kotlin
for (interceptor in plugin.interceptors.reversed()) {
    interceptedSender = InterceptedSender(interceptor, interceptedSender)
}
...
private class InterceptedSender(
    private val interceptor: HttpSendInterceptor,
    private val nextSender: Sender
) : Sender {
    override suspend fun execute(requestBuilder: HttpRequestBuilder): HttpClientCall {
        return interceptor.invoke(nextSender, requestBuilder)
    }
}
```

Each interceptor is a `suspend Sender.(HttpRequestBuilder) -> HttpClientCall`. The `Sender` receiver
bound to *this* interceptor's lambda body is `nextSender` — the **next** stage of the chain (another
plugin's interceptor, or ultimately `DefaultSender`, which does the real network call). Calling
`execute(request)` from inside `TokenRefreshPlugin`'s own lambda therefore skips straight past this
plugin's own logic and goes to whatever comes after it in the chain. There is no mechanism by which
`execute()` re-enters the interceptor that called it.

Consequence: the value written to `retryCountKey` right before each of the three `execute()` calls is
never read back, because reading it only happens at the top of *this* interceptor's body — and this
interceptor's body only runs once per top-level `HttpClient.send`, triggered by the pipeline in
`HttpSend.install` (`scope.requestPipeline.intercept(HttpRequestPipeline.Send) { ... }`), not by the
nested `execute()` calls. `retries` is `0` on every entry, confirming the hypothesis.

**Confirmed independently by the plugin's own test suite**, which demonstrates the consequence without
meaning to. Two existing tests use a mock handler that returns 401 unconditionally
(`createClient(authStorage) { unauthorized() }`) — i.e. the retried request *also* gets 401:

- `` `on 401 after another coroutine refreshed should retry with the stored token and not refresh` ``
  (`TokenRefreshPluginTest.kt:69-80`)
- `` `on 401 with a token refreshed while waiting for the lock should retry without refreshing` ``
  (`:82-96`)

Both assert `requests.size == 2` and `tokenRefresher.logoutCallCount == 0` — i.e. after the retry also
comes back 401, the plugin makes no third attempt and does not log out. Neither test asserts the final
`response.status`, so this passes as green today; it is the observable half of the same defect the
revisit entry describes structurally.

**A second, compounding gap, not in the original revisit note:** `retryCountKey` lives on the
`HttpRequestBuilder`'s own `Attributes`, which is fresh per logical request. Even setting aside the
`execute()` dispatch issue, the counter cannot accumulate *across* separate calls to
`client.get(...)` either — there is no carryover. So `MAX_RETRIES` could not have worked as "log out
after N failed refresh attempts over time" even if `execute()` did re-enter the interceptor; at best it
could only ever cap retries within one logical request, and it doesn't do that either.

**Install order** (`KmpNetworkModule.kt`): `TokenRefreshPlugin` is installed after
`ErrorMappingPlugin`, `HostSelectionPlugin`, `AuthHeaderPlugin`, and before `DebugLocalhostPlugin` on
the authenticated client. It is the only `HttpSend` interceptor that reasons about 401 status, so no
other plugin masks or duplicates this behaviour.

## Root cause

`TokenRefreshPlugin.kt:49-126` registers a single `HttpSend` interceptor and relies on `execute()`
being able to re-invoke that same interceptor to accumulate a per-request retry count. Ktor's
`HttpSend` chains interceptors via `InterceptedSender`, where `execute()` always dispatches to the
*next* stage of the chain, never back into the caller. So the 401-retry logic at
`TokenRefreshPlugin.kt:52-63` only ever executes once per logical request; `retryCountKey` is written
but never observed with a nonzero value, and `MAX_RETRIES` is dead code.

## Impact

Not a runaway-retry bug (the thing `MAX_RETRIES` looks like it's guarding against) — it's the opposite
failure: **the plugin retries exactly once per code path, and if that single retry also comes back
401, it gives up silently** — no further retry, no logout, no error surfaced beyond returning the
still-401 response to the original caller.

This matters most for the self-refresh path (`:100-124`, the common case): if
`tokenRefresher.refresh(...)` succeeds and the retried request with the *newly refreshed* token still
gets 401 (server-side session actually dead, refresh returned a token the server doesn't honor, etc.),
the user is left holding a 401 response with no logout prompt — a silent auth dead-end. The refresh
*exception* path is already handled correctly (`:100-111` catches and logs out); this is the sibling
case where refresh succeeds but the retried request still fails, which nothing currently handles.

The two "another coroutine already refreshed" paths (`:76-84`, `:88-98`) are a narrower race window —
they only fire when two requests overlap around a refresh — but hit the identical silent-failure
shape.

Severity: real but low-frequency (needs the retried request to also fail), user-visible as "app stops
working, no logout" rather than a crash or data-loss.

## Open questions

- Should a failed retry attempt a fresh refresh (i.e. actually loop, bounded by a real counter), or is
  "retry once, then logout if still 401" sufficient? The existing design already treats "the stored
  token differs from what I sent" as sufficient grounds to retry without a full refresh — extending
  that into a bounded loop changes the mutex/double-check structure more than a single added check
  would.
- Is the per-request `retryCountKey`/`MAX_RETRIES` machinery worth keeping at all if the fix is "retry
  once, then check the result"? A single boolean/status check after each `execute()` call replaces it.

## Options

**A. Remove the dead `retryCountKey`/`MAX_RETRIES` machinery; after each retry's `execute()` call,
check the response status and log out if it's still 401.**
Three call sites (`:83`, `:97`, `:124`) each get an `if (response.response.status ==
HttpStatusCode.Unauthorized) plugin.tokenRefresher.logout()` before returning. Matches the plugin's
existing "retry once" behaviour — it just stops swallowing the failure. Small, localized diff; every
retry call site changes but the control flow shape doesn't. Con: still only ever retries once; a
transient 401 that would have succeeded on a second retry now logs the user out instead.

**B. Turn the interceptor into a real bounded loop** (`while (retries < MAX_RETRIES) { ... }` inside
the single interceptor body, instead of relying on nested `execute()` re-entry), so `MAX_RETRIES`
means what its name says.
More faithful to the original intent, makes the constant meaningful again. Con: larger rewrite of the
mutex/double-check structure, more surface for a subtle behavioural regression in a security-relevant
path, harder to review. No evidence more than one retry is currently needed in practice (nothing in
the codebase or tests suggests token refreshes fail intermittently in a way a second retry would
rescue).

**C. Won't fix — leave as is, just correct the revisit.md entry to close the "verify" loop.**
The race window is narrow and nothing today reports users hitting a stuck-401 state. Con: it's a
silent auth dead-end with no telemetry to notice if it does happen; low cost to fix (option A) makes
"do nothing" hard to justify once verified.

**Recommendation: A.** It directly closes the gap this investigation found (a failed retry today is
indistinguishable from success to the caller), keeps the diff small and reviewable, and doesn't change
retry *behavior* — only what happens when the existing single retry doesn't help. B solves a problem
nothing suggests actually exists (need for >1 retry) at a much higher review cost for auth code.

## Decision

**Option A, chosen by gregory (2026-08-07).**

## What landed

`TokenRefreshPlugin.kt`: removed the dead `MAX_RETRIES` constant and `retryCountKey` attribute
entirely. Added a local `suspend fun Sender.retryOrLogout(request)` inside `install()` that wraps
each of the three retry call sites (`:80`, `:93`, `:119` post-change) — it calls `execute(request)`
as before, and if the retried response is still `HttpStatusCode.Unauthorized`, calls
`plugin.tokenRefresher.logout()` before returning it. No change to when a retry is attempted, only to
what happens when that single retry also fails.

`TokenRefreshPluginTest.kt`:
- Removed the `` `on 401 with the retry count already at the maximum should log out and return the
  response` `` test and the private `MAX_RETRIES`/`RETRY_COUNT_KEY` companion object — the attribute
  they exercised no longer exists.
- Fixed the two tests that used a blanket `{ unauthorized() }` mock handler for *every* request,
  which meant their retries were also failing — under the old code that was silently ignored, so the
  tests passed while demonstrating the bug; under the new code it would have flipped their
  `logoutCallCount` assertions from `0` to `1`. Changed both (`on 401 after another coroutine
  refreshed should retry with the stored token and not refresh`, `on 401 with a token refreshed while
  waiting for the lock should retry without refreshing`) to a call-count-based handler where the retry
  succeeds with `200 OK`, matching what their names actually claim to test.
- Added `` `on 401 after a successful refresh if the retry is also unauthorized should log out` `` —
  covers the self-refresh path (the common case, and the one this investigation's Impact section
  identifies as most consequential): refresh succeeds, credentials are stored, but the server still
  rejects the retried request, and the plugin now logs out instead of returning the 401 silently.
  The other two retry call sites (`:80`, `:93`) share the same `retryOrLogout` helper, so this one
  test is sufficient evidence the new logout-on-failed-retry behavior works; it isn't duplicated per
  call site.

Deliberately left out: the "double-check" call site's own failed-retry-logs-out test (would be the
same `retryOrLogout` logic exercised via `QueuedAuthStorage` instead of a static token) — not added
since it's the same shared helper already covered by the self-refresh test above, and CLAUDE.md's
surgical-changes convention argues against a same-shape duplicate test.

Verified: `./gradlew :core:api:jvmTest --tests "...TokenRefreshPluginTest"` (7/7 passed), full
`./gradlew jvmTest` and `./gradlew ktlintCheck` both green across the repo.

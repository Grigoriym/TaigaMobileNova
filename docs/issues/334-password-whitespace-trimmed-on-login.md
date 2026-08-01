# 334 — Spaces are not a valid character in passwords (but are for Taiga)

**Status:** Done (uncommitted)
**Link:** https://github.com/Grigoriym/TaigaMobileNova/issues/334
**Reporter:** DrScottN · **Updated:** 2026-08-01

## Report

> I've been testing a home server, and ran into an annoying 'no accounts with these
> credentials' bug that was solved by removing a ` ` (space) character from the end of
> my password.
>
> I have not tested spaces in other places. My guess is that the input processing
> strips or converts these characters at some point.

| | |
|---|---|
| **Symptom** (fact) | Login fails with "no account found with the given credentials" when the password has a trailing space. |
| **Environment** (fact) | Self-hosted ("home server"). |
| **Reporter's diagnosis** (hypothesis) | "input processing strips or converts these characters" — tested below, and correct. |

**The report omits:** app version, platform (Android/iOS/Desktop), Taiga version, and
whether "removing the space" meant changing the stored server-side password or just
typing it differently. That last gap matters and is addressed in Open questions.

## Findings

**1. The app trims the password before sending it.**
`feature/login/ui/.../LoginViewModel.kt:101` — `password = _state.value.password.trim()`.
Present since the auth revamp (`4b01626f`, 2025-06-21); the later commits only
reformatted it.

**2. Nothing else on the client touches it.** Traced the full path:
- `LoginScreen.kt:190-221` — the field passes its value straight to `onPasswordValueChange`; no filter, no transformation. `PasswordVisualTransformation` is display-only.
- `LoginViewModel.kt:218-220` — `setPassword` stores the raw value in state.
- `AuthRepositoryImpl.kt:33` — forwards `authData.password` verbatim. (Line 28's `removeTrailingSlashes()` applies to the *server URL* only.)
- `AuthRequest.kt:6` — plain `@Serializable` data class; kotlinx does not alter strings.

So the ViewModel's `.trim()` is the **only** mutation of the password anywhere on the client.

**3. Taiga preserves password whitespace end-to-end.** Verified against a local
taiga-back checkout at `6.10.2` (`/home/gregory/proj/taiga/taiga-back`, `5fee3b3d`):
- `taiga/auth/serializers.py:44-55` — `TokenObtainPairSerializer.password = serializers.CharField(write_only=True)`, passed straight into `login()`.
- `taiga/base/api/fields.py:495-512` — `CharField.from_native` is `smart_str(value)`; no stripping.
- `taiga/base/api/fields.py:368-402` — `WritableField.field_from_native` calls `from_native`, then only validates. No mutation.
- `taiga/users/services.py:52-65` — `get_and_validate_user` → Django `check_password`, a byte-exact hash comparison.
- Password *set* paths are equally verbatim: `auth/services.py:168,199` (`set_password(password)` on register) and `users/api.py:198-219` (`change_password` reads `request.DATA.get("password")` **raw**, with no serializer at all).

> **Note for future readers:** Taiga does **not** use stock Django REST Framework — it
> vendors a DRF-2.x-style fork under `taiga/base/api/`. Reasoning from modern DRF's
> `CharField(trim_whitespace=True)` default gives the wrong answer here; that option
> does not exist in this code. I initially made exactly that error.

**4. The error string matches.** `taiga/auth/services.py:72-75` raises
`"No active account found with the given credentials"` — the message the reporter
quotes. Confirms they hit the normal-login credential-mismatch path, not some other
failure.

**5. Confirmed against a live Taiga instance** (local dev, `localhost:9000`, seeded
`admin`/`admin`), via the taiga-mcp client:

**Existing account, no state changed:**

| Request | Result |
|---|---|
| `POST /api/v1/auth` password `"admin"` | **200** — authenticates |
| `POST /api/v1/auth` password `"admin "` (one trailing space) | **401** — `"No active account found with the given credentials"` |

**Full round trip**, with public registration temporarily enabled and a throwaway
account (`wstest334`, id 11) registered with the password `"trailing "`:

| Request | Result | What it proves |
|---|---|---|
| `POST /api/v1/auth/register` password `"trailing "` | **201** | The register path does **not** strip whitespace — `BaseRegisterSerializer` accepts and stores it. |
| `POST /api/v1/auth` password `"trailing "` | **200** | The trailing space was stored verbatim and is required at login. |
| `POST /api/v1/auth` password `"trailing"` | **401** — `"No active account found with the given credentials"` | **This is exactly the request the app sent before the fix**, and exactly the error the reporter saw. |

The third row is the bug, reproduced end-to-end against a real Taiga: a password the
server accepted at registration becomes unusable once `.trim()` is applied to it. The
rejection message is **verbatim** the one quoted in the issue.

**Conclusion:** Taiga accepts, stores, and requires a trailing space. The app silently
removes it. The reporter's guess was right.

## Root cause

`LoginViewModel.kt:101` calls `.trim()` on the user's password before building
`AuthData`. When a Taiga account's password has leading or trailing whitespace, the
app can never send the correct credential, and login fails permanently — while the
same credential works in Taiga's web UI, which sends it unmodified.

Note this makes the reporter's own wording self-consistent only one way: since the app
trims regardless, changing *what they typed* could not have changed the request. Their
fix must have been changing the **stored** password server-side, which then matched
what the app was already sending.

## Impact

- **Who:** any user whose Taiga password has leading or trailing whitespace.
- **Severity:** total, permanent lockout from the app for that account. Not a degraded
  experience — the app is unusable, with a misleading "wrong credentials" message that
  points the user at the wrong problem.
- **Frequency:** rare. Requires whitespace at a string edge, which most users never do
  deliberately, though password managers can generate it.
- **Workaround:** change the password server-side (what the reporter did).
- **Scope:** all platforms — the code is in `commonMain`. GitHub OAuth login is
  unaffected (no password involved).

## Secondary finding

`LoginViewModel.kt:111` — `val isPasswordInputError = _state.value.password.isBlank()`.
A password consisting only of whitespace is rejected client-side and never submitted.
Same class of bug, same file; worth deciding on together rather than leaving a second
inconsistency behind. Untested against a real instance, but Taiga's only length rule is
`>= 6` characters, so six spaces is a valid Taiga password.

## Open questions

1. **Did the reporter change the stored password, or something else?** Their wording is
   ambiguous. **Does not block** — the defect is proven from the code on both sides
   independently of their exact steps.
2. **Is their instance stock Taiga 6.10.x?** Assumed; verification used a 6.10.2
   checkout. **Does not block** — this code path is long-standing and unlikely to differ.
3. ~~**Not reproduced against a live server.**~~ **Fully resolved** — see Findings
   point 5. A live Taiga stores a trailing-space password, requires it at login, and
   returns the reporter's exact error for the trimmed variant. No part of the chain
   rests on inference any more. Unit-level reproduction also exists via
   `FakeAuthRepository.authCalledWith` (`testing/.../FakeAuthRepository.kt:8`);
   existing tests missed this only because `getRandomString()` never produces edge
   whitespace.

   *Test-instance state:* public registration was temporarily enabled to allow this,
   and account `wstest334` (id 11) was created. Both should be reverted.

## Options

### Option 1 — Send the password verbatim; validate with `isEmpty()` *(recommended)*

Drop `.trim()` at line 101; change line 111 to `isEmpty()`. Leave the `server` and
`username` trims alone.

- **Pros:** matches Taiga's actual behaviour exactly; fixes the reporter; the client
  stops silently mutating a credential it isn't the authority on; aligns with NIST
  SP 800-63B (verifiers shall accept the space character and shall not truncate).
- **Cons — real one:** today, a stray trailing space from a copy-paste is silently
  absorbed and the login still succeeds. After this change that user gets a failed
  login instead. We trade a rare *silent save* for a rare *silent failure* — but the
  failure is recoverable by retyping, whereas the current bug is not recoverable at all
  from inside the app.
- **Risk:** low. **Blast radius:** the password-login path only; OAuth untouched.

### Option 2 — Send verbatim, but keep `isBlank()`

Fix line 101 only.

- **Pros:** one-line diff; still fixes the reported case.
- **Cons:** leaves a whitespace-only password unusable, which is the same bug in a
  narrower form — we'd be fixing the principle in one line and violating it in the next.
- **Risk:** low. **Blast radius:** same.

### Option 3 — Won't fix

- **Pros:** zero risk; preserves the accidental-paste convenience.
- **Cons:** leaves accounts that work in the web UI permanently unable to log in via the
  app, with an error message that misdirects the user. The convenience protects a
  guess about user intent; the bug breaks a verified real user.
- **Risk:** none technically, but the issue stays open and will recur.

### Option 4 — Remove all three trims (server, username, password)

- **Pros:** fully consistent "don't touch user input".
- **Cons:** trimming genuinely helps the other two. Server URLs are commonly pasted with
  stray whitespace, and Taiga usernames cannot contain spaces anyway
  (`auth/serializers.py:78` enforces `^[\w.-]+$` at registration), so trimming username
  is harmless and prevents paste errors. This adds failure modes with no upside.
- **Risk:** medium — the server URL feeds `serverStorage.defineServer()`, so it has a
  wider blast radius than login.

### Rejected without full workup

**Retry with a trimmed password after a failure.** Would paper over both cases, but it
doubles failed-auth attempts against Taiga's `LoginFailRateThrottle`
(`taiga/auth/api.py:53`), guesses at user intent, and makes the credential path harder
to reason about.

## Decision

**Option 1**, chosen by the maintainer (Grigoriym) on 2026-08-01: send the password
verbatim, and validate it with `isEmpty()` instead of `isBlank()`.

## Plan

Two parts. The regression test lands first and is observed failing, so we know it tests
the real defect rather than passing vacuously.

| # | Change | Verification |
|---|---|---|
| 1 | Add two regression tests to `LoginViewModelTest` | Run `:feature:login:ui:jvmTest` — both must **fail** before part 2: one on the `.trim()`, one on `isBlank()` |
| 2 | `LoginViewModel.kt` — drop `.trim()` on password (line 101), `isBlank()` → `isEmpty()` (line 111) | Same tests now pass; full `:feature:login:ui:jvmTest` stays green |

Test 1 — `login sends the password verbatim` — sets a password with leading, trailing
*and* interior spaces and asserts `FakeAuthRepository.authCalledWith?.password` equals
it exactly. Fails before the fix because `.trim()` strips the edges.

Test 2 — whitespace-only password — asserts a six-space password is actually submitted
rather than flagged as an input error. Fails before the fix because `isBlank()` is true
for it, so `login()` is never reached.

## What landed

**`LoginViewModel.kt`** — two lines:
- L101: `password = _state.value.password.trim()` → `password = _state.value.password`
- L111: `password.isBlank()` → `password.isEmpty()`

**`LoginViewModelTest.kt`** — two regression tests, added next to the existing
`validateAuthData` group:
- `on validateAuthData should send the password verbatim keeping leading and trailing spaces`
- `on validateAuthData with a whitespace only password should login`

Both were confirmed failing before the fix, with the failures naming the exact defect:

```
expected:<[  pass word  ]> but was:<[pass word]>     ← the .trim()
Expected value to be false                          ← isPasswordInputError from isBlank()
```

Neither uses Turbine: `MainDispatcherRule` supplies an `UnconfinedTestDispatcher`, so the
login coroutine runs eagerly and the fake can be asserted on directly.

**Verification:** `:feature:login:ui:jvmTest` 21/21 green, `:feature:login:data:jvmTest`
green, and a full `./gradlew jvmTest` across all modules green. The pre-existing
`on validateAuthData with empty password should not login` still passes — it uses `""`,
which `isEmpty()` still rejects, so validation of genuinely empty input is unchanged.

**Deliberately not done:**
- `server` and `username` keep their `.trim()` (Option 4 rejected — see above).
- Issue not closed, nothing committed or pushed.
- No reproduction against a live Taiga instance; the unit-level reproduction plus the
  taiga-back source reading was judged sufficient. Open question 1 (what the reporter
  actually changed) remains unresolved and does not affect the fix.

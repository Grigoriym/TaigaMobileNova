# MASVS security review: implementation plan

**Status: OPEN.** Created 2026-08-09. No task started yet.

**Baseline:** two reference studies already in `docs/security/` —
[Aegis — Security & Crypto Practice Study](Aegis%20-%20SECURITY_CRYPTO_STUDY.md) and
[KeePassDX — Cryptography & Secret-Handling Architecture](KeePassDX%20-%20CRYPTO_ARCHITECTURE.md) —
read patterns worth borrowing from two well-regarded FOSS Android apps with a similar
local-secret/self-hosted threat model. **Neither is an audit of this app** — treat them as
reference material for what "good" looks like, not as findings against TaigaMobileNova.

The actual review is done with the **`masvs-review` skill**
(`~/proj/grappim/agentic-grappim/skills/masvs-review/`), which reviews **one MASVS v2 category per
run** from source and maintains `docs/security/masvs.md` as the living register — deviations get
recorded once with their bound, not re-raised every run. Read the skill itself
(`SKILL.md` + `references/masvs-controls.md` + `references/kmp-checks.md`) before running task 0;
this plan doesn't repeat its mechanics.

A sequence of small, independent tasks, same shape as `docs/testing/improvement-plan.md` and
`docs/desktop/linux-release-plan.md`: one task per session, done, verified, finalized, committed.

## How to run a task

1. Read the status table below and take the task marked **NEXT** (or the first `todo` if none is
   marked). **Before assuming the NEXT task is actually undone, run `git status`/`git diff`** — a
   session can finish the review and even the Result note, then get cut off before the table
   update, `finalize`, and commit (same failure mode the other two plans document).
2. Read only that task's section, plus the skill's own docs if you need the mechanics again.
3. Invoke the `masvs-review` skill, scoped to the one category named in the task — don't let it
   default to a whole-app pass. It reads `docs/security/masvs.md` first (task 0 creates it if
   absent), checks source per `references/kmp-checks.md`, and separates verified-statically /
   needs-a-device-or-APK / not-checked, per the skill's own Step 3.
4. **Any Open finding worth fixing now:** fix it if it's a small, isolated change; if it's bigger,
   write it into `docs/revisit.md` with enough evidence (`file:line`) that a cold session can act on
   it — not chat, and not fixed inline in a way that makes this task's own diff unreviewable.
5. Update the status table (`✅ done — <date>`, move NEXT) and add a `**Result (<date>):**` note
   saying what the register actually gained — new Accepted deviations, new Open findings, what moved
   to the "needs a device" bucket. **End the note by naming what comes next**, same convention as
   the other two plans.
6. Run the **`finalize` skill** — automatically, no asking (standing rule, same as the other plans).
7. **Commit and push** — same standing authorization: don't ask, don't stop after finalize waiting
   to be told. Never commit a red build, never push straight to `dev` (branch first if not already
   on one), ask before anything beyond commit+push (opening a PR, force-push, etc.).

**A finding that needs a device or a built APK is not this plan's job to close.** Name it in the
register's third table and move on — a follow-up device-testing pass (`emulator-testing` skill) is
a separate, later effort, not a reason to block a task here.

## Status

| # | Task | MASVS category | Status |
|---|---|---|---|
| 0 | Storage — the server credential at rest | STORAGE | ✅ done — 2026-08-09 |
| 1 | Cryptography — key management for whatever protects it | CRYPTO | ✅ done — 2026-08-09 |
| 2 | Network — TLS, cleartext, the custom trust manager | NETWORK | ✅ done — 2026-08-09 |
| 3 | Authentication — login flow, GitHub OAuth WebView | AUTH | todo — ⬅ NEXT |
| 4 | Platform — WebView, IPC surface, screenshot leakage | PLATFORM | todo |
| 5 | Code quality — minSdk, dependency scanning, input validation | CODE | todo |
| 6 | Privacy — permissions, crash reporting, data clearing on logout | PRIVACY | todo |
| 7 | Resilience — scope decision only, no code review | RESILIENCE | todo |

**Order rationale:** Storage first — the stored server credential is the asset the skill's own
framing centers on, and a scoping pass already found exactly where it lives. Crypto follows
immediately since it's the same question one layer down (is that storage protected, and how) —
natural to review back to back while the Storage findings are fresh. Network is next because
scoping found a real, non-trivial trust-on-first-use implementation that deserves a careful read,
not a quick grep (task 2). Auth follows Network and precedes Platform because the login flow's
GitHub OAuth WebView is both an AUTH and a PLATFORM concern on the same code — Auth reviews the
protocol question first, Platform picks up the WebView-mechanics half right after, while it's
fresh. Code and Privacy are smaller, more mechanical checks, ordered after the meaty ones. Resilience
is last and is a scope decision, not an audit, per the skill's own default for a self-hosted FOSS
client — record it once and close the plan.

---

## Task 0 — Storage

**Why:** `AuthStorageImpl` (`core/storage/src/commonMain/kotlin/com/grappim/taigamobile/core/storage/auth/AuthStorage.kt:20-49`)
stores the Taiga session `token` and `refresh_token` as plain `stringPreferencesKey`s in an
unencrypted DataStore `Preferences` file — no Keystore-backed cipher over the value anywhere in the
read path. Separately, `androidApp/src/main/AndroidManifest.xml:12` sets
`android:allowBackup="true"` on the **release** manifest, while the **debug** manifest
(`androidApp/src/debug/AndroidManifest.xml:6-8`) is the one that turns it off — the exact inversion
`kmp-checks.md` calls out by name as backwards ("a common inversion is `false` in debug and `true`
in main — backwards, since debug is the build with nothing worth taking"). On a device with `adb
backup` or cloud backup enabled, the plaintext session token travels with the backup.

**Scope:** run `masvs-review` for MASVS-STORAGE. Confirm or refute both leads above with source
evidence (don't just copy this task's framing into the register). Also check: whether the cached
server URL (`DataStoreServerStorage`/`ServerStorageImpl`, `core/storage/.../server/`) is itself
sensitive enough to matter here; whether any `logcat`/`Timber` call site near auth logs the token or
password (`core/api`'s Ktor logging plugin is the likely place to check); and iOS specifically —
`AuthStorage` is `commonMain`, so confirm there's no Keychain-backed wrapper being bypassed on that
platform (i.e. iOS gets the identical plaintext DataStore file everyone else does, or it doesn't —
say which).

**Done when:** `docs/security/masvs.md` exists with a Storage section, and both leads from this
task's "Why" are resolved one way or the other in the register (an Open finding with a proposed
fix, or an Accepted deviation with a stated bound) — not left as unconfirmed leads.

**Finalize focus:** high — this is the first task in the plan, so also note anything about running
the skill itself that wasn't obvious (how the category gets passed, whether the register skeleton
needed anything beyond the skill's own template).

**Result (2026-08-09):** `docs/security/masvs.md` created. Both leads from the "Why" confirmed with
source evidence, not just copied framing:

- **Session token/refresh token plaintext in DataStore** confirmed on **all three platforms**
  (Android `filesDir/datastore/auth_storage.preferences_pb`, JVM `appDataDir()/...`, iOS
  `NSDocumentDirectory/...`) — no Keystore/Keychain wrapper anywhere (`grep -rl
  'Keychain\|kSecAttr\|SecItem'` across all source sets returns nothing; same for
  `Keystore\|SecretKey\|Cipher.getInstance\|KeyGenParameterSpec`). Recorded as an **Open** finding
  for MASVS-STORAGE-1, explicitly handed to task 1 to decide how to key it, per this task's own
  "Why" framing.
- **`allowBackup` inversion confirmed exactly as scoped** — release manifest had no
  `dataExtractionRules`/`fullBackupContent` at all (default: back up everything), debug had both
  set to disable backup entirely. **Fixed in this task** (small, isolated — one manifest edit + two
  new `res/xml` files) rather than deferred: release keeps `allowBackup="true"` but now excludes the
  auth DataStore file (and its legacy `SharedPreferencesMigration` source) from cloud backup and
  device transfer via `data_extraction_rules.xml` (API 31+) and `backup_rules.xml` (API 24-30,
  matching `minSdk`). Verified against the merged release manifest output
  (`processFdroidReleaseMainManifest`). Per the skill's own rule, a fixed finding doesn't stay in
  the register — the register has a prose note explaining the fix instead of an Open row.
- Cached server URL (`DataStoreServerStorage`) confirmed to hold only a bare base URL, no embedded
  credential — recorded as an **Accepted deviation**, not a finding.
- No log call site anywhere logs the token/password/secret — verified statically, noted in the
  register.
- Two register items moved to the "needs a device" bucket: whether the new backup exclusion
  actually holds under a real `adb backup`/cloud backup, and (once task 1 lands a cipher) whether
  Keystore/Keychain hardware-backing is actually enforced.

**Running the skill itself:** no surprises — `docs/security/masvs.md` didn't exist, so this task
created it from the skill's Step 4 template as-is (header, Accepted/Open/Needs-a-device tables). One
addition beyond the template: a "Notes" section at the bottom for a fixed-finding explanation, since
the skill says a fixed finding "leaves the register" but gives no guidance on where to briefly note
*why* a row that a reader would expect (backup inversion, given how prominently task 0's "Why"
names it) isn't there. Recommend keeping that "Notes" section as the register's per-category
convention for that situation, rather than reinventing it each task.

**Next: Task 1 — Cryptography.** Register already has the Open MASVS-STORAGE-1 finding pointing at
it; task 1's job is to decide how (or whether) to key protection for the token/refresh_token, and
separately confirm no other key material exists in source/build config/version catalogue.

---

## Task 1 — Cryptography

**Why:** falls directly out of task 0 — if the credential (or anything else) ends up needing
protection beyond plain DataStore, this is the task that reviews *how* it should be keyed
(Keystore-backed `SecretKey`, `KeyGenParameterSpec` purposes/block mode/IV reuse) rather than
inventing a scheme inline in task 0. If task 0 concludes the credential's exposure is bounded enough
to accept as-is (e.g. Android's app-private DataStore directory plus the `allowBackup` fix already
closes the realistic threat), this task may find nothing to review beyond confirming there is no key
material anywhere in source, build config, or the version catalogue — a real, if smaller, MASVS-
CRYPTO-2 check on its own.

**Scope:** run `masvs-review` for MASVS-CRYPTO. Grep for any `SecretKey`/`Cipher.getInstance`/
`KeyGenParameterSpec` use across all source sets (per `kmp-checks.md`'s "check every source set"
rule) — if none exists anywhere, MASVS-CRYPTO-1/2 are likely N/A by construction (nothing does
application-level crypto), which is a real, recordable outcome, not a skipped task.

**The app is live in production — if this task adds Keystore/Keychain-backed encryption over
`token`/`refresh_token`, every already-installed user has a *plaintext* value sitting in
`AuthStorage`'s DataStore file today.** A read path that assumes ciphertext breaks or silently logs
out every existing user on upgrade unless this is handled explicitly. This is not a reason to skip
adding encryption — it's a reason the task isn't done until the upgrade path is named: either a
one-time migration (read plaintext if decryption fails / value isn't recognizably ciphertext,
re-encrypt in place) or an accepted "next login re-establishes it, here's what that costs the user"
tradeoff. Whichever is chosen, state it in the register next to the MASVS-CRYPTO-2 row, not just in
code.

**Done when:** register has a Cryptography section — either concrete findings tied to task 0's
outcome (including the production-migration path if encryption is added), or an explicit "no
application-level cryptography exists" note with the grep that backs it.

**Result (2026-08-09):** Confirmed no application-level cryptography existed anywhere pre-task
(`grep -rl 'Keystore\|SecretKey\|Cipher\.getInstance\|KeyGenParameterSpec'` and the Keychain
equivalent both empty; no key material in source/build config/version catalogue). Rather than
defaulting to "accept as bounded," the crypto decision was put to the user directly (three options:
accept plaintext as bounded / implement Android+iOS now / Android only with iOS deferred) — chosen:
**Android only, iOS deferred**.

Implemented: a `TokenCipher` seam in `core/storage/.../auth/` (interface + `NoopTokenCipher`
passthrough), threaded through `AuthStorageImpl` so every read/write of `token`/`refresh_token` goes
through it. Android's actual, `AndroidKeystoreTokenCipher`, wraps the value in AES/GCM keyed by an
`AndroidKeyStore`-resident key (`KeyGenParameterSpec`, `BLOCK_MODE_GCM`, no padding, fresh random IV
per encryption, never reused) — ciphertext stored as `"v1:" + base64(iv + ciphertext)`. **Production
migration**, since the app is live: any stored value without the `"v1:"` prefix is a pre-cipher
plaintext token and is returned as-is by `decrypt()` rather than treated as an error — no forced
logout on upgrade — then gets encrypted in place the next time `setAuthCredentials` runs (the next
token refresh), a deliberately lazy migration chosen over a write-on-read side effect through the
`tokenFlow`/`refreshTokenFlow` `Flow.map` collectors. A `"v1:"`-prefixed value that fails to decrypt
(corruption, or a Keystore key invalidated by e.g. a biometric enrollment change) returns `""` —
treated as logged out rather than sending a broken bearer token. JVM/desktop keeps `NoopTokenCipher`
(outside MASVS scope by the skill's own Step 0); iOS also keeps `NoopTokenCipher` for now, recorded
as an explicit Open finding (MASVS-STORAGE-1/CRYPTO-2) with the fix shape named (Keychain item,
`kSecAttrAccessibleWhenUnlockedThisDeviceOnly`, same plaintext-fallback migration pattern) — not left
implicit.

Verified: `:core:storage:jvmTest` (new tests: cipher round-trip, legacy-plaintext passthrough,
decrypt-failure → empty string, plus `NoopTokenCipherTest`), full `./gradlew jvmTest`, `ktlintCheck`
(one auto-format needed on the changed class signature), `koverXmlReport`/`:koverVerify` — all green.
Compiled on all three targets (`compileAndroidMain`, `compileKotlinIosArm64`,
`compileKotlinIosSimulatorArm64`). `AndroidKeystoreTokenCipher` itself has **no automated test** — no
Android unit-test source set exists in this repo by design (CLAUDE.md) — so its Keystore behavior is
only reviewed by reading the code; hardware-backing enforcement is recorded in the register's "Needs
a device" table, not claimed as verified.

**Next: Task 2 — Network.** Register already scoped `CompositeTrustManager`'s TOFU flow and the iOS
gap (no trust-manager equivalent there either) in this plan's own task 2 section — task 2's job is to
actually run the skill's three TOFU questions against source and write the Network section.

---

## Task 2 — Network

**Why:** scoping already found a real, deliberately-designed trust-on-first-use system, not a naive
trust-everything override — worth a careful read, not a quick grep. `CompositeTrustManager`
(`core/api/src/androidMain/kotlin/com/grappim/taigamobile/core/api/CompositeTrustManager.kt`, wired
into the JVM engine the same way via `PlatformHttpClientEngine.jvm.kt`) wraps the platform default
`X509TrustManager`: it falls through to the default check first, and only offers TOFU
(`UntrustedCertificateException(pendingCertTrust(...))`) when that default check fails **and** the
presented cert's CN/SAN actually matches the requested host (`hostMatchesCertificate`) — exactly the
"falls through to platform default" and "hostname-matched before offering trust" checks
`kmp-checks.md`'s MASVS-NETWORK section names as the ones that separate a bounded TOFU flow from an
unbounded one. Pinning is the SHA-256 fingerprint of the leaf cert (`sha256Fingerprint`), stored via
`TrustedCertStorage`, with a real revoke UI already shipped
(`feature/settings/ui/.../trustedcerts/TrustedCertificatesScreen.kt`). This looks like the kind of
pattern the skill's rules ask to record as an Accepted deviation, not report as a finding — but the
three-question check `kmp-checks.md` names has to actually run first, including "is the pin
per-certificate" (regenerate a leaf, confirm the app objects — not source-checkable, flag for the
"needs a device" bucket).

**Also found: iOS has no equivalent at all.** `PlatformHttpClientEngine.ios.kt`
(`core/api/src/iosMain/...`) is a one-line `Darwin.create()` with no `TrustedCertStorage` wired in —
the TOFU flow only exists on Android and JVM/desktop. State this as a named per-platform gap: an iOS
user pointed at a self-signed server either can't connect at all, or Darwin's own default trust
handling is doing something undocumented here — check which, don't assume.

`android:usesCleartextTraffic="true"` (`androidApp/src/main/AndroidManifest.xml:18`) is also in
scope — likely an accepted deviation (self-hosted LAN instances speak plain HTTP), matching the
skill's own worked example, but record it with its actual bound rather than assuming the example
applies verbatim.

**Done when:** register has a Network section covering the trust manager per-platform (Android/JVM
vs. iOS), the cleartext deviation with its bound, and states plainly which of the three TOFU
questions were verified from source vs. still need a device.

**Result (2026-08-09):** `docs/security/masvs.md` gained a Network section. `CompositeTrustManager`
(identical on Android and JVM/desktop — same class, wired via `PlatformHttpClientEngine.android.kt`
and `.jvm.kt`) recorded as an **Accepted deviation**, not a finding: all three of `kmp-checks.md`'s
TOFU questions verified —

1. **Falls through to the platform default first** — TOFU is only reached from the
   `catch (e: CertificateException)` arm after `defaultTrustManager.checkServerTrusted` already
   rejected the chain (`CompositeTrustManager.kt:63-79`). Verified from source.
2. **Hostname-matched before offering trust** — `hostMatchesCertificate` (SAN, with a CN fallback for
   SAN-less self-signed certs) runs before `UntrustedCertificateException` is thrown; a mismatch gets
   `CertificateHostnameMismatchException` instead of a TOFU offer (`:69-78`, `:95-111`). Verified from
   source.
3. **Pin is per-certificate, not per-host** — the storage layer's `isTrusted`/`trust` key is
   `(host, sha256Fingerprint)` (`TrustedCertStorageImpl.matches`), not host alone, so a regenerated
   cert on an already-trusted host would not match and would re-trigger TOFU. Verified from source
   *and* a dedicated unit test (`CompositeTrustManagerTest.\`pin for a host does not trust a different
   certificate presented by that same host\``) — but the full handshake-level behaviour against a
   real regenerated leaf still needs a device/live server, moved to "Needs a device."

`android:usesCleartextTraffic="true"` recorded as an **Accepted deviation** with its actual bound:
applies app-wide (no `network_security_config.xml` scoping it to specific hosts), and
`AuthHeaderPlugin` attaches the bearer token regardless of scheme — so the token is sent in the clear
if the user configures an `http://` server, with no in-app warning today. MASVS-NETWORK-2 recorded as
N/A by construction (user-supplied server), per the control's own qualifier.

**iOS confirmed to have no equivalent at all** — `Darwin.create()` ignores the `trustedCertStorage`
parameter; no Keychain-backed trust store, no TOFU. This is the *safe* direction (fails closed rather
than silently bypassing validation) so it is **not** a MASVS-NETWORK finding, but it is a real
feature-parity gap: `TrustedCertificatesScreen` (`commonMain`) is reachable on iOS yet permanently
empty there since nothing ever populates `TrustedCertStorage` on that platform. Recorded as a Note,
not an Open row.

Two small, real gaps found but not fixed inline (out of this task's scope — a documentation review,
not a UI change) — written into `docs/revisit.md` with evidence: **#32** (no warning when the
configured server URL is `http://`, despite the token being sent over it) and **#33**
(`TrustedCertificatesScreen` inert on iOS, plus the missing TLS trust port).

**Next: Task 3 — Authentication.** Register doesn't yet have an Auth section; task 3's job is to
verify the `GithubOAuthWebViewDialog` WebView finding with its own file:line and resolve it (fix or
documented bound), plus decide whether MASVS-AUTH-2/3 apply at all (likely N/A, no app-lock exists).

---

## Task 3 — Authentication

**Why:** `GithubOAuthWebViewDialog`'s Android actual
(`feature/login/ui/src/androidMain/kotlin/com/grappim/taigamobile/feature/login/ui/GithubOAuthWebViewDialog.android.kt`)
drives GitHub's OAuth authorization inside an in-app `WebView` with `javaScriptEnabled = true` and
`domStorageEnabled = true`, intercepting the redirect in `shouldOverrideUrlLoading` to pull the
`code` query param off the URL. This is the exact pattern `kmp-checks.md` names: "a third-party
login in an embedded WebView fails RFC 8252 regardless of how the WebView is configured — the app
can read what the user types into someone else's login page, and the user cannot see the address
bar." Note: `docs/desktop/linux-release-plan.md` task 8 already found (and fixed) that this button
is a dead no-op on JVM/iOS — the button is hidden there now — so today this WebView flow only
actually executes on Android. That narrows the finding's real-world scope; it doesn't remove it.

**Scope:** run `masvs-review` for MASVS-AUTH. Verify the WebView finding with its own file:line
(don't just copy this task's framing into the register) and propose the fix (Custom Tab /
`androidx.browser` — the standard RFC 8252-compliant replacement) or state why the WebView is being
kept with a documented bound (JS restricted, no bridge, origin-locked to GitHub's OAuth host).
Separately, check where the primary username/password login sends credentials — is it a plain Taiga
API call over whatever `NETWORK` task 2 already characterized, or does it have its own concern.
Decide whether MASVS-AUTH-2/3 (local auth, step-up auth) apply at all — there's no app-lock/biometric
gate anywhere in the codebase per prior sessions' knowledge, so likely N/A; confirm rather than assume.

**Done when:** register has an Auth section; the WebView finding is resolved (Open finding with a
proposed fix, or Accepted deviation with a stated bound) rather than left hanging for task 4 to
pick up by default.

---

## Task 4 — Platform

**Why:** picks up the WebView-*mechanics* half of task 3's finding (MASVS-PLATFORM-2 is the WebView
control itself; MASVS-AUTH-1 is "is this a secure auth protocol" — same code, two controls, reviewed
in sequence rather than duplicated) plus what task 3 doesn't cover: IPC surface and UI-level
leakage. Scoping found the manifest's IPC surface is minimal — `MainActivity` is the only
`exported="true"` component, and its only `intent-filter` is the plain `MAIN`/`LAUNCHER` launch pair
(`androidApp/src/main/AndroidManifest.xml:22-30`), no deep-link scheme — so MASVS-PLATFORM-1 is
likely a quick pass unless the review turns up a content provider or other manifest entry this
grep-based scoping missed. **`FLAG_SECURE` was grepped for and found nowhere in the codebase** — no
screen currently opts out of appearing in the recents-list thumbnail, which matters most for the
login screen (credential visible in a screenshot) and any future "reveal API key"-style UI.

**Done when:** register has a Platform section; the WebView entry cross-references task 3's finding
rather than restating it; `FLAG_SECURE`/recents exposure is recorded as either a finding or an
accepted deviation with a stated reason (e.g. "no screen currently shows a raw credential" — verify
that's actually true of the login screen before writing it down).

---

## Task 5 — Code quality

**Why:** `minSdk = 24` (`gradle/libs.versions.toml:22`) is MASVS-CODE-1 — record it as a deliberate
reach decision and note whether a reason is documented anywhere (README, an issue) or isn't. **No
`.github/dependabot.yml` and no dependency-check/OSV/Snyk plugin anywhere in `.kts`/`.yml`/`.toml`**
(confirmed by grep) — MASVS-CODE-3 is very likely a real, simple gap: a Gradle-ecosystem
`dependabot.yml` is a small, self-contained fix, not a design decision requiring a scope pass.
MASVS-CODE-4 (untrusted server input): confirm the app's own Ktor/serialization config tolerates
unknown/null fields from the server the same way `tools/seed`'s does (CLAUDE.md already documents
`ignoreUnknownKeys` for the seed tool specifically — check whether the app's own DTOs share that
config or have their own), and that any server-supplied HTML/URL (task descriptions, wiki content,
avatar URLs) is escaped or sandboxed before being rendered or followed.

**Done when:** register has a Code section; the dependabot gap either has a fix landed in this task
(a small, self-contained `.github/dependabot.yml` addition) or is deferred to `docs/revisit.md` with
a stated reason for not doing it now.

---

## Task 6 — Privacy

**Why:** the app ships two Android flavours with different crash-reporting posture —
`androidApp/src/gplay/kotlin/.../CrashReporterImpl.kt` (real Crashlytics, per CLAUDE.md's Logging
table) vs. `androidApp/src/fdroid/kotlin/.../CrashReporterImpl.kt` (confirmed a pure no-op:
`isAvailable = false`, every method a stub) — MASVS-PRIVACY-3 needs both flavours named explicitly
in the register, not just "crash reporting exists." MASVS-PRIVACY-4 (can the user clear their data)
ties to a bug `docs/desktop/linux-release-plan.md` task 7 already found and fixed for JVM/desktop
(logout silently left the local Room cache populated). **Android's own actual was already correct
before that task touched anything** — `core/storage/src/androidMain/.../db/TaigaDBExt.android.kt` is
`actual suspend fun TaigaDB.clearAllTablesKmp() = clearAllTables()`, a real call, not the `= Unit`
stub JVM/iOS had — so Android's logout path likely already satisfies MASVS-PRIVACY-4; confirm this
by reading the call site (`AuthStateManager.logoutSuspend()`) rather than assuming it from the
desktop task's framing, and check iOS too (that task's Result note says iOS's stub signature changed
but its `= Unit` body was deliberately left alone — meaning iOS likely still has this gap open right
now). Declared permissions (`INTERNET`, `ACCESS_NETWORK_STATE`, both manifest-confirmed and both
obviously used) vs. actual use is a quick diff, unlikely to turn up anything.

**Done when:** register has a Privacy section covering both flavours' crash-reporting posture and
states, per platform, whether logout actually clears cached data — cross-referencing the desktop
plan's task 7 rather than re-investigating what it already proved for JVM.

---

## Task 7 — Resilience (scope decision only)

**Why:** the skill's own default is that MASVS-RESILIENCE is out of scope for a self-hosted FOSS
client — it defends a vendor's assets against the device owner, and here the device owner is the
data owner, and the source is public anyway. This task isn't a code review; it's making that
decision explicit for this specific app and recording it once, per the skill's Step 0, so it never
needs re-deciding in a future run.

**Scope:** confirm the reasoning actually holds for TaigaMobileNova (self-hosted by design, FOSS,
no vendor asset being protected against the app's own user) and write the "Out of scope:
MASVS-RESILIENCE (...)" line into `docs/security/masvs.md`'s header per the skill's register
template. No root/tamper/obfuscation review beyond that.

**Done when:** the register's header states the Resilience exclusion with its one-line reason. This
is the last task in the plan — once done, close the plan doc the same way
`docs/testing/improvement-plan.md` and `docs/desktop/linux-release-plan.md` were closed (status
banner, note what's kept).

**Finalize focus:** low.

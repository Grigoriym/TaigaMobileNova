# Biometric App-Lock — Scoping

## Background

`docs/security/masvs.md` records MASVS-AUTH-2 (local authentication) and MASVS-AUTH-3 (step-up auth)
as **N/A by construction** — no biometric/app-lock mechanism exists anywhere in the codebase
(confirmed: `grep -rln 'biometric\|Biometric\|BiometricPrompt\|androidx.biometric'` across all source
sets and `gradle/libs.versions.toml` is empty). That classification is correct as a MASVS finding —
those controls only apply if such a mechanism exists to evaluate — but it's a feature gap, not a
closed question, and gregory asked to scope adding one.

**No decision has been made and nothing has been built.** This is a scoping doc only, written so a
future session can pick this up without re-deriving the codebase survey below. Requirements gregory
has already stated: **disabled by default**, and **toggleable in Settings**.

---

## What exists today that this would build on

- **Toggle storage pattern**: `TaigaSessionStorage.crashReportingEnabled` (`core/storage/src/commonMain/kotlin/com/grappim/taigamobile/core/storage/TaigaSessionStorage.kt:24,51,147-148`)
  is the exact shape a `biometricLockEnabled: Flow<Boolean>` key would follow — a
  `booleanPreferencesKey` in the same DataStore, defaulting via `?: false` this time instead of `?: true`.
- **Settings UI pattern**: `SettingsInterfaceViewModel` (`feature/settings/ui/src/commonMain/kotlin/com/grappim/taigamobile/feature/settings/ui/interfacescreen/SettingsInterfaceViewModel.kt`)
  already toggles crash reporting and gates its visibility on `crashReporter.isAvailable` — a new row
  would sit next to it, gated on an equivalent `isBiometricLockAvailable` check.
- **Per-platform capability gating pattern**: `isGithubOAuthSupported()` (`feature/login/ui/src/commonMain/.../GithubOAuthWebViewDialog.kt`)
  is an `expect`/`actual` returning `true` only on Android, `false` on iOS/JVM — the same shape fits
  "is biometric hardware+enrollment available on this platform."
- **Cold-start login gate**: `MainViewModel.initialNavState` (`composeApp/src/commonMain/kotlin/com/grappim/taigamobile/main/MainViewModel.kt:60-71`)
  already picks `LoginNavDestination` vs. the logged-in start destination from `authStorage.isLoggedIn`.
  A lock screen would be a third branch in the same `combine`, gated on
  `biometricLockEnabled && isLoggedIn && !unlockedThisSession`.
- **`MainActivity`** (`androidApp/src/main/kotlin/com/grappim/taigamobile/MainActivity.kt:20`) extends
  plain `ComponentActivity`, not `FragmentActivity`.
- **No app-lifecycle observer exists anywhere in the repo** — confirmed by grep
  (`ProcessLifecycleOwner`/`LifecycleEventObserver`/`Lifecycle.Event` : no hits outside `build/`).
  `MainActivity.onResume`/`onPause` exist but are only used today for the in-app-update checker
  (`:44-53`), not for detecting backgrounding app-wide.

---

## Per-platform mechanism

| Platform | Mechanism | Cost |
|---|---|---|
| Android | `androidx.biometric:biometric`'s `BiometricPrompt` | **New catalog dependency.** `BiometricPrompt` requires hosting from a `FragmentActivity` (or a `Fragment`) — `MainActivity` would need to become a `FragmentActivity`. That's a safe supertype widening (`FragmentActivity` extends `ComponentActivity`, so `enableEdgeToEdge()`/`installSplashScreen()`/Compose `setContent` all still apply) but it touches the app's single entry point, so it's a real change to verify, not a no-op. |
| iOS | `platform.LocalAuthentication.LAContext.evaluatePolicy` | No new dependency — ships with Kotlin/Native's Apple SDK bindings already. |
| Desktop/JVM | No standard OS-level biometric API exposed to the JVM | Report `isAvailable = false`, hide the toggle — same as `CrashReporterImpl`'s no-op JVM/desktop actual and `isGithubOAuthSupported()`'s `false` on JVM. Not a gap worth closing; there's no vendor asset to protect on desktop that isn't already behind the OS session lock. |

---

## Open decisions (need an answer before implementation starts)

1. **Fallback policy.** If the biometric check fails, or the user removes all enrolled
   fingerprints/Face ID after turning this on, what happens?
   - Retry-only (can lock the user out of the app entirely — bad if biometrics stop working)
   - Fall back to the Taiga account password
   - Fall back to device PIN/pattern (`BiometricPrompt.PromptInfo.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)` on Android; iOS's `LAContext` has an equivalent `deviceOwnerAuthentication` policy that already includes passcode fallback by default, vs. `deviceOwnerAuthenticationWithBiometrics` which doesn't)
2. **Re-lock trigger.** Cold-start-only gating is cheap (hooks into the existing `initialNavState`
   combine). Re-locking when the app *returns from background* — arguably the actual point of an
   app-lock — needs new infra: a `ProcessLifecycleOwner`-equivalent per platform to detect
   background→foreground and reset an in-memory "unlocked this session" flag. Decide whether v1 ships
   cold-start-only or includes this.
3. **`MainActivity` → `FragmentActivity` change**: confirm nothing else in the Compose/edge-to-edge/
   splash-screen setup assumes `ComponentActivity` specifically before committing to it.

---

## Testability

No Android or iOS unit-test source set exists in this repo (CLAUDE.md, by design). The DataStore
toggle and ViewModel wiring (`biometricLockEnabled` read/write, `initialNavState`'s new branch) are
testable in `jvmTest` with a fake, same as every other `TaigaSessionStorage` flag. The actual
`BiometricPrompt`/`LAContext` call sites are not automatable here — verification would be manual, on
a real device or emulator (see the `emulator-testing` skill; this project has no
`docs/EMULATOR_TESTING.md` yet, so that would be the first task to establish one).

---

## Rough sizing

- Storage + Settings toggle: small, an afternoon.
- Per-platform `expect`/`actual` prompt mechanism (Android `FragmentActivity` change + `BiometricPrompt`,
  iOS `LAContext`): the bulk of the work.
- Re-lock-on-resume infra (if in scope for v1): genuinely new cross-platform infrastructure, not a
  toggle flip — size unknown until decision 2 above is made.
- Fallback-policy UX (decision 1): blocks starting implementation at all until answered.

## Next step

Not started. Resume by: deciding items 1–3 above, then writing an actual task-broken-down
implementation plan (`plan.md` in this same folder, following the `docs/features/*/plan.md`
convention) before touching code.

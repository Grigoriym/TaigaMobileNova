# MASVS register

Profile: Android / iOS (JVM/desktop is outside MASVS — noted where relevant, not reviewed as a
MASVS target) · self-hosted, user-supplied server · reviewed 2026-08-09, STORAGE and CRYPTO.

Out of scope: MASVS-RESILIENCE (defends a vendor's assets against the device owner; this is a
self-hosted FOSS client where the device owner is the data owner and the source is public —
formal decision recorded in task 7 of `docs/security/masvs-review-plan.md`, not yet run).

## Accepted deviations

| Control | What we do instead | Bound | Why |
|---|---|---|---|
| MASVS-STORAGE-2 | Cached server URL (`DataStoreServerStorage`, `core/storage/.../server/DataStoreServerStorage.kt`) stored in plaintext DataStore | Value is a bare base URL (e.g. `https://api.taiga.io` or a self-hosted host) — no userinfo/credential embedded in it, confirmed by reading the only write path (`defineServer`) and its default (`getServerDefaultValue`) | Reveals which Taiga instance the user talks to, not a credential; unencrypted storage is proportionate to that sensitivity |

## Open

| Control | Finding | Where | Severity |
|---|---|---|---|
| MASVS-STORAGE-1 / MASVS-CRYPTO-2 | **iOS only** (Android fixed this task, see Notes; JVM/desktop is outside MASVS). Session `token` and `refresh_token` are stored as plaintext `stringPreferencesKey`s in an unencrypted DataStore `Preferences` file at `NSDocumentDirectory/auth_storage.preferences_pb` — no Keychain-backed wrapper. `PlatformStorageModule`'s iOS actual still wires a plain `AuthStorageImpl(createAuthDataStore(), NoopTokenCipher())`, same as JVM/desktop. The fix shape is the same one Android just got, ported to Keychain instead of `Cipher`/`KeyGenParameterSpec`: store the token as a Keychain item (`kSecAttrAccessibleWhenUnlockedThisDeviceOnly`) instead of inside the DataStore file, with a plaintext-fallback read for the migration window. | `core/storage/src/iosMain/kotlin/com/grappim/taigamobile/core/storage/di/StorageModule.ios.kt:31-34`; `core/storage/src/commonMain/.../auth/TokenCipher.kt` (the `TokenCipher` seam Android now uses) | Medium-High — bearer token grants full account access until it expires/is revoked server-side; refresh_token extends that. Deliberately scoped out of this task (user chose "Android only, iOS deferred" over a three-platform crypto change in one review task) — a real, sized fix, not a leftover. |

## Needs a device or an APK

| Control | Check | Why source can't answer it |
|---|---|---|
| MASVS-STORAGE-2 | Whether the release APK's backup exclusions (`data_extraction_rules.xml` / `backup_rules.xml`, added task 0) actually keep the auth DataStore file out of a real `adb backup` / cloud backup / D2D transfer | Needs a built release APK and a device to run `adb backup` / trigger Android's backup agent and inspect the resulting archive |
| MASVS-STORAGE-1 / MASVS-CRYPTO-2 | Whether `AndroidKeystoreTokenCipher`'s AES key is actually hardware-backed (TEE/StrongBox) as requested via `KeyGenParameterSpec`, not just requested | Needs a device — hardware enforcement isn't verifiable from source; also, there is no Android unit-test source set in this repo (CLAUDE.md, by design), so this class has no automated test at all, only the manual review below |
| MASVS-CRYPTO-2 | Whether the plaintext→ciphertext migration (existing installs' unprefixed token gets re-encrypted on next `setAuthCredentials`) actually fires for real installed users, vs. some cohort staying on plaintext indefinitely | Needs telemetry or a real upgrade test from a pre-cipher build — the migration is exercised in `AuthStorageImplTest` (jvmTest, `NoopTokenCipher`/`FakeTokenCipher`) but that proves the code path, not real-world convergence time |

## Notes

- **Logs**: `grep -rnE '(logcat|Timber|println|NSLog).{0,120}(token|apiKey|password|secret|cookie|Authorization)'`
  across all source sets returns nothing — no call site logs the session token, refresh token, or
  password. Verified statically.
- **`allowBackup` inversion — fixed, not left as a finding.** The release manifest
  (`androidApp/src/main/AndroidManifest.xml`) set `android:allowBackup="true"` with no
  `dataExtractionRules`/`fullBackupContent` at all, while the **debug** manifest
  (`androidApp/src/debug/AndroidManifest.xml:6-8`) was the one that turned backup off entirely —
  backwards, since debug is the build with nothing worth taking. Fixed this task: release now keeps
  `allowBackup="true"` (other cached data still benefits from backup/restore and device transfer)
  but adds `android:dataExtractionRules="@xml/data_extraction_rules"` (API 31+) and
  `android:fullBackupContent="@xml/backup_rules"` (API 24-30, matching `minSdk`) excluding the auth
  DataStore file and its legacy `SharedPreferencesMigration` source (`auth_storage.xml`) from cloud
  backup and device transfer by name. Verified the merged release manifest picks up both attributes
  (`processFdroidReleaseMainManifest` output). Per the skill's Step 4, a fixed finding leaves the
  register — this note exists only to explain why MASVS-STORAGE-2 has no Open row for backup.
- **No application-level cryptography existed before this task.** `grep -rl
  'Keystore\|SecretKey\|Cipher\.getInstance\|KeyGenParameterSpec'` and `grep -rl
  'Keychain\|kSecAttr\|SecItem'` across all source sets both returned nothing — no key material in
  source, build config, or `gradle/libs.versions.toml`. The `sha256Fingerprint` calls in
  `CompositeTrustManager` (`core/api`) are `MessageDigest`-based cert-pin hashing, a MASVS-NETWORK
  concern (task 2), not application data encryption — noted, not counted as a CRYPTO finding.
- **MASVS-STORAGE-1's plaintext-token finding was fixed for Android this task**, not left open. A
  `TokenCipher` seam was added (`core/storage/.../auth/TokenCipher.kt`, interface + `NoopTokenCipher`
  passthrough) and `AuthStorageImpl` now runs every read/write through it
  (`core/storage/.../auth/AuthStorage.kt`). Android's actual is `AndroidKeystoreTokenCipher`
  (`core/storage/src/androidMain/.../auth/AndroidKeystoreTokenCipher.kt`): AES/GCM with a
  Keystore-resident key (`KeyGenParameterSpec`, `BLOCK_MODE_GCM`, no padding, a fresh random IV per
  encryption via `cipher.iv` — never reused), ciphertext stored as `"v1:" + base64(iv + ciphertext)`.
  **Production migration**: `decrypt()` returns any value without the `"v1:"` prefix unchanged
  (a pre-cipher plaintext token from an already-installed user), so existing sessions keep working
  without a forced logout; the value gets encrypted in place the next time `setAuthCredentials` runs
  (i.e. the next token refresh), not on every read, to avoid a write-on-read side effect across the
  `tokenFlow`/`refreshTokenFlow` `Flow.map` collectors. A `"v1:"`-prefixed value that fails to decrypt
  (corrupt data, or a Keystore key invalidated by e.g. biometric enrollment change) returns `""`
  rather than propagating garbage as a bearer token — the user is treated as logged out instead of
  the app sending a broken `Authorization` header. **JVM/desktop and iOS both still use
  `NoopTokenCipher`** (identity passthrough) — JVM/desktop because it's outside MASVS scope by the
  skill's own Step 0, iOS by explicit choice this task (see the Open row above) rather than by
  oversight. Verified: `./gradlew :core:storage:jvmTest`, `ktlintCheck`, `koverXmlReport`/`:koverVerify`
  all green; `AndroidKeystoreTokenCipher` itself has no automated coverage (no Android unit-test
  source set in this repo) and is listed in "Needs a device" above.

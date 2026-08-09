# MASVS register

Profile: Android / iOS (JVM/desktop is outside MASVS — noted where relevant, not reviewed as a
MASVS target) · self-hosted, user-supplied server · reviewed 2026-08-09, STORAGE only.

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
| MASVS-STORAGE-1 | Session `token` and `refresh_token` are stored as plaintext `stringPreferencesKey`s in an unencrypted DataStore `Preferences` file, on **all three platforms** — no Keystore/Keychain-backed cipher over the value anywhere in the read or write path. Android: `context.preferencesDataStoreFile("auth_storage")` → `filesDir/datastore/auth_storage.preferences_pb`. JVM/desktop: `appDataDir()/auth_storage.preferences_pb`. iOS: `NSDocumentDirectory/auth_storage.preferences_pb` — confirmed no Keychain wrapper exists anywhere (`grep -rl 'Keychain\|kSecAttr\|SecItem'` across all source sets returns nothing); iOS gets the identical plaintext file every other platform gets. Readable by anything with the app's uid, or off a rooted device / unencrypted desktop disk. | `core/storage/src/commonMain/kotlin/com/grappim/taigamobile/core/storage/auth/AuthStorage.kt:20-52` (impl); `core/storage/src/androidMain/.../di/StorageModule.android.kt:65-75`; `core/storage/src/jvmMain/.../di/StorageModule.jvm.kt:59-61`; `core/storage/src/iosMain/.../di/StorageModule.ios.kt:60-64` (platform DataStore wiring) | Medium-High — bearer token grants full account access until it expires/is revoked server-side; refresh_token extends that. Deferred to task 1 (Cryptography) to decide *how* to key protection for this value rather than invent a scheme here. |

## Needs a device or an APK

| Control | Check | Why source can't answer it |
|---|---|---|
| MASVS-STORAGE-2 | Whether the release APK's backup exclusions (`data_extraction_rules.xml` / `backup_rules.xml`, added this task) actually keep the auth DataStore file out of a real `adb backup` / cloud backup / D2D transfer | Needs a built release APK and a device to run `adb backup` / trigger Android's backup agent and inspect the resulting archive |
| MASVS-STORAGE-1 | Keystore/Keychain hardware-backing guarantees (once task 1 adds a cipher) as actually enforced, not just requested via `KeyGenParameterSpec` | Needs a device — StrongBox/TEE presence and enforcement isn't verifiable from source |

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

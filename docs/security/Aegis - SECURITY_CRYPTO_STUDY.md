# Aegis — Security & Crypto Practice Study

An investigation of how [Aegis Authenticator](https://github.com/beemdevelopment/Aegis) (v3.4.2, `a9d45b3a`)
implements at-rest encryption, credential management, and app-level hardening — written as a
reference for porting the same patterns into another Android app.

Everything below was read out of this repo; file:line references point at the real implementation.
Aegis' own upstream design doc is [`docs/vault.md`](docs/vault.md) — this document covers what that
one does *not*: the runtime/lifecycle/OS-integration half of the security design, and the trade-offs.

---

## 1. Threat model (inferred)

What the design actually defends against:

| Threat | Defense |
|:--|:--|
| Attacker with the vault **file** (backup, cloud sync, ADB pull, stolen SD card) | AES-256-GCM over the whole content blob; key from scrypt(password) |
| Attacker with an **unlocked-but-idle device** | Auto-lock on minimize / screen-off / back; master key dropped from memory |
| **Screenshots / screen recording / task switcher** leakage | `FLAG_SECURE` on every window, on by default in release builds |
| **Malicious app** on device | No exported components that touch the vault; panic trigger requires a verified signer |
| **Coercion / border search** | Panic responder wipes the vault file |
| Biometric bypass via a **re-enrolled fingerprint** | KeyStore key invalidation is detected and the slot is disabled |

Explicitly **not** defended against: a rooted/compromised device while the vault is unlocked, and
memory forensics of the live process. Aegis says so; that's the honest boundary.

---

## 2. The core idea: envelope encryption with LUKS-style key slots

This is the single most valuable pattern to steal.

```
             ┌───────────────────────────────────────────┐
             │ vault file (aegis.json)                   │
             │                                           │
  password ──┼─► [PasswordSlot]  ─┐                      │
             │    scrypt+AES-GCM  │                      │
             │                    ├─► master key (256b) ─┼─► AES-256-GCM ─► db (ciphertext)
  biometric ─┼─► [BiometricSlot] ─┘                      │
             │    KeyStore+AES-GCM                       │
             └───────────────────────────────────────────┘
```

A random 256-bit **master key** encrypts the content. It is never derived from anything — it is
generated once (`MasterKey.generate()`, `CryptoUtils.generateKey()` at `CryptoUtils.java:103`) and
then **wrapped** (encrypted) once per credential into a *slot*.

Why this matters, concretely:

- **Changing the password does not re-encrypt the data.** It re-wraps a 32-byte key.
  See `SecurityPreferencesFragment.java:322-352` — derive new key, `slot.setKey(creds.getKey(), cipher)`,
  drop the old slot, add the new one, save. The multi-megabyte content blob is untouched.
- **Multiple unlock methods coexist** without storing the password anywhere.
- **Revoking biometrics = deleting one slot** + the KeyStore alias (`SecurityPreferencesFragment.java:245-256`).
- **Consequence to be aware of:** the master key is only as strong as the *weakest* slot. Aegis
  documents this explicitly in `docs/vault.md:83`.

The slot abstraction is a small class hierarchy — `Slot` (abstract, holds `_encryptedMasterKey` +
`CryptParameters`), `RawSlot`, `PasswordSlot`, `BiometricSlot` — and the base class only knows how to
do two things (`vault/slots/Slot.java:51-76`):

```java
public MasterKey getKey(Cipher cipher)      // unwrap: decrypt _encryptedMasterKey with a ready cipher
public void setKey(MasterKey key, Cipher c) // wrap:   encrypt master key with a ready cipher
```

**The subtle, important part**: `Slot` takes an already-initialized `Cipher`, not a key. That is what
makes the biometric slot work — the `Cipher` can come back from `BiometricPrompt` after the OS
authenticated the user. The crypto layer doesn't care where the cipher came from.

---

## 3. Primitives and parameters

`crypto/CryptoUtils.java:27-34`:

```java
public static final String CRYPTO_AEAD = "AES/GCM/NoPadding";
public static final byte CRYPTO_AEAD_KEY_SIZE   = 32; // 256-bit
public static final byte CRYPTO_AEAD_TAG_SIZE   = 16; // 128-bit tag
public static final byte CRYPTO_AEAD_NONCE_SIZE = 12; // 96-bit

public static final int CRYPTO_SCRYPT_N = 1 << 15;    // 32768
public static final int CRYPTO_SCRYPT_r = 8;
public static final int CRYPTO_SCRYPT_p = 1;
```

**AEAD choice.** AES-256-GCM everywhere. Integrity/authenticity come free, so a wrong password is
detected as a `BadPaddingException` (GCM tag failure) rather than producing garbage — mapped to a
dedicated `SlotIntegrityException` and distinguished from generic `SlotException` (`Slot.java:56-60`).
That distinction is what lets "try each password slot until one authenticates" be a safe loop
(`PasswordSlotDecryptTask.java:34-46`).

**scrypt parameters** are the same as Android's own FDE parameters. The doc notes the reason they
aren't higher: Android's per-app heap limit — N=2^15 with r=8 needs ~32 MB. Note `android:largeHeap="true"`
in the manifest, which is partly why this works at all.

**scrypt implementation is vendored**, not taken from the BouncyCastle provider:
`crypto/bc/SCrypt.java` + `crypto/bc/Salsa20Engine.java` are copies of BC sources, patched to use
`Integer.rotateLeft` directly (commit `5dfdbabf`) — a measurable KDF speedup. BouncyCastle *is* a
dependency (`bcprov-jdk18on:1.80`) and is used for Argon2 in importers; only the hot scrypt path is forked.

**Nonce handling — deliberately not hand-rolled** (`CryptoUtils.java:58-73`):

```java
// generate the nonce if none is given
// we are not allowed to do this ourselves as "setRandomizedEncryptionRequired" is set to true
if (nonce != null) { cipher.init(opmode, key, new GCMParameterSpec(TAG*8, nonce)); }
else               { cipher.init(opmode, key); }   // provider generates the IV
```

Encrypt path never supplies a nonce — the provider does, which for AndroidKeyStore keys is enforced
by `setRandomizedEncryptionRequired(true)`. Decrypt path supplies the stored nonce. This is the
correct way to make GCM nonce reuse structurally impossible.

**Documented residual risk**: 96-bit random nonces have a birthday bound; NIST says stay under 2^32
invocations per key. `docs/vault.md:27-42` states this openly and names XChaCha20-Poly1305 /
AES-GCM-SIV as the future fix. Good practice worth copying: write down the assumption you're relying on.

**One quirk**: `CryptoUtils.encrypt` splits the GCM tag off the ciphertext and stores it as a separate
field, then re-appends it on decrypt (`CryptoUtils.java:75-101`). Purely a file-format decision (the
tag lives in a `params` JSON object); it adds a `ByteArrayOutputStream` copy per decrypt. I would not
copy this — keep the tag appended, it's what every JCE provider expects.

---

## 4. File format

Single JSON file, `aegis.json`, in `context.getFilesDir()`:

```json
{
  "version": 1,
  "header": {
    "slots": [ /* one object per credential */ ],
    "params": { "nonce": "<hex 12B>", "tag": "<hex 16B>" }
  },
  "db": "<base64 ciphertext>"
}
```

- `slots: null, params: null` ⇒ vault is **plaintext** and `db` is a JSON object instead of a string
  (`VaultFile.java:62-77`). Encryption is genuinely optional in Aegis.
- Two independent version numbers: the **file format** version (`VaultFile.VERSION = 1`) and the
  **content** version (currently 3), so the container and the payload schema can evolve separately.
  Forward compat is a hard fail: `if (obj.getInt("version") > VERSION) throw` (`VaultFile.java:64`).
- Per-slot JSON carries everything needed to unwrap it — for a `PasswordSlot` that's `n`, `r`, `p`,
  `salt` (`PasswordSlot.java:34-48`). **Store KDF parameters with the ciphertext**, never as
  compiled-in constants, or you can never raise them.
- The header is **not authenticated**. Only the slots' wrapped keys and the `db` blob are. An attacker
  can flip bits in the header, but the worst outcome is a failed unwrap. Noted at `docs/vault.md:87-91`.

---

## 5. Slot types in detail

### 5.1 PasswordSlot — scrypt

`KeyDerivationTask.java:24-35` generates a **fresh 256-bit salt on every password set** and stores the
parameters into the slot as a side effect of `deriveKey(password, params)`. KDF runs on a background
thread with a boosted priority (`ProgressDialogTask.java:60-62`):

```java
Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE);
```

Unlock tries every password slot in turn and returns on the first one whose GCM tag verifies
(`PasswordSlotDecryptTask.java:34-46`). No "which slot are you?" hint is stored, and no password
verifier hash exists — authenticity of the AEAD *is* the password check.

### 5.2 BiometricSlot — Android KeyStore + BiometricPrompt

The whole biometric story is ~20 lines in `BiometricSlot.java` (it's just a `RawSlot`) plus the
KeyStore config, which is where the security actually lives (`crypto/KeyStoreHandle.java:44-68`):

```java
new KeyGenParameterSpec.Builder(id, PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
    .setBlockModes(BLOCK_MODE_GCM)
    .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
    .setUserAuthenticationRequired(true)      // ← the actual enforcement
    .setRandomizedEncryptionRequired(true)    // ← no caller-chosen IVs
    .setKeySize(256)
    .build()
```

The key is **hardware-backed, non-exportable, and unusable without a fresh biometric auth**. The
alias is the slot's UUID, so slot ↔ KeyStore key stay in sync.

Enrollment flow (`helpers/BiometricSlotInitializer.java:45-72`):
1. Create `BiometricSlot` → new UUID.
2. Generate KeyStore key under that UUID.
3. Build an **encrypt** cipher, hand it to `BiometricPrompt.authenticate(info, CryptoObject(cipher))`.
4. On success, the callback receives the *authorized* cipher and wraps the master key with it.
5. On cancel/failure, `reset()` deletes the orphan KeyStore key.

Unlock flow (`ui/AuthActivity.java:270-293` + `366-395`) is the mirror image with a **decrypt** cipher
built from the slot's stored nonce.

**The detail people get wrong**: using `CryptoObject` means the auth result is cryptographically
bound. A hooked "return true from onAuthenticationSucceeded" gives an attacker nothing, because
without a real auth the KeyStore refuses to let the cipher do work. If you take one thing from the
biometric code, take this.

**Key invalidation handling** (`KeyStoreHandle.java:90-103`) is the second detail:

```java
// try to initialize a dummy cipher and see if an InvalidKeyException is thrown
try { Cipher.getInstance(CRYPTO_AEAD).init(Cipher.ENCRYPT_MODE, key); }
catch (InvalidKeyException e) {
    // some devices throw a plain InvalidKeyException, not KeyPermanentlyInvalidatedException
    return true;
}
```

Adding a fingerprint invalidates the key. Aegis probes for it *proactively* and, if found, hides the
biometric button and shows an explanatory message instead of throwing at the user
(`AuthActivity.java:135-164`). Note the OEM-bug tolerance — catching the *base* exception type.
There's a matching workaround in `KeyStoreHandle.java:57-64` for buggy Keymaster HALs that wrap
errors in `ProviderException`.

### 5.3 The "backup password" slot

A `PasswordSlot` with `is_backup: true` (`PasswordSlot.java:80-86`). The point: your day-to-day unlock
can be a 6-digit PIN while **exports and Android backups get stripped down to a strong password only**.

`SlotList.exportable()` (`SlotList.java:81-100`) is the enforcement point:

- always strip **all** biometric slots (a KeyStore key is device-bound; a biometric slot in an export
  is dead weight that only widens the attack surface), and
- if a backup slot exists, strip the regular password slots too.

It's called on every export path (`VaultRepository.exportFiltered:166-169`) and by the Android backup
agent (`AegisBackupAgent.java:79`). This is a genuinely elegant answer to "weak local unlock vs.
strong off-device protection" and it costs almost no code.

---

## 6. Persistence and backups

**Atomic writes.** Every save goes through `androidx.core.util.AtomicFile`
(`VaultRepository.java:52-90`) with proper `failWrite()` on the error path. A crash mid-save cannot
truncate the vault. For a file that holds irreplaceable secrets, this is not optional.

**Three backup channels**, all of which get the `exportable()` treatment:
1. Manual export to a user-chosen SAF location.
2. Scheduled local backups with version retention (`VaultManager.scheduleBackup:164-185`).
3. **Android cloud backup / D2D transfer** via a custom `BackupAgent`.

The `AegisBackupAgent` (`AegisBackupAgent.java`) is worth studying:

- `allowBackup="true"` + `fullBackupOnly="true"` + a rules file that includes **only**
  `files/backup/aegis.json` and shared prefs (`res/xml/backup_rules.xml`) — the live
  `files/aegis.json` is *never* in scope.
- `onFullBackup` writes the *stripped, exportable* copy to that staging path, calls
  `super.onFullBackup()`, and deletes the staging dir in a `finally` (`:73-92`).
- The user preference is honored — **except** for device-to-device transfer, which is always allowed
  (`:48-56`), since D2D never leaves the user's hands.
- `onCreate` can't use Hilt because restore runs the app in a restricted mode; it constructs its
  dependencies manually and says why (`:36-39`). Easy trap to fall into.
- Every exception is caught, recorded into prefs as a `BackupResult`, and surfaced in the UI later —
  silent backup failure is treated as a bug class of its own.

Temp export files land in `cacheDir`, and the cache directory is wiped on every app start
(`AegisApplicationBase.java:56-57`).

---

## 7. Lock lifecycle — where most authenticator apps are weak

The master key lives only in the in-memory `VaultRepository` held by a singleton `VaultManager`.
Locking is literally `_repo = null` plus listener notification (`VaultManager.java:103-111`).

Four independently-toggleable auto-lock triggers (`Preferences.java:34-49`, a bitmask):

| Trigger | Mechanism |
|:--|:--|
| `AUTO_LOCK_ON_MINIMIZE` | `ProcessLifecycleOwner` observer on `ON_STOP` (`AegisApplicationBase.java:105-114`) |
| `AUTO_LOCK_ON_DEVICE_LOCK` | `BroadcastReceiver` for `ACTION_SCREEN_OFF`, registered `RECEIVER_NOT_EXPORTED` (`:49-51`) |
| `AUTO_LOCK_ON_BACK_BUTTON` | back handling in `MainActivity` |
| `AUTO_LOCK_OFF` | explicit opt-out |

Defaults: back button + device lock (`Preferences.java:189`).

**The `setBlockAutoLock` escape hatch** (`VaultManager.java:206-220`) solves a real problem: opening
the SAF document picker stops your process, which would instantly lock the vault mid-export. So
`fireIntentLauncher()` sets the flag before launching, and `AegisActivity.onResume` clears it
(`AegisActivity.java:86-89`). Anyone implementing lock-on-minimize will hit this exact bug.

**On lock, every activity finishes** via `AegisActivity.onLocked` — with a reflection hack calling the
private `finish(int)` with `FINISH_TASK_WITH_ACTIVITY = 2`, so the app doesn't vanish from Recents,
falling back to `finishAndRemoveTask()` on newer Android (`AegisActivity.java:94-108`). Cosmetic, but
illustrative of how much of this is UX polish around a security primitive.

**`FLAG_SECURE`** is applied centrally in the `AegisActivity` base class (`:71-74`) and again for
dialogs via `Dialogs.showSecureDialog` (`Dialogs.java:61-62`) — dialogs have their own windows and are
a classic miss. Default is build-type dependent: `true` in release, `false` in debug
(`app/build.gradle:79, 90`), so debugging isn't painful.

**Credentials never cross an IPC boundary.** `VaultFileCredentials` is `Serializable`, but it is only
ever passed in-process; nothing puts it in an `Intent` or `Bundle`. `getCredentials()` returns a
deep clone via Java serialization (`Cloner.java`) so callers mutating a `SlotList` can't corrupt the
live one.

---

## 8. Panic / duress wipe

`PanicResponderActivity` implements the [Guardian Project Panic](https://guardianproject.info/panic/)
protocol. The security-critical part is that it **verifies the caller's signing certificate** before
acting (`PanicResponderActivity.java:29-44`):

```java
TrustedIntents trustedIntents = TrustedIntents.get(this);
trustedIntents.addTrustedSigner(GuardianProjectRSA4096.class);
trustedIntents.addTrustedSigner(GuardianProjectFDroidRSA2048.class);
intent = trustedIntents.getIntentFromTrustedSender(this);   // null if untrusted
```

The activity is `exported="true"` — it has to be — so without signer pinning any installed app could
send the trigger and destroy the user's 2FA secrets. The action is also gated behind an opt-in
preference. Generalizable rule: **an exported component with a destructive effect must authenticate
its caller, and package name is not authentication.**

---

## 9. Password UX as a security control

- **zxcvbn** strength meter (`helpers/PasswordStrengthHelper.java`), with input capped at 64 chars
  because the library blows up on long inputs, and `strength.wipe()` afterwards.
- **PIN keyboard mode** (`pref_pin_keyboard`): numeric input, and the field is switched to a
  `no_autofill` variant so password managers don't interfere (`AuthActivity.java:77-90`). Enabling it
  requires confirming the current password *and* validating it's digits-only
  (`SecurityPreferencesFragment.java:151-176`).
- **Password reminder**: if you unlock with biometrics for long enough, you'll forget the password
  that's the only thing recoverable from a backup. Aegis periodically forces a reminder dialog before
  the biometric prompt and resets the timer on each password unlock
  (`AuthActivity.java:180-194, 352-354`).
- After 3 failed attempts, the field flips to `TYPE_TEXT_VARIATION_PASSWORD` so the user can
  reveal-and-check what they're typing (`AuthActivity.java:322-327`). No lockout, no delay — the KDF
  *is* the rate limiter, which is the right call for a local vault (rate-limiting is trivially
  bypassed by attacking the file directly).
- Passwords are handled as `char[]` end-to-end (`EditTextHelper.getEditTextChars`), avoiding
  `String` interning. **Note**: they're never actually zeroed afterwards — see limitations.
- Failed unlocks are recorded to an audit log (`_auditLogRepository.addVaultUnlockFailedPasswordEvent()`).

---

## 10. Backward-compatibility done carefully

A bug (`afb9e59`, issue #95) changed how passwords >64 bytes were converted to bytes, breaking those
vaults. The fix (`PasswordSlotDecryptTask.java:48-78`) is a template for handling this class of
problem:

1. Try the correct derivation.
2. On `SlotIntegrityException`, if the slot isn't yet marked `repaired` **and** the password is >64
   bytes, retry with the deprecated `toBytesOld()` path.
3. On success, **transparently re-wrap** the master key with the correct key and set `repaired: true`.
4. `AuthActivity.finish()` saves the vault when `isSlotRepaired()` (`:295-311`).

The deprecated function is kept, annotated `@Deprecated`, with a comment naming the offending commit.
Silent, one-time, self-healing migration — the user never learns there was a bug.

---

## 11. Testing and release practice

- `SCryptTest` runs RFC 7914 test vectors against the vendored implementation — mandatory when you
  fork a crypto primitive.
- `SlotTest`, `VaultTest` (unit) and `VaultRepositoryTest` (instrumented) cover the wrap/unwrap and
  round-trip paths.
- Committed sample vaults (plain + encrypted) in test resources double as format documentation.
- `docs/decrypt.py` — a standalone 93-line Python script that decrypts a vault given the password.
  This is a **format-independence guarantee** for users and an executable spec.
- Release: `minifyEnabled` + `shrinkResources` + R8, and reproducible-build settings
  (`cruncherEnabled = false`, no generated densities). README publishes the APK signing certificate
  fingerprints for `apksigner verify`.
- `android.webkit.WebView.MetricsOptOut = true` in the manifest.

---

## 12. Limitations and things I would do differently

Not criticism of Aegis so much as a list of "don't assume it's covered":

1. **The audit log DB is plaintext.** `AppDatabase` is stock Room, no SQLCipher (`database/AppDatabase.java`).
   It stores event types and timestamps, not secrets — but it does leak usage patterns to anyone with
   file access. If your equivalent log holds anything sensitive, encrypt it or fold it into the vault.
2. **No key/password zeroization.** `char[]` is used, but `Arrays.fill(password, '\0')` is never
   called, and `MasterKey` holds a `SecretKeySpec` that is never destroyed. Combined with
   `Cloner`'s serialize-to-`ByteArrayOutputStream` round-trip, master key bytes end up scattered
   across the heap. Real mitigation on the JVM is limited, but it's cheap to at least wipe passwords
   after derivation.
3. **The whole decrypted vault sits in memory as JSON `String`s** while unlocked. Fine given the
   stated threat model; be aware if yours is stricter.
4. **No root / tamper / debugger detection.** A deliberate choice (Aegis itself *uses* libsu to import
   from other apps' data dirs). Don't add attestation theater; do consider Play Integrity only if you
   have a server-side reason.
5. **GCM tag stored separately** from the ciphertext — extra copy on every decrypt, and it deviates
   from the JCE convention. Keep it appended.
6. **The header isn't authenticated.** Acceptable here; if your header carried anything
   security-relevant, pass it as GCM **AAD**.
7. **scrypt params are frozen at N=2^15** by Android's heap limit and there's no cost-upgrade-on-unlock
   path. Since params are stored per slot, adding one would be easy — and Argon2id is already a
   dependency for importers (`ui/tasks/Argon2Task.java`), so migration is within reach.
8. **`allowBackup="true"`** is a deliberate, carefully-fenced decision here (custom agent + stripped
   slot list + only a staging file in scope). Copying `allowBackup="true"` *without* all three pieces
   is how apps leak their databases to Google Drive.

---

## 13. Adoption checklist

If I were lifting this into another Android app, in priority order:

**Tier 1 — the architecture**
- [ ] Random master key + per-credential wrapped slots. Never derive the data key from the password.
- [ ] AES-256-GCM everywhere; let the provider pick the nonce on encrypt; store nonce + tag with the ciphertext.
- [ ] Store KDF params (algo, N/r/p or m/t/p, salt) **per slot in the file**. Fresh salt on every set.
- [ ] Distinguish "wrong credential" (AEAD tag failure) from "broken crypto" at the exception level.
- [ ] `AtomicFile` for every write.
- [ ] Two version numbers: container format and content schema. Hard-fail on future versions.

**Tier 2 — OS integration**
- [ ] KeyStore key with `setUserAuthenticationRequired(true)` + `setRandomizedEncryptionRequired(true)`;
      unlock via `BiometricPrompt.CryptoObject`, never a boolean callback.
- [ ] Probe for permanently-invalidated keys by init'ing a dummy cipher; catch base `InvalidKeyException`.
- [ ] `FLAG_SECURE` in a base Activity **and** for dialogs.
- [ ] Auto-lock: `ProcessLifecycleOwner` `ON_STOP` + `ACTION_SCREEN_OFF` receiver, each user-toggleable,
      **plus** a block-auto-lock flag around SAF/camera intents.
- [ ] Backup rules that include only a staging file, written by a `BackupAgent` that emits a
      stripped-down copy. Allow D2D unconditionally, cloud only on opt-in.

**Tier 3 — the polish that prevents real incidents**
- [ ] Separate "backup password" slot; strip device-bound slots from anything that leaves the device.
- [ ] Periodic password reminder if biometrics is the daily unlock.
- [ ] Signer verification on any exported component with a destructive effect.
- [ ] Wipe `cacheDir` at startup.
- [ ] A standalone decrypt script + committed sample files as an executable format spec.
- [ ] Test vectors for any crypto primitive you vendor.

### Kotlin sketch of the core

```kotlin
private const val AEAD = "AES/GCM/NoPadding"
private const val KEY_SIZE = 32
private const val TAG_BITS = 128

class MasterKey(val key: SecretKey) {
    companion object {
        fun generate() = MasterKey(
            KeyGenerator.getInstance("AES").apply { init(KEY_SIZE * 8) }.generateKey()
        )
    }
}

/** Everything needed to reverse one encryption, minus the key. */
data class CryptParams(val nonce: ByteArray, val tag: ByteArray)

/** Wrap: the caller supplies an *initialized* cipher — that's what allows a
 *  BiometricPrompt-authorized cipher to be passed straight in. */
fun wrap(master: MasterKey, cipher: Cipher): Pair<ByteArray, ByteArray> =
    cipher.iv to cipher.doFinal(master.key.encoded)   // tag stays appended

fun unwrap(wrapped: ByteArray, cipher: Cipher): MasterKey =
    MasterKey(SecretKeySpec(cipher.doFinal(wrapped), "AES"))

// Password slot: key from KDF (prefer Argon2id if you're not bound by Aegis' compat)
fun deriveKey(password: CharArray, salt: ByteArray, n: Int, r: Int, p: Int): SecretKey =
    SecretKeySpec(SCrypt.generate(password.toUtf8Bytes(), salt, n, r, p, KEY_SIZE), "AES")

// Biometric slot: KeyStore-backed, auth-gated
fun generateBiometricKey(alias: String): SecretKey =
    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
        init(KeyGenParameterSpec.Builder(alias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setRandomizedEncryptionRequired(true)
            .setKeySize(KEY_SIZE * 8)
            .build())
    }.generateKey()

// Encrypt path: never pass a nonce — required when setRandomizedEncryptionRequired(true)
fun encryptCipher(key: SecretKey): Cipher =
    Cipher.getInstance(AEAD).apply { init(Cipher.ENCRYPT_MODE, key) }

fun decryptCipher(key: SecretKey, nonce: ByteArray): Cipher =
    Cipher.getInstance(AEAD).apply {
        init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
    }
```

---

## 14. File map

| Concern | Path |
|:--|:--|
| Primitives, cipher construction | `app/src/main/java/com/beemdevelopment/aegis/crypto/CryptoUtils.java` |
| Master key wrapper | `.../crypto/MasterKey.java` |
| Android KeyStore | `.../crypto/KeyStoreHandle.java` |
| Vendored scrypt (patched BC) | `.../crypto/bc/SCrypt.java`, `.../crypto/bc/Salsa20Engine.java` |
| Slot base / password / biometric / list | `.../vault/slots/{Slot,PasswordSlot,BiometricSlot,SlotList}.java` |
| File format, header, encrypt/decrypt of content | `.../vault/VaultFile.java` |
| In-memory credentials | `.../vault/VaultFileCredentials.java` |
| Persistence, export paths | `.../vault/VaultRepository.java` |
| Lock state, backup scheduling | `.../vault/VaultManager.java` |
| Android backup/restore agent | `.../AegisBackupAgent.java` |
| Backup scope rules | `app/src/main/res/xml/backup_rules.xml`, `backup_rules_old.xml` |
| Unlock UI, biometric prompt | `.../ui/AuthActivity.java` |
| FLAG_SECURE, lock listener | `.../ui/AegisActivity.java` |
| Auto-lock wiring | `.../AegisApplicationBase.java`, `.../receivers/VaultLockReceiver.java` |
| Panic wipe + signer pinning | `.../ui/PanicResponderActivity.java` |
| Credential management UI | `.../ui/fragments/preferences/SecurityPreferencesFragment.java` |
| KDF / unlock background tasks | `.../ui/tasks/{KeyDerivationTask,PasswordSlotDecryptTask,Argon2Task,PBKDFTask}.java` |
| Biometric slot enrollment | `.../helpers/BiometricSlotInitializer.java` |
| Upstream design doc | `docs/vault.md`, `docs/decrypt.py` |
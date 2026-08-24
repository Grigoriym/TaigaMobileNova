# KeePassDX — Cryptography & Secret-Handling Architecture

Study notes on how encryption is structured in this codebase, written for someone who wants
to borrow patterns for another Android app.

Analysed at commit `e94bf9307` (master, 2026-06-28). All references are `path:line`.

---

## 1. Module topology

Crypto is deliberately split across three layers, and this separation is the single most
reusable idea in the repo:

| Module | Package | Responsibility | Android deps |
|---|---|---|---|
| `:crypto` | `com.kunzisoft.encrypt` | Algorithms only. Cipher/hash factories, Argon2 + AES-KDF JNI bindings, asymmetric signing. Knows nothing about KeePass. | NDK only |
| `:database` | `…keepass.database.crypto` | Format layer. KDBX key hierarchy, KDF parameter serialization, authenticated block streams. | none meaningful |
| `:app` | `…keepass.biometric`, `…keepass.timeout` | Platform layer. Android Keystore, BiometricPrompt, clipboard/screenshot hardening. | full |

Consequences worth copying:

- The entire cipher surface of the app is **56 lines** (`crypto/…/CipherFactory.kt`). Every
  algorithm decision is in one auditable file.
- `:crypto` and `:database` are JVM-testable; there are instrumented tests at
  `crypto/src/androidTest/java/com/kunzisoft/encrypt/` (`AESTest`, `Argon2Test`, `SignatureTest`)
  and `database/src/test/java/com/kunzisoft/keepass/tests/crypto/`.
- Swapping a KDF or cipher never touches UI code.

---

## 2. The key hierarchy (KDBX 4)

```
password (CharArray)  ─ SHA-256 ─┐
key file (raw/hex/XML) ─ normalize ─┤
hardware key response  ─ SHA-256 ─┘
                                  │
                        SHA-256(concat)          = masterKey        (32 B)
                                  │
                        KDF (Argon2id / AES-KDF) = transformedKey   (32 B)
                                  │
      cmpKey = masterSeed(32) ‖ transformedKey(32) ‖ [1 byte]
                                  │
              ┌───────────────────┴────────────────────┐
     SHA-256(cmpKey[0..63])                   SHA-512(cmpKey[0..64])
              │                                        │
          finalKey                                  hmacKey
     (data encryption)                     (authentication, per-block)
```

### 2.1 Credential normalization — `database/…/element/MasterCredential.kt`

Every credential becomes exactly 32 bytes *before* being combined:

- **Password** (`:167`): encode with the DB's charset → `SHA-256`. The intermediate byte array
  and the `ByteBuffer` backing array are both zeroed (`:176-179`).
- **Key file** (`:192`): tries XML keyfile (v1/v2) → raw 32 bytes → hex-decoded 64 bytes →
  otherwise `SHA-256` of the file content. The XML parser explicitly enables
  `FEATURE_SECURE_PROCESSING` to block XXE (`:236-241`).
- **Hardware key** (`:228`): `SHA-256` of the YubiKey challenge-response.

Fixed-length inputs mean the later concatenation is unambiguous — no length-extension or
delimiter confusion between "pass:word + keyfile" and "pass + word:keyfile".

### 2.2 Master key — `database/…/element/database/DatabaseKDBX.kt:297`

```kotlin
private fun composedKeyToMasterKey(passwordData, keyFileData, hardwareKeyData) =
    HashManager.hashSha256(passwordData, keyFileData, hardwareKeyData)
```

`hashSha256` (`crypto/…/HashManager.kt:42`) is a vararg that skips nulls, so absent factors
simply contribute nothing.

### 2.3 Final key + MAC key — `DatabaseKDBX.kt:629-659`

```kotlin
var transformedMasterKey = kdfEngine.transform(masterKey, kdfParameters)
if (transformedMasterKey.size != 32) transformedMasterKey = hashSha256(transformedMasterKey)

val cmpKey = ByteArray(65)
System.arraycopy(masterSeed, 0, cmpKey, 0, 32)       // fresh random on every save
System.arraycopy(transformedMasterKey, 0, cmpKey, 32, 32)
finalKey = resizeKey(cmpKey, encryptionAlgorithm.cipherEngine.keyLength())

cmpKey[64] = 1                                        // domain separation byte
hmacKey = SHA-512(cmpKey)
// finally { cmpKey.clear(); transformedMasterKey.clear() }
```

**The pattern:** one expensive KDF invocation yields two independent keys, separated by a
trailing constant byte and by different hash functions/lengths. The encryption key never
equals the MAC key, and neither can be derived from the other.

`resizeKey` (`:661`) is a small HKDF-ish expander: SHA-256 for ≤32 bytes, SHA-512 above,
falling back to iterated HMAC if a cipher ever wants more than 64 bytes.

### 2.4 Per-block MAC keys — `database/…/crypto/HmacBlock.kt`

```kotlin
fun getHmacKey64(key: ByteArray, blockIndex: ByteArray): ByteArray =
    SHA-512(blockIndex ‖ key)
```

Each 8 KiB block is authenticated under its **own** key. Blocks therefore can't be reordered,
duplicated, or spliced between files — the index is bound into the key itself, not just into
the MAC input (though it's in both). The file header uses the reserved index
`0xFFFFFFFFFFFFFFFF` (`UnsignedLong.MAX_BYTES`).

---

## 3. Read path — `database/…/file/input/DatabaseInputKDBX.kt:124-215`

```
1. header.loadFromFile()          → header bytes + SHA-256, via DigestInputStream
2. assignMasterKey() ; makeFinalKey(header.masterSeed)
3. compare stored SHA-256 vs computed  → mismatch = corrupt header
4. compute header HMAC (block index MAX) vs stored → mismatch = WRONG CREDENTIALS
5. HmacBlockInputStream (verify each block)  →  CipherInputStream (decrypt)
6. GZIP  →  inner header  →  XML pull parser
7. protected XML values ← ChaCha20/Salsa20 keystream, applied in document order
```

Two properties worth internalizing:

**Verify before decrypt.** Step 4 is the actual password check, and it happens before a single
byte of ciphertext is fed to a cipher. Step 5 verifies each block's MAC *before* handing it to
`CipherInputStream` — `HmacBlockInputStream.readSafeBlock()` (`stream/HmacBlockInputStream.kt:86-132`)
reads the stored HMAC, recomputes over `index ‖ size ‖ data`, throws `IOException("Invalid Hmac")`
on mismatch, and only then lets the buffer be consumed. This is encrypt-then-MAC done properly:
a chosen-ciphertext attacker never reaches the padding logic.

**Length is authenticated too.** The 4-byte block size is inside the MAC input, and a
zero-length block terminates the stream. Truncation is detectable.

### 3.1 Legacy KDBX 3.1 contrast — `:158-175`

The pre-4.0 branch has `HashedBlockInputStream` — an *unkeyed* SHA-256 per block — plus a
32-byte `streamStartBytes` sentinel compared after decryption. That's integrity-only and
malleable: anyone can recompute the hashes. It's also decrypt-then-verify. This is a KDBX 3.1
spec limitation faithfully reproduced, not a defect in this code, and it's exactly why KDBX 4
moved to HMAC. **Do not model new work on this branch.**

---

## 4. Write path — `database/…/file/output/DatabaseOutputKDBX.kt`

`setIVs()` (`:288-324`) regenerates, on **every single save**, from one `SecureRandom`:

- `masterSeed` (32 B)
- `encryptionIV` (cipher-dependent length: 16 B, or 12 B for ChaCha20)
- KDF salt/seed, via `randomizeKdfParameters()` → `Argon2Kdf.randomize()` / `AesKdf.randomize()`
- `innerRandomStreamKey` (64 B for ChaCha20 in v4; 32 B for Salsa20 in v3.1)
- `streamStartBytes` (v3.1 only)

So two saves of identical content produce completely unrelated ciphertext, and no key/IV pair
is ever reused across saves.

Header hash and header HMAC are produced by **stacking output streams**
(`DatabaseHeaderOutputKDBX.kt:44-64`):

```kotlin
dos = DigestOutputStream(outputStream, SHA-256)   // hash of header
mos = MacOutputStream(dos, hmac)                  // HMAC of same bytes
// … write header fields to mos …
hashOfHeader = dos.messageDigest.digest()         // :104
headerHmac  = mos.mac                             // :105
```

One pass over the header bytes, two digests, no buffering the header in memory twice. Then
`writeDatabase` (`:80-111`) writes `hashOfHeader ‖ headerHmac`, and wraps the body as
`CipherOutputStream(HmacBlockOutputStream(out, hmacKey))` — MAC applied to the ciphertext,
matching the read path.

`HmacBlockOutputStream.close()` (`stream/HmacBlockOutputStream.kt:38-49`) flushes the partial
block and then writes a second, empty block as the explicit end-of-stream terminator.

---

## 5. Key derivation functions

### Argon2 — `database/…/crypto/kdf/Argon2Kdf.kt`

- Both **Argon2d** and **Argon2id**, selected by UUID (`:141-180`).
- Parameters: `S` salt, `P` parallelism, `M` memory, `I` iterations, `V` version (0x10–0x13).
- Defaults (`:195-206`): 3 iterations, 16 MiB, parallelism 4, 32-byte fresh salt per save.
- `transform()` (`:54`) reads params, converts memory from bytes to 1 KiB blocks, delegates to
  `Argon2Transformer` → JNI.
- Available RAM is checked before running: `DatabaseInputKDBX.setMethodToCheckIfRAMIsSufficient`
  (`:118-122`) lets the app refuse a KDF that would OOM the device.

### AES-KDF — `database/…/crypto/kdf/AesKdf.kt` + `crypto/…/aes/AESTransformer.kt`

Legacy, compatibility only. 500 000 rounds by default, 32-byte random seed per save.
The construction: the 32-byte master key is repeatedly encrypted with **AES-256/ECB/NoPadding**
under the seed as key, then hashed with SHA-256 (`AESTransformer.kt:66-81`).

Two implementation details worth noting:

- **Fallback that actually works.** `AESTransformer.transformKey()` (`:34-43`) tries JNI first and
  catches *any* exception to fall back to `transformKeyInJVM()`. Unlike Argon2, this KDF is fully
  functional without the native library.
- **The native path splits the work across two pthreads** (`crypto/src/main/jni/aes/aes_jni.c:426-455`),
  one per 16-byte half of the key — the halves are independent under ECB, so the round loop
  parallelizes perfectly. It also calls `secure_wipe_memory()` on the AES key schedule before
  returning, which is the C-side equivalent of the `ArrayUtil.clear()` discipline in §7.

Being ECB-based and non-memory-hard is precisely why this KDF was superseded by Argon2; the
parallelism above is also available to an attacker's GPU.

### Parameter serialization — `database/…/crypto/VariantDictionary.kt`

A typed key→value map (UInt32/UInt64/Int32/Int64/Bool/String/ByteArray) with its own binary
encoding. This is how KDF parameters travel in the file header without a schema change every
time a KDF gains a knob. If you ever need forward-compatible crypto parameters in your own
format, this 258-line file is a good template.

---

## 6. Ciphers — `database/…/crypto/` + `crypto/…/CipherFactory.kt`

| Algorithm | Transformation | Key | IV |
|---|---|---|---|
| AES-256 | `AES/CBC/PKCS5Padding` | 32 B | 16 B |
| Twofish | `Twofish/CBC/PKCS7PADDING` (BC) | 32 B | 16 B |
| ChaCha20 | `Chacha7539` (BC) | 32 B | 12 B |

Selected by UUID in `EncryptionAlgorithm.kt`; `CipherEngine.keyLength()` is hardcoded to 32 for
all three.

**Native fast path with graceful fallback** — `CipherFactory.kt:23-38`:

```kotlin
val cipher = if (forceNative || NativeLib.loaded()) {
    try { Cipher.getInstance(transformation, AESProvider()) }     // JNI-backed JCE provider
    catch (e: Exception) { Cipher.getInstance(transformation) }   // fall back to platform
} else Cipher.getInstance(transformation)
```

`NativeLib` (`crypto/…/NativeLib.kt`) caches the `System.loadLibrary` result so a missing `.so`
degrades to the JCE implementation instead of crashing. Note Argon2 has **no** such fallback —
`Argon2Transformer.kt:15` calls `NativeLib.init()` and goes straight to JNI.

**Inner random stream** — `CrsAlgorithm.kt` + `HashManager.kt:70-109`. Protected XML field values
get a second keystream layer inside the already-encrypted file:

- Salsa20: key = `SHA-256(streamKey)`, with the **KeePass-specified constant IV**
  `E8 30 09 4B 97 20 5D 2A`. A fixed IV is safe here only because the key is random per save.
- ChaCha20: `SHA-512(streamKey)` split into a 32-byte key and 12-byte IV.

Which fields get this treatment is configurable per database — `MemoryProtectionConfig.kt`
defaults to protecting **Password** and **Notes** only.

---

## 7. Secret hygiene in memory

This is the most portable discipline in the codebase, and it costs nothing to adopt.

**`CharArray` / `ByteArray`, never `String`.** Passwords flow as `CharArray` from the UI through
`MasterCredential` into the hasher. `ProtectedString`
(`database/…/element/security/ProtectedString.kt:34`) stores `charArrayValue: CharArray` with a
private setter, and copies defensively in every constructor.

**Explicit wiping** — `database/…/utils/ArrayUtil.kt:22-45`:

```kotlin
const val charNull = '\u0000'
fun CharArray.clear()     = fill(charNull)
fun ByteArray.clear()     = fill(0)
fun StringBuilder.clear() { for (i in indices) setCharAt(i, charNull) }
```

Called consistently and in `finally` blocks: `makeFinalKey` wipes `cmpKey` and the transformed
key; `HmacBlockInputStream` wipes each per-block key after use (`:116`); `MasterCredential.clear()`
(`:125`) nulls password, keyfile and hardware key together.

**Caveat:** `ProtectedString` is *zeroable*, not *encrypted in memory*. Unlike desktop KeePass's
`ProtectedBinary` (which XORs against a session pad), values sit in plaintext on the heap until
cleared. On Android that's a reasonable trade; just don't over-claim it.

---

## 8. Attachment cache encryption

Large attachments spill to disk, so they're encrypted with an **ephemeral session key** that
exists only while the database is open.

- `BinaryCache.kt:11` — `loadedCipherKey: LoadedKey`, generated at DB open, gone at close.
- `LoadedKey.kt:12-16` — `KeyGenerator.getInstance("Blowfish").generateKey()` + 8-byte
  `SecureRandom` IV.
- `BinaryFile.kt:73-89` — reads/writes through `CipherInputStream`/`CipherOutputStream` wrapped
  in Base64, so cache files on disk are unreadable once the DB is locked.
- `BinaryCache.getBinaryData()` (`:17`) keeps small blobs in RAM (`BinaryByte`) and only spills
  larger ones to `BinaryFile`.

The *pattern* — ephemeral key bound to the unlocked-session lifetime — is excellent and worth
copying. The *instantiation* has two problems (see §12): Blowfish, and a single IV reused for
every attachment.

---

## 9. Android Keystore + biometric unlock — `app/…/biometric/DeviceUnlockManager.kt`

The design: never store the database password. Store it **encrypted under a hardware-backed key
that only a biometric/credential prompt can use**.

**Key generation** (`:97-135`):

```kotlin
KeyGenParameterSpec.Builder(ALIAS, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
    .setBlockModes(BLOCK_MODE_CBC)
    .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7)
    .setUserAuthenticationRequired(true)
    // API 30+: device credential without requiring an enrolled biometric
    .setUserAuthenticationParameters(0, AUTH_DEVICE_CREDENTIAL)
    // API 28+ with FEATURE_STRONGBOX_KEYSTORE: put it in the secure element
    .setIsStrongBoxBacked(true)
    .build()
```

**Persistence** — `app/…/app/database/CipherDatabaseEntity.kt`: a Room row of
`(database_uri, encrypted_value, specs_parameters)`. Only ciphertext + IV. The IV is captured
from `cipher.parameters.getParameterSpec(IvParameterSpec::class)` after encryption (`:218`),
because the Keystore generates it — you cannot supply your own.

**The part most implementations get wrong** — invalidation recovery (`:188-203`, `:269-289`):

```kotlin
catch (e: KeyPermanentlyInvalidatedException) {   // user enrolled a new fingerprint
    if (firstLaunch) {
        deleteAllEntryKeysInKeystoreForBiometric(appContext)  // drop key AND all ciphertexts
        initDecryptData(ivSpecValue, firstLaunch = false, action)   // retry exactly once
    } else throw e
}
```

`UnrecoverableKeyException` gets the same treatment. The `firstLaunch` flag bounds the recursion
to one retry. `deleteAllEntryKeysInKeystoreForBiometric` (`:419`) deletes the Keystore key and
then, in a `finally`, purges every stored ciphertext — because those are now permanently
undecryptable and keeping them just produces confusing errors forever.

There's also a three-way API-level split that's tedious to rediscover: biometric vs.
device-credential vs. "device credential *is* a biometric operation on R+"
(`:450-491`), plus `setUserAuthenticationValidityDurationSeconds(5)` as the deprecated M–Q path.

---

## 10. Passkeys / asymmetric signing — `crypto/…/Signature.kt`

Supports **ES256** (`-7`, P-256 ECDSA), **RS256**, and **EdDSA** via BouncyCastle:

- `generateKeyPair(keyTypeIdList)` (`:189`) — picks the first supported algorithm from the
  relying party's preference list.
- `sign(privateKeyPem: CharArray, message)` (`:76`) — note the private key is handled as
  `CharArray`, consistent with §7.
- `convertPublicKeyToMap()` (`:239`) — emits COSE key maps for WebAuthn.
- `SigningInfo.getAllFingerprints()` (`:365`) — SHA-256 fingerprints of the calling app's signing
  certs, used to bind a passkey to a verified caller.

Private keys never touch the Keystore — they're stored as **protected custom fields on a normal
KDBX entry** (`database/…/model/PasskeyEntryFields.kt:84`):

```kotlin
ProtectedString(enableProtection = true, passkey.privateKeyPem)
```

So a passkey inherits the entire hierarchy above: inner-random-stream obfuscation inside the XML,
then the file cipher, then the HMAC. `Passkey.clear()` (`model/Passkey.kt:48`) wipes the PEM
`CharArray` when done. This is a different trade-off from §9 — portability across devices and
sync, at the cost of hardware key isolation.

---

## 11. Platform hardening (app layer)

| Concern | Implementation |
|---|---|
| Screenshots / recents | `FLAG_SECURE` toggled in `app/…/utils/AppUtil.kt:161-163`, applied via `StylishActivity` |
| Clipboard | `ClipboardHelper.kt:73-76` sets `android.content.extra.IS_SENSITIVE` on the `ClipData` (suppresses the paste preview); `timeoutCopyToClipboard` schedules a `ClearClipboardTask` that calls `clearPrimaryClip()` on P+ |
| Idle lock | `timeout/TimeoutHelper.kt` — configurable app timeout closing the database |
| Password generation | `password/PasswordGenerator.kt:138` — `SecureRandom`, not `Random` |

---

## 12. Assessment: what to take, what to leave

### Take

1. **Three-layer module split** (§1). Algorithms / format / platform. Keeps the auditable
   surface tiny.
2. **Two keys from one KDF via domain separation** (§2.3). The `cmpKey[64] = 1` trick, or any
   equivalent (`HKDF-Expand` with distinct `info` strings) — never reuse one key for both
   encryption and authentication.
3. **`HmacBlockInputStream` / `HmacBlockOutputStream`** (§3, §4). ~150 lines each, format-agnostic,
   streaming authenticated framing with per-block keys, verify-before-decrypt, authenticated
   lengths, explicit terminator block. Directly liftable.
4. **`DigestOutputStream` + `MacOutputStream` stacking** (§4) to compute a hash and a MAC over
   the same bytes in one pass.
5. **Regenerate every nonce/salt/seed on every write** (§4).
6. **`ArrayUtil` wiping + `CharArray` everywhere** (§7). Free, and it makes secret lifetime
   visible in the code.
7. **Keystore invalidation recovery** (§9). Copy the `firstLaunch` retry + purge-all-ciphertexts
   logic verbatim; it's the difference between "biometrics stopped working, reinstall the app"
   and a graceful re-enrollment.
8. **Ephemeral session key for on-disk caches** (§8).
9. **RAM check before running a memory-hard KDF** (§5).
10. **`VariantDictionary`** (§5) if you need versionable crypto parameters in a file format.

### Leave / fix before reusing

1. **`LoadedKey.BINARY_CIPHER = "Blowfish/CBC/PKCS5Padding"`** — 64-bit block cipher (birthday
   bound ~32 GB), unauthenticated, and **the same IV is reused for every attachment in a session**
   (`BinaryFile.kt:76,88` reinit with the same `cipherKey.iv`). Under CBC that leaks equality of
   identical plaintext prefixes across attachments. Use AES-GCM with a per-file random nonce.
2. **CBC + separate HMAC generally.** Mandated by the KDBX spec here. For greenfield work use an
   AEAD (AES-GCM or ChaCha20-Poly1305) and skip the entire two-key construction.
3. **The KDBX 3.1 read path** (§3.1) — unauthenticated, decrypt-then-verify.
4. **`CipherFactory`'s global provider mutation** (`:17-20`): `Security.removeProvider(BC);
   Security.addProvider(BC)` runs as a side effect of first touching the object, mutating
   JVM-global state from an `init` block. Prefer passing a `Provider` instance explicitly (which
   the rest of the file already does).
5. **Argon2 defaults of 16 MiB / t=3** are the KeePass compatibility defaults, on the low side
   for a new design. OWASP-style guidance is ≥19 MiB and tuned upward to your device budget.
6. **AES-KDF** — compatibility only; never a default for new code.
7. **Vendored Argon2 C** (`crypto/src/main/jni/argon2/`) means you own its update path. Consider
   `androidx.security` or a maintained JVM Argon2 binding unless you specifically need the
   native speed.
8. **`ProtectedString` is not memory-encrypted** (§7) — don't advertise it as such.

---

## 13. File index

**`:crypto`**
```
CipherFactory.kt          AES / Twofish / ChaCha20 construction, native fallback
HashManager.kt            SHA-256/512 helpers, Salsa20 & ChaCha20 inner streams
Signature.kt              ES256 / RS256 / EdDSA, COSE encoding, APK fingerprints
NativeLib.kt              lazy System.loadLibrary with cached failure
StreamCipher.kt           thin BouncyCastle StreamCipher wrapper
aes/, argon2/             JNI bindings + JCE provider shim
src/main/jni/             vendored aes + argon2 C sources, CMake
```

**`:database` — crypto**
```
crypto/EncryptionAlgorithm.kt   UUID → engine mapping
crypto/CipherEngine.kt          key/IV length contract
crypto/{Aes,Twofish,ChaCha20}Engine.kt
crypto/CrsAlgorithm.kt          inner random stream selection
crypto/HmacBlock.kt             per-block key derivation + Mac construction
crypto/VariantDictionary.kt     typed serializable parameter map
crypto/kdf/{Argon2Kdf,AesKdf,KdfEngine,KdfFactory,KdfParameters}.kt
```

**`:database` — streams & elements**
```
stream/HmacBlockInputStream.kt    authenticated read framing
stream/HmacBlockOutputStream.kt   authenticated write framing
stream/HashedBlock*Stream.kt      legacy KDBX 3.1 unkeyed framing
stream/MacOutputStream.kt         MAC-computing passthrough stream
element/MasterCredential.kt       credential normalization
element/database/DatabaseKDBX.kt  key hierarchy (deriveMasterKey, makeFinalKey)
element/security/ProtectedString.kt, MemoryProtectionConfig.kt
element/binary/{BinaryCache,LoadedKey,BinaryFile}.kt   attachment cache encryption
file/input/DatabaseInputKDBX.kt, file/output/DatabaseOutputKDBX.kt
file/DatabaseHeaderKDBX.kt, file/output/DatabaseHeaderOutputKDBX.kt
utils/ArrayUtil.kt                clear() extensions
```

**`:app`**
```
biometric/DeviceUnlockManager.kt     Keystore key lifecycle, invalidation recovery
biometric/DeviceUnlockFragment.kt    BiometricPrompt + CryptoObject
app/database/CipherDatabaseEntity.kt Room storage of (uri, ciphertext, iv)
timeout/{TimeoutHelper,ClipboardHelper}.kt
utils/AppUtil.kt                     FLAG_SECURE
password/PasswordGenerator.kt        SecureRandom generation
```

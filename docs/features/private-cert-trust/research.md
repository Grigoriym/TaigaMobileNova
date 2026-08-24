# Self-Signed / Private-CA Certificate Trust — Research

## Background

Reported in [#322](https://github.com/Grigoriym/TaigaMobileNova/issues/322): a self-hosted Taiga
instance using a cert issued by a private CA fails to connect on Android with an opaque
"Connection error". Root cause: Android excludes user-installed CAs from app trust by default
(since API 24) unless the app opts in via `network_security_config.xml`, and this project's
`ErrorMappingPlugin`/`NetworkErrorMapper` collapsed `SSLHandshakeException` into the same generic
bucket as any other I/O failure (fixed separately — see `ERROR_SSL_CERTIFICATE` in
`NetworkException.kt`).

This doc is a scratchpad of findings on how other self-hosted-client Android apps handle
connecting to a server with an untrusted certificate, gathered before deciding what (if anything)
TaigaMobileNova should build. **No decision has been made yet** — this is investigation only.

Three known patterns going in:

- **(A) Do nothing** — tell users to get a properly-trusted cert (Let's Encrypt, reverse proxy).
- **(B) `network_security_config.xml`** trusting user-installed CAs app-wide.
- **(C) Trust-on-first-use (TOFU) per-server certificate pinning** — show the user the cert
  fingerprint on first connect, let them accept it, pin that exact cert for that exact host going
  forward, independent of the system/user CA store.

---

## nextcloud-android

**Pattern used:** hybrid of B + C, not just one of the three.

### Layer 1 — Pattern B (`network_security_config.xml`)

`app/src/main/res/xml/network_security_config.xml`:

- Global base-config, no domain scoping, applies in release builds (no `<debug-overrides>`)
- Trusts `src="system"` and `src="user"` CAs, plus `cleartextTrafficPermitted="true"`
- Only governs components using the platform default `SSLContext`/trust manager (WebView's normal
  TLS path, any library that doesn't install its own `SSLSocketFactory`)

### Layer 2 — Pattern C (TOFU pinning — the actual mechanism for the real API client)

Classes live in the `com.github.nextcloud:android-library` dependency (not in the app repo's
source tree — found by decompiling the AAR from the Gradle cache): `NetworkUtils`,
`AdvancedX509TrustManager`, `AdvancedSslSocketFactory`.

- `NextcloudClient$Companion.getOkHttpClient` builds an `AdvancedX509TrustManager` backed by a
  "known servers" `KeyStore` and installs it via
  `OkHttpClient.Builder.sslSocketFactory(sslSocketFactory, trustManager)` — the modern OkHttp
  REST client uses this custom trust manager, not just legacy code.
- Same trust manager is also wired into the legacy Apache-HttpClient path
  (`OwnCloudClientFactory.createOwnCloudClient` → `NetworkUtils.registerAdvancedSslContext`).
- Dialog: `SslUntrustedCertDialog.kt` (app repo) + `X509CertificateViewAdapter.java` — shown on
  both native TLS failures and WebView `onReceivedSslError` (`NextcloudWebViewClient.kt`).

**Trust-decision logic** (`AdvancedX509TrustManager.checkServerTrusted`, decompiled):

1. If the presented cert is already in the known-servers store → trust immediately, **no further
   checks at all** (skips validity/expiry check and skips standard chain validation entirely).
2. Otherwise, falls through to `TrustManagerFactory.init(null)` (system default CAs only — not
   user-installed CAs at this layer) + explicit `checkValidity()`; failures are bundled into a
   `CertificateCombinedException` and surfaced to the dialog.

**Dialog content on accept/reject:**

- Shows: Subject CN/O/OU/C/ST/L, Issuer CN/O/OU/C/ST/L, validity dates, fingerprints (SHA-256,
  SHA-1, MD5), signature algorithm.
- Accept → `NetworkUtils.addCertToKnownServersStore(cert, context)`.
- Reject → `sslErrorHandler.cancel()` / dialog cancel, connection aborted.

**Persistence:**

- A Java `KeyStore` (default type, e.g. BKS), file `knownServers.bks`, stored via
  `context.getFilesDir()` / `context.openFileOutput(..., MODE_PRIVATE)` — app-private storage.
- KeyStore password is the hardcoded literal `"password"` (`LOCAL_TRUSTSTORE_PASSWORD`) — low
  real-world impact since the file is already sandboxed, but sloppy.
- Alias = `Integer.toString(certificate.hashCode())`.

**Scope: per-certificate, not per-(host, certificate).**

- Trust is keyed only by the certificate itself — `isKnownServer()` never looks at the
  hostname/authType argument.
- **Security tradeoff to flag if copying this pattern:** once a cert is accepted for any host,
  that exact certificate is silently trusted for *any* host that later presents it, and the bypass
  skips expiry re-validation too — an expired-but-previously-accepted cert is trusted forever with
  no re-check. A host-bound design (store `(hostname, fingerprint)` pairs, not just fingerprint)
  would be safer than what's here.

**Nothing found:** no `CertificatePinner`/fixed-CA pinning, and no documented "we refuse to
support self-signed certs" rationale in code comments — the repo actively supports this via the
two layers above rather than punting to pattern A.

---

## owncloud-android

_Not yet investigated._

## jellyfin-android

**Pattern used:** (B) — app-wide `network_security_config.xml` trusting user-installed CAs. No
custom `TrustManager`, no TOFU flow, no per-server certificate pinning.

**Evidence:**

- File: `app/src/main/res/xml/network_security_config.xml`, wired via
  `android:networkSecurityConfig="@xml/network_security_config"` in
  `app/src/main/AndroidManifest.xml:43`.
- Scope: a single global `<base-config>` — all domains (no `<domain-config>`), all build types (no
  `<debug-overrides>` anywhere in the tree).
- Trust anchors: `src="system"` and `src="user"` together, plus `cleartextTrafficPermitted="true"`,
  all at the same global base-config level.
- No custom trust manager anywhere — grepped `TrustManager`, `X509TrustManager`, `SSLContext`,
  `CertificatePinner`, `HostnameVerifier`: zero hits outside the network security config. The
  `OkHttpClient` for API calls (`app/src/main/java/org/jellyfin/mobile/app/AppModule.kt:65`) is
  instantiated with no customization, purely inheriting the OS-level trust decision.
- **WebView layer is stricter and inconsistent with the native path.** The in-app WebView (loads
  the Jellyfin web client UI) hard-fails on SSL errors: `JellyfinWebViewClient.onReceivedSslError()`
  (`app/src/main/java/org/jellyfin/mobile/webapp/JellyfinWebViewClient.kt:109-112`) always calls
  `handler.cancel()`, only logging via `Timber.e`. No trust dialog, no fingerprint/issuer display,
  no accept/reject — SSL errors here fail regardless of what the network security config allows for
  native traffic.
- No documentation of the tradeoff in README/CONTRIBUTING/any `.md` at time of search.

**Tradeoffs worth flagging before copying this pattern:**

- Native (OkHttp) and WebView traffic run under **two different, inconsistent trust policies** —
  API/streaming calls trust user-installed CAs, the embedded web UI does not.
- `src="user"` trust is global to the app, not scoped to the user's own server domain — any CA
  installed on the device (including ones added for unrelated apps or a MITM proxy) is trusted for
  every domain this app talks to, no per-host isolation.
- No visibility into what certificate was actually presented, no revocation path other than
  removing the CA from Android system settings.
- Simplest of the patterns seen so far to implement, but weaker isolation and no user-facing
  transparency compared to Nextcloud's TOFU/pinning approach.

## syncthing-android

**Not directly applicable — different problem shape entirely.** This app doesn't connect to an
arbitrary remote server the user specifies; it bundles the Syncthing daemon as a **local
subprocess** and talks to it over `https://127.0.0.1:<port>` only. The "server" is spawned by the
app itself, so client and server are both controlled by the same install. Repo is archived; the
Google Play removal that led here was an unrelated storage-permission (`MANAGE_EXTERNAL_STORAGE`)
policy dispute, not anything to do with cert trust.

**What it does instead: fixed single-cert pinning to a self-generated cert.**

- **Files:** `SyncthingTrustManager.java` (custom `X509TrustManager`); `ApiRequest.java`
  (`getSslSocketFactory()` — wires it into the `SSLContext`/`SSLSocketFactory` for all REST calls
  via Volley); `WebGuiActivity.java` (`loadCaCert()` — separately trusts the same cert for the
  embedded WebView); `Constants.java` (cert/key file locations); `SyncthingService.java` (the
  bundled `syncthing` binary generates `https-cert.pem`/`https-key.pem` on first run).
- **Trust decision** (`SyncthingTrustManager.checkServerTrusted`): on every handshake, re-reads
  `https-cert.pem` from `context.getFilesDir()` and does `cert.verify(ca.getPublicKey())` against
  the presented chain. No chain-of-trust/CA validation, no expiry check, no revocation check — just
  "was this signed by that one specific public key." Throws if the file is missing.
  `checkClientTrusted` is a no-op (irrelevant, no client certs used).
- **Hostname verification is explicitly disabled** (`setHostnameVerifier((h, s) -> true)`) — only
  defensible because the target is hardcoded to loopback.
- **No TOFU UI at all.** The app never fetches a cert over the network and asks the user to accept
  it — it silently trusts whatever its own child process wrote to its own private storage. No
  "first connect" moment from the user's perspective.
- Private key stored as a plain file in app-private storage, not Android Keystore.
- **No `network_security_config.xml` anywhere in the repo.**

**Why this isn't a useful reference for pattern C:** no per-host cert store, no fingerprint
display, no accept/reject UX, no handling of multiple remote servers, no pin rotation/expiry/
revocation — none of what TaigaMobileNova would actually need. The one transferable piece is the
mechanical plumbing (`ApiRequest.getSslSocketFactory` — custom `TrustManager` wired into `OkHttp`/
`SSLContext`), as a starting point for building a real per-host pinning store on top. **Do not
copy the disabled hostname verification** — safe only because this app's target is always
loopback; carrying it over to a remote-server scenario would be a real vulnerability.

## home-assistant-android

**Pattern used:** (B) — app-wide `network_security_config.xml` trusting user-installed CAs, plus a
custom fallback trust manager to work around specific ROMs that don't honor `src="user"` through
the standard path. No TOFU/pinning (pattern C) anywhere — `CertificatePinner` is never referenced.
User-facing behavior on failure is closer to pattern A's (static error string, no cert detail
shown), even though the underlying trust config is pattern B.

**Evidence:**

- `app/src/main/res/xml/network_security_config.xml` (+ same for `wear/`; automotive reuses
  `:app`'s):
  ```xml
  <base-config cleartextTrafficPermitted="true">
      <trust-anchors>
          <certificates src="system"/>
          <certificates src="user"/>
      </trust-anchors>
  </base-config>
  ```
  Global, applies to release builds (no `<debug-overrides>`), not domain-scoped.
  `cleartextTrafficPermitted="true"` is also global.
- `common/src/main/kotlin/.../data/TLSHelper.kt`: configures OkHttp's `SSLContext`. Uses the
  platform default `X509TrustManager` (honors the config above) as primary, wrapped in
  `CompositeX509ExtendedTrustManager` with a **fallback** trust manager built only from
  `AndroidCAStore` `"user:"`-prefixed aliases — added because some ROMs (e.g. /e/OS, per referenced
  issues #6810/#5565) don't apply user-installed CAs through the standard trust-manager path even
  though their WebView/browser does.
- `CompositeX509ExtendedTrustManager.kt`: tries the primary trust manager first; only on
  `CertificateException` does it retry the user-CA-only fallback. **Not "always intercept"** — it
  defers to the system path first. Client-cert checks (mTLS) always go through the primary only.
- Also present, unrelated to server trust: mTLS client-certificate support via an
  `X509ExtendedKeyManager` sourced from `KeyChainRepository`/keystore.

**Where trust decisions are persisted:** nowhere in the app. No local file/DB/Keystore entry for
"user accepted cert X for host Y" — trust is entirely delegated to Android's OS-level CA stores
(system + user, the latter managed by the user outside the app via
`Settings > Security > Encryption & credentials`). Global to the device, not per-server.

**Tradeoffs worth flagging before copying this pattern:**

- No user visibility into *which* certificate was trusted — no fingerprint/issuer/chain shown
  anywhere, no in-app confirmation step. Materially weaker than TOFU: a rogue CA installed on the
  device by any means is trusted by this app too, with zero extra friction.
- Global, not per-server — trusting a user CA at the OS level trusts it for the whole device and
  every server this app talks to, not just "my self-hosted instance."
- `cleartextTrafficPermitted="true"` app-wide is a separate relaxation bundled into the same config
  file, worth noting if copying it wholesale.
- The `CompositeX509ExtendedTrustManager` fallback is a documented *deliberate widening* of trust
  to work around broken ROMs specifically — not a considered design for the self-signed-cert UX
  problem, and doesn't help a user who hasn't installed the CA at all.
- No documented rationale anywhere against per-server pinning — the cited issues are about
  user-CA-store reliability on specific ROMs, not a considered rejection of TOFU.

## Other candidates from the initial survey

_Not yet investigated: immich-app/immich (mobile/), advplyr/audiobookshelf-app._

---

## Open questions / things to decide later

- If we copy the TOFU approach, should trust be scoped per-`(host, fingerprint)` rather than
  per-fingerprint alone, to avoid Nextcloud's cross-host trust leak?
- Should an accepted cert's expiry still be re-checked on every connection, even after the initial
  TOFU acceptance?
- Where would the pinned-cert store live in this app's architecture — `AuthStorage`? A new
  storage class alongside it?

# Server-Side Setup for Manual QA

This doc is a self-contained brief for another agent (or the same agent in a fresh session) to set
up the test server needed for **Phase 5 manual QA** of the private-CA/self-signed certificate trust
feature (see `plan.md` in this folder — all of Phases 1–4 are implemented and unit-tested; only the
manual device/emulator repro is outstanding).

## Context

TaigaMobileNova (this repo) now supports trust-on-first-use (TOFU) certificate pinning: when the
app connects to a Taiga server whose TLS certificate isn't trusted by Android's default CA store
(self-signed, or issued by a private CA), it should show a dialog with the certificate's
issuer/subject/validity/SHA-256 fingerprint and let the user accept it. On accept, the app pins that
exact `(host, fingerprint)` pair and retries the login. See `research.md` for the background
(originally reported in [#322](https://github.com/Grigoriym/TaigaMobileNova/issues/322)) and
`plan.md` for the implementation details.

**What this doc is for:** setting up a Taiga instance reachable over HTTPS with a certificate the
device does **not** already trust, so the new flow can actually be exercised end-to-end.

## Goal

Get to a state where:

1. A Taiga instance is reachable at `https://<some-host>:<some-port>/`.
2. The certificate presented there is signed by a private CA that has **not** been installed
   anywhere on the test device/emulator.
3. The app's login screen can be pointed at that URL to trigger the untrusted-cert path.

You do **not** need a fully-configured, feature-complete Taiga instance — the TLS handshake fails
(or the pinning check runs) before any Taiga API call actually needs to succeed, so a minimal or
even partially-broken backend behind the proxy is fine for the connection-failure/dialog part of
the test. A working backend only matters for confirming that *retry-after-accept* actually logs in.

## Steps

### 1. Generate a private CA and a leaf certificate

```bash
# Private CA
openssl req -x509 -newkey rsa:4096 -sha256 -days 3650 -nodes \
  -keyout ca-key.pem -out ca-cert.pem -subj "/CN=Home Lab Test CA"

# Server key + CSR — CN/SAN must match whatever host you'll actually connect to.
# Use your machine's LAN IP for a physical device, or 10.0.2.2 for the standard Android
# Studio emulator (its alias for the host machine's loopback).
openssl req -newkey rsa:2048 -nodes -keyout server-key.pem -out server.csr \
  -subj "/CN=<TARGET_IP>"

echo "subjectAltName=IP:<TARGET_IP>" > san.cnf

openssl x509 -req -in server.csr -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out server-cert.pem -days 825 -sha256 -extfile san.cnf
```

Replace `<TARGET_IP>` with the actual address you'll type into the app's login screen.

### 2. Find the existing local Taiga instance

The user has a Taiga instance already running locally via `docker compose` (referenced earlier as
"on localhost through docker compose on this machine"). Before doing anything else:

- Run `docker compose ps` (or `docker ps`) to find the running Taiga containers and figure out
  which service is the HTTP entry point (commonly a gateway/nginx/taiga-front-facing service on
  port 80 or similar) and which Docker network it's on.
- **Do not modify that compose file's own service definitions.** The goal is to add a *new*,
  separate TLS-terminating reverse proxy sitting in front of it, so the existing plain-HTTP local
  setup keeps working unmodified for normal development use.

### 3. Add a throwaway TLS-terminating reverse proxy

Drop a standalone nginx (or Caddy) container on the same Docker network as the existing Taiga
services, terminating TLS with the certificate from step 1 and proxying to whatever the existing
gateway/frontend service is:

```nginx
server {
    listen 8443 ssl;
    ssl_certificate     /certs/server-cert.pem;
    ssl_certificate_key /certs/server-key.pem;
    location / {
        proxy_pass http://<existing-gateway-service-name>:80;
    }
}
```

```bash
docker run -d --network <taiga-compose-network> -p 8443:8443 \
  -v $(pwd)/certs:/certs:ro \
  -v $(pwd)/nginx.conf:/etc/nginx/conf.d/default.conf:ro \
  nginx
```

Adjust the proxied service name/port to whatever step 2 actually found — don't assume a name.

### 4. Verify the proxy works before involving the app at all

```bash
curl -vk https://<TARGET_IP>:8443/
```

Confirm you get a TLS handshake and *some* HTTP response (even an error page from Taiga is fine —
it proves the proxy chain works). `-k` is required here since curl doesn't trust the private CA
either, which is expected.

### 5. Do NOT install the CA on the test device

This is the whole point of the test — leaving `ca-cert.pem` uninstalled on the device/emulator is
what reproduces the "untrusted certificate" path. Do not add it to the system or user trust store.

## What to check once the server is up

Hand back to whoever is doing the actual app-side manual QA (or continue yourself if you're also
driving the Android build/emulator):

1. Enter `https://<TARGET_IP>:8443/` as the server URL on the login screen and attempt to log in.
2. **Expect:** the new "Untrusted certificate" dialog appears, showing issuer/subject/validity
   dates/SHA-256 fingerprint matching what `openssl x509 -in server-cert.pem -noout -fingerprint
   -sha256` reports for the leaf cert.
3. Tap **dismiss/no** → confirm the login just fails normally (no crash, no infinite spinner), and
   nothing was persisted (a second login attempt shows the same dialog again).
4. Retry, this time tap **confirm/yes** → confirm the login request retries automatically and either
   succeeds (if the backend behind the proxy is a real working Taiga instance with valid
   credentials) or fails with a normal Taiga auth error (wrong credentials) — but critically, it
   should **not** show the untrusted-cert dialog again on that retry, since the cert is now pinned.
5. Restart the app (fresh process) and log in again with the same server URL → the dialog should
   **not** reappear, confirming the pin persisted across process restarts (it's backed by
   `DataStore`, not in-memory state).
6. Optional stronger check: change `server-cert.pem`'s SAN or regenerate a different leaf cert for
   the same host, restart the proxy, and confirm the dialog reappears — proving the pin is scoped to
   the specific certificate, not just the host.

## Explicitly out of scope for this setup task

- Don't touch the real docker-compose Taiga configuration's own ports/services.
- Don't install the CA anywhere on the test device.
- No need to make the backend behind the proxy fully functional — a real login success (step 4) is
  a nice-to-have confirmation, not a blocker for validating the dialog/pinning behavior itself.

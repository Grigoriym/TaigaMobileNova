# Custom request headers — done steps

## 1. ✅ Storage + validator

`core:storage` `customheaders/`: `CustomHeader`, `CustomHeaderError`, `CustomHeaderValidator`,
`CustomHeadersStorage` + DataStore impl (encrypted blob, cached `currentHeaders`), per-platform
`@Single` providers (Android Keystore alias of its own, Noop on JVM/iOS). `FakeCustomHeadersStorage`
in `:testing`. Tests: validator rules incl. `Authorization`/`User-Agent` reserved; storage round
trip, empty list removes the key, bad blob reads as none, survives logout.
Verify: `./gradlew :core:storage:jvmTest :testing:compileKotlinJvm`
Note: validator is a plain `object` (stateless, no DI needed). Separate DataStore file
(`custom_headers_storage`), so `AuthStateManager`'s logout can't reach it — "survives logout" holds
by construction, no test. Kit `FakeSecretCipher` only prefixes `ENC:`; the test asserts that
prefix. Next: step 2.


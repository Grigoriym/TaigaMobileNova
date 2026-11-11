# Custom request headers — checklist

**Progress:** 4 / 8 · **Current step:** 5

Plan and rationale: [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Finished steps move to
`CHECKLIST-DONE.md`. All steps commit to `feat/android17-local-network-permission` (PR 468).

## 5. Custom headers screen + Settings entry

`feature:settings:ui` `customheaders/`: NavDestination, State, ViewModel (rows, per-row error,
Save enabled only when valid and changed, disabled offline), Screen; Settings row; nav graph entry.
ViewModel tests incl. failure path.
Verify: `./gradlew :feature:settings:ui:jvmTest ktlintCheck`

## 6. Login entry

"Custom headers (N)" button on `LoginScreen` → `onCustomHeadersClick` → same destination, wired in
`composeApp`. Count from `CustomHeadersStorage.headers`. Tests for the count.
Verify: `./gradlew :feature:login:ui:jvmTest`

## 7. Device check against a header-auth proxy

Stand up the fixture from the plan (nginx in front of `localhost:9000` demanding a custom header).
On the emulator and desktop: login fails with the proxy message and does **not** log out an
existing session; add the header from Login → login works; avatars load; header removed in
Settings → next refresh shows the proxy message. Record the fixture command in the plan.
Verify: the above, screenshots.

## 8. Close-out

Changelog stubs if a release is open, `docs/security/masvs.md` note (new stored secret), CLAUDE.md
pointer if a convention emerged, PR 468 title/body updated to cover both features, `finalize`.
Verify: full `./gradlew jvmTest koverXmlReport :koverVerify ktlintCheck detekt`

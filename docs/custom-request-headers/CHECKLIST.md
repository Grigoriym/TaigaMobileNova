# Custom request headers — checklist

**Progress:** 7 / 8 · **Current step:** 8

Plan and rationale: [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Finished steps move to
`CHECKLIST-DONE.md`. All steps commit to `feat/android17-local-network-permission` (PR 468).

## 8. Close-out

Changelog stubs if a release is open, `docs/security/masvs.md` note (new stored secret), CLAUDE.md
pointer if a convention emerged, PR 468 title/body updated to cover both features, `finalize`.
Verify: full `./gradlew jvmTest koverXmlReport :koverVerify ktlintCheck detekt`

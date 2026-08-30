# Performance Hardening — Done Steps

Archive of ticked steps from [CHECKLIST.md](CHECKLIST.md), kept for precedent when a later step
cites one by number. Not a place to look for open work.

## Step 1: Investigate CI regression checks for APK size and Perfetto/Macrobenchmark trace metrics — ✅ done 2026-08-29

Investigation only, per the step's own "not a commitment to add either check" scope — no workflow
YAML written, no production change. Findings written into IMPLEMENTATION_PLAN.md's "1. CI
regression checks..." section:

- **(a) APK size delta:** downloaded `diffuse` 0.3.0 and ran it directly against this project's own
  built APKs (`app-fdroid-debug.apk` vs `app-gplay-debug.apk`) — it parsed AGP 9.3.1's V2-signed
  output cleanly and produced the expected dex/arsc/manifest size tables, confirming the tool still
  works here even though its own upstream repo has been quiet since Feb 2024. `usefulness/diffuse-action`
  is an actively-maintained wrapper Action (pushed 2026-08-25) worth using instead of hand-rolling
  the CLI invocation; it only emits the diff as an output, so it'd be paired with
  `peter-evans/create-or-update-comment` to actually post the PR comment. Fork-PR-secrets gap for
  the release-accurate variant is still open and unsolved — the debug-vs-base-branch delta sidesteps
  it and is the recommended first cut.
- **(b) Macrobenchmark trace metrics:** read `core/storage`'s Android auth persistence code
  end-to-end. Found that `AndroidKeystoreTokenCipher.decrypt()` has a legacy plaintext-fallback path
  (any stored value without the `"v1:"` prefix passes through undecrypted) — meaning a
  pre-authenticated session can be seeded onto a fresh CI emulator by writing *unprefixed* plaintext
  token values into `auth_storage.preferences_pb`, without ever needing to reproduce the
  per-install AndroidKeyStore key. Not prototyped end-to-end (the file is a protobuf, so it needs
  the real DataStore APIs to write correctly, and the `benchmark` module's release build type's
  `run-as`/`adb root` accessibility is still unverified). Also confirmed
  `reactivecircus/android-emulator-runner` supports KVM-backed acceleration on GitHub-hosted
  `ubuntu-latest` runners in principle, not yet tried against this repo. Recommendation stands as
  written: scheduled/nightly, not per-PR, given emulator cost.

**Verify:** N/A — investigation step, no code changed; findings are the deliverable.

**Next:** queue is empty. Whether to turn either sub-finding into an actual `build.yml`/scheduled
workflow is a new decision for gregory to make (a real CI change, and (b) still needs an end-to-end
seeding prototype before it's committable) — not something this step's "investigate" scope commits
to starting.

## Step 2: Add APK size delta check to PRs — ✅ done 2026-08-30

Implemented finding (a) from step 1: a new `apk-size-check` job in `.github/workflows/build.yml`,
parallel to the existing `build` job. Design decision made with gregory first (baseline-APK source
has a real CI-cost tradeoff): rebuild `dev`'s merge-base commit in the same job rather than fetching
a `dev`-push-triggered artifact — self-contained, no new push-triggered workflow step, no
cross-workflow artifact dependency, at the cost of one extra full `assembleFdroidDebug` per PR.

Job does: checkout PR head → `assembleFdroidDebug` → save `head.apk` → `git fetch --depth=1` +
`checkout` the PR's base sha (`github.event.pull_request.base.sha`) → `assembleFdroidDebug` again →
save `base.apk` → `usefulness/diffuse-action@v1` (confirmed compatible with this project's AGP
9.3.1/V2-signed output in step 1) diffs the two → `peter-evans/find-comment@v4` +
`peter-evans/create-or-update-comment@v5` post/update a single tagged PR comment (`<!-- apk-size-diff
-->` marker) with `diffuse-action`'s `diff-gh-comment` output, so repeated pushes update one comment
instead of spamming new ones each time.

Matches this project's existing debug-build signing setup (`TAIGA_ALIAS_D`/`TAIGA_KEY_PASS_D`/
`TAIGA_STORE_PASS_D`/`ENCODED_STRING_D` env vars, `taigamobilenova_debug.jks` restore) — no
gplay/`google-services.json` needed since the check only builds the fdroid flavor. Third-party
action versions pinned to major-version tags (`@v1`/`@v4`/`@v5`), matching this workflow file's
existing style (`actions/checkout@v7`, etc.) rather than pinning to commit SHAs.

**Verify:** `.github/workflows/build.yml` validated with `actionlint` (via
`docker run --rm -v "$(pwd)":/repo -w /repo rhysd/actionlint:latest .github/workflows/build.yml`) —
zero new findings; the only shellcheck notices are pre-existing (`SC2086`, unquoted var in an
`echo | base64 -d` step already present in the untouched `build` job, and reused here for
consistency). Not yet verified by an actual PR run — the job's real behavior (Gradle build succeeds
twice, `diffuse-action` output shape matches what's assumed, the PR comment renders as expected)
will only be confirmed once this branch's PR runs the workflow for real.

**Next:** step 3 (macrobenchmark CI) is unstarted — see CHECKLIST.md. It is not gated in the
"needs gregory's decision" sense, but it's substantially larger (real emulator work, an unverified
`run-as` assumption, an undecided trend-detection design) than step 2 was, so it's being left as its
own step rather than folded into this one.

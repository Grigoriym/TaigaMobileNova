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

# 2026-08-08 — Screenshot testing: is it worth doing now?

**Status:** Investigating — options laid out, no decision made yet
**Link:** [testing improvement plan, "Considered and deferred"](../testing/improvement-plan.md#considered-and-deferred)
— original verdict (2026-08-02): "High maintenance for a solo-maintained app. Revisit only if
visual regressions become a recurring, concrete problem — not preemptively."

## The question

Now that the Compose UI test sweep (tasks 10–21) is fully closed, is the original deferral still
right, or does the infrastructure those tasks built change the cost side of the tradeoff enough to
revisit it?

## What's different now that wasn't true 2026-08-02

The project already runs `runComposeUiTest` on the **JVM/Desktop target**, not Robolectric or a
real Android device — every Screen and widget test from tasks 10–21 renders through host-machine
Skia via Compose Desktop. That matters for screenshot testing specifically:

- `captureToImage()` (part of `androidx.compose.ui.test`, already a `jvmTest.dependencies` of every
  module task 10+ touched via `compose.dependencies.uiTest`) can capture any composable this
  project's tests already render — **no new dependency needed** to get pixels out of a test.
- The Android-native tools (Roborazzi, Paparazzi) exist because Android screenshot testing needs
  something that renders a `View`/Composable off-device — Robolectric (shadow Android framework) or
  Layoutlib. This project doesn't have that problem: Desktop already renders for real.

So the "can we capture a screenshot" question is already answered yes, for free. The real cost is
what it always was for screenshot testing: **golden-image management and determinism**, not capture
mechanics.

## Options surveyed

### A. Do nothing — reaffirm the 2026-08-02 deferral

No visual regressions have been reported since then. Nothing forces a decision.

### B. Roborazzi with `roborazzi-compose-desktop`

Roborazzi ships a desktop-target artifact (`io.github.takahirom.roborazzi:roborazzi-compose-desktop`,
alpha versions as of this search — `1.6.0-alpha-2`+) with `recordRoborazziDebug`/
`verifyRoborazziDebug` Gradle tasks, golden-file management, and diff-image generation built in.

- **Pros:** record/verify workflow, diffing, and output-path config all exist already — not
  hand-rolled.
- **Cons:** the desktop artifact is alpha-versioned (not the mature Robolectric-backed artifact this
  tool is best known for). Pulling in a new test dependency for a project whose stated convention is
  hand-written fakes over frameworks (CLAUDE.md, Testing) is a real philosophy mismatch, not just a
  version-maturity concern.

### C. Hand-rolled: `captureToImage()` + a golden PNG comparison helper

Since capture is already free, write a small helper (`assertScreenshotMatches(image, goldenPath)`
in `:testing`, maybe ~100 lines): encode `ImageBitmap` to PNG via Skia, compare byte-for-byte or with
a pixel-tolerance diff against a checked-in golden, `-Precord` system property to write goldens
instead of asserting.

- **Pros:** no new dependency, fits the project's own conventions exactly (compare to how `:testing`
  already hand-rolls fakes instead of using MockK).
- **Cons:** all the golden-management/diff-visualization tooling Roborazzi gives for free has to be
  built and maintained by hand.

## The real blocker, and it applies to B and C equally

Font rendering is not deterministic across machines for either option — Compose Desktop renders
text through the **host OS's actual font stack**, unlike Robolectric (which bundles fixed fonts
specifically to make screenshot tests reproducible). This is a widely-reported problem for Compose
Desktop / non-Robolectric screenshot testing: identical-looking images fail a byte-exact diff at
thousands of pixels because of anti-aliasing/hinting differences between the machine that recorded
the golden and the machine that verifies it.

Two ways to live with this, neither free:

1. **Goldens are recorded and verified only on CI** (same reasoning CLAUDE.md's Kover section
   already uses: "CI is always the deterministic case"). Workable, but it means a contributor can't
   record or verify a golden locally and get a trustworthy answer — updating a golden requires a
   CI round-trip (push a branch, let CI record, pull the artifact back down) rather than running a
   Gradle task on their own machine. That's real ongoing friction for a solo maintainer, not a
   one-time setup cost.
2. **Pixel-tolerance diffing** instead of exact match — softens the friction but trades it for
   tuning a threshold that's tight enough to catch real regressions and loose enough not to flag
   font-hinting noise, which is its own maintenance burden.

Neither option — Roborazzi's desktop artifact or a hand-rolled comparator — avoids this; it's a
property of rendering text with the host's real font stack rather than a bundled deterministic one.

## Recommendation

**A — reaffirm the deferral, for a narrower reason than the original note gave.** The original
"high maintenance for a solo-maintained app" verdict undersold *why*: it wasn't about capture
mechanics (task 10–21's infrastructure has since made that free) but about the font-determinism
problem, which is unchanged by anything built since 2026-08-02 and unavoidable in either candidate
approach. Capturing pixels stopped being the hard part; keeping a golden-image suite green across
machines without either a CI-only workflow or tolerance tuning is still exactly as much ongoing
maintenance as it was before, for a problem (visual regressions) that still hasn't happened in
practice.

**Worth revisiting, not dropped outright** (unlike the iOS-native-target and Kanban-screenshot items
in the same deferred table, which were closed for good) — if a real visual regression ships and
this comes up again, option C is the one to prototype first: it costs nothing to try if the
CI-only-record/verify workflow turns out to be acceptable, and it matches the project's own
no-new-test-frameworks convention better than pulling in an alpha desktop artifact.

## Next step

None scoped — this stays a deferred item in the testing plan's "Considered and deferred" table,
now with the fuller reasoning above instead of the one-line original note. Revisit only when a
concrete visual regression makes the cost worth paying.

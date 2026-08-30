# Frictions

Tooling friction hit during work, newest last. One line each. Promoted or fixed entries get
deleted — see `finalize`.

- 2026-08-14: every Renovate PR failed guardrails because the `gradle/libs.versions.toml` wire
  fired on any touch to the file and a bot commit can never carry `Gate-change:`; fixed (ported
  from wallosmobile) by narrowing the wire to `detekt`/`ktlint`/`composeRules`/`agp`/`kover` keys
  only — verified against real commits `9d925e6b` (ksp bump, now passes) and `6f12f291` (detekt
  bump, still correctly fails).
- 2026-08-15: `NavigationSuiteScaffold`'s own default `layoutType` needs `currentWindowAdaptiveInfo()`
  from `org.jetbrains.compose.material3.adaptive:adaptive`, a separate artifact from
  `material3-adaptive-navigation-suite` with its own version — nothing in Maven Central metadata or
  the artifact's `.module` file said so; only found by `jar xf`-ing both artifacts and `javap`-ing
  the referenced-but-missing `WindowAdaptiveInfo` type across them.
- 2026-08-15: `currentWindowAdaptiveInfo()` and `WindowWidthSizeClass` compile clean (only a warning)
  even though both are deprecated in favor of `currentWindowAdaptiveInfoV2()` /
  `isWidthAtLeastBreakpoint()` — first pass used the deprecated pair and only caught it by reading
  compiler warning output, not from any red/failing signal.
- 2026-08-15: `Skill({skill: "android-skills:navigation-3"})` returned "Unknown skill" from inside
  a forked subagent, even though CLAUDE.md documents it as available — worked around by reading
  `~/.claude/plugins/marketplaces/android-skills/navigation/navigation-3/SKILL.md` and its
  `references/` files directly instead. Not re-tried from a non-fork session, so unclear if it's a
  fork-specific plugin-loading gap or broader. **Reproduced again 2026-08-15 (step 9) from a plain,
  non-fork session** — same "Unknown skill" error, same workaround (read the `SKILL.md` file
  directly). Not fork-specific; the skill name from CLAUDE.md's table just doesn't resolve via the
  `Skill` tool at all, in any session type. Second occurrence — one more and this needs an actual
  fix, not another line here.
- 2026-08-15: assumed `NavBackStackEntry.toRoute<T>(typeMap = ...)` existed, copying
  `SavedStateHandle.toRoute<T>(typeMap = ...)`'s call shape into 6 NavGraph call sites (step 8) —
  compiler rejected all 6 ("No parameter with name 'typeMap' found"). `NavBackStackEntry.toRoute()`
  takes no typeMap arg at all; it reads the typeMap the enclosing `composable<T>(typeMap = ...)`
  already registered on the destination. Confirmed by reading
  `navigation-common-desktop-2.9.2-sources.jar` directly rather than guessing from the sibling
  `SavedStateHandle` overload's signature.
- 2026-08-15: a hand-rolled `CompositionLocal` shaped like androidx's own `LocalResultEventBus`
  (an `object` wrapping a private `compositionLocalOf`) failed `ktlintCheck` twice over
  (`compose:compositionlocal-naming`, `compose:compositionlocal-allowlist`) before switching to a
  plain top-level `val = staticCompositionLocalOf<T> { error(...) }` — this repo's own
  `LocalScreenReadySignal`/`LocalOfflineState` shape — which only needed one `.editorconfig`
  allowlist addition (`compose_allowed_composition_locals`) to pass. Mirroring an upstream API's own
  internal shape is not the same as matching this repo's convention for the same concept; check
  `.editorconfig` for a `compose_*` allowlist before assuming a rule needs disabling.
- 2026-08-22: `xdotool type "http://127.0.0.1:9000"` (GUI-testing the desktop build's login screen)
  typed the colon as a caret (`http^//...`) even after `windowactivate` + plain `type` (no
  `--window`), the fix the existing `local-taiga-instance` memory already documents for the
  keystrokes-not-delivered issue. Retyping with `xdotool type --clearmodifiers "..."` produced the
  correct string. Root cause not investigated (probably a stuck-shift or dead-key state); the
  `--clearmodifiers` flag is the workaround.
- 2026-08-22: `xdotool key --window <id> ctrl+r` (GUI-testing the step-15 desktop refresh shortcut)
  silently dropped every keystroke, with no error — the exact same `--window`-drops-keyboard-input
  quirk `local-taiga-instance` already documents for `xdotool type --window`, but for `xdotool key`
  too. Cost several minutes of chasing a phantom app bug (assumed the shortcut was broken) before
  testing a plain key press and finding it worked. Fix: drop `--window` for `xdotool key` sends the
  same as for `type` — click/`windowactivate` the target first, then send the key bare.
- 2026-08-29: `xdotool mousemove --window <id> X Y click 1` against the composeApp desktop window
  registered only intermittently (roughly 1 in 5-6 attempts) — same exact coordinates, same target
  button, sometimes toggled the UI and sometimes silently did nothing, with no error and the mouse
  cursor visibly landing on target each time (`getmouselocation` confirmed). Not a coordinate/offset
  problem (retried at several nearby offsets with the same flakiness) and not specific to one widget
  (back-arrow nav and two other buttons all showed it). Gave up after ~10 attempts and verified step
  4's fix via code read + `jvmTest`/`ktlintCheck` instead of a live click-through. Root cause not
  found; worth a fresh look if this blocks a future GUI-verification step.
- 2026-08-29: guessed `diffuse` 0.3.0 download URL (`diffuse-0.3.0-binary.jar`) 404'd; the release
  asset is actually a `diffuse-0.3.0.zip` — checked via `gh`/GitHub releases API
  (`browser_download_url`) instead of guessing the filename pattern from the version tag.
- 2026-08-30: `pip install pyyaml` reported "already satisfied" but `python3 -c "import yaml"` still
  failed with `ModuleNotFoundError` — this machine's `python3` on PATH resolves to a linuxbrew
  install (3.14) that doesn't see the apt-installed pyyaml under `/usr/lib/python3/dist-packages`.
  Fixed by calling `/usr/bin/python3` explicitly for the one-off YAML-parse check.

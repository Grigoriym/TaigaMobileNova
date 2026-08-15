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

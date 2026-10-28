# 450 — Add monochrome icon

**Status:** In progress
**Link:** https://github.com/Grigoriym/TaigaMobileNova/issues/450   **Updated:** 2026-10-28

## Report

Feature request (label `enhancement`), not a defect: "Proposing to add monochrome layer to launcher
icon in order for it to be themable on modern Android versions", referencing
https://developer.android.com/develop/ui/compose/system/icon_design_adaptive.

Environment: Android 13+ (API 33) with "Themed icons" enabled in the launcher. The report names no
device or launcher; none is needed — the mechanism is platform-wide.

## Findings

- Both adaptive icon definitions declare only `<background>` and `<foreground>`, no `<monochrome>`:
  `androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml:3-4` and `ic_launcher_round.xml:3-4`.
  So on Android 13+ with themed icons on, the launcher falls back to the full-color icon.
- No flavor (`gplay`, `fdroid`, `debug`, …) overrides any launcher resource — the `main` set is the
  only one to change.
- The adaptive foreground points at the raster `@mipmap/ic_launcher_foreground` (`.webp`, 5
  densities), but a vector of the same logo already exists at
  `androidApp/src/main/res/drawable/ic_launcher_foreground.xml` (8 flat-filled polygons, 108dp
  viewport; byte-identical copy in `uikit/.../composeResources/drawable/`). Nothing in `androidApp`
  references the drawable version.
- Rendering those 8 polygons in a single color (done with PIL in-session, mirroring the vector's
  group transform) gives a clean silhouette: a fir tree split by a thin vertical seam, plus the
  trunk. Two halves and the stacked trunk rects merge without artifacts, so the existing paths can be
  reused as-is. Bounding box ≈ x 32.9–74.3dp, y 25.5–81.9dp of the 108dp canvas — inside the 72dp
  safe circle, same scale as the color foreground.
- `<monochrome>` is read only on API 33+; older platforms ignore it, so it can live in the existing
  `mipmap-anydpi-v26` files (this is what Android Studio's own Image Asset template generates).
- The system tints the monochrome drawable and uses only its alpha, so the fill color is irrelevant.

## Root cause

N/A — missing feature. The adaptive icon simply has no monochrome layer.

## Impact

Cosmetic. Users with themed icons on see this app as the one full-color icon among tinted ones. No
functional effect, no workaround on the user side.

## Open questions

- Should the 48dp "logo zone" guideline for themed icons be met? The tree is ~41×56dp, so it
  slightly exceeds a 48dp circle vertically — same as the current color foreground. Matching the
  color foreground's size keeps the themed and normal icons visually consistent; shrinking it is a
  design call, not a correctness one. Not blocking.

## Options

1. **Reuse the vector paths as a one-color drawable (recommended).** Add
   `drawable/ic_launcher_monochrome.xml` = the existing vector with every `fillColor` set to one
   color, and add `<monochrome android:drawable="@drawable/ic_launcher_monochrome"/>` to both
   adaptive icon XMLs.
   - Pros: ~3 files, no new artwork, pixel-aligned with the existing foreground, vector (no
     per-density rasters).
   - Cons: a third copy of the logo geometry to keep in sync if the logo ever changes.
   - Risk/blast radius: launcher icon only; splash screen (`themes.xml:20`) uses the foreground and
     is untouched.
2. **Point `<monochrome>` at the existing `drawable/ic_launcher_foreground.xml` directly.**
   - Pros: zero new files beyond the XML edits; the system ignores color anyway.
   - Cons: relies on the 8 colors all being fully opaque (they are) — any future semi-transparent
     detail would show up as a lighter shade, and the intent is non-obvious to a reader. Marginal
     saving over option 1.
3. **Commission a dedicated monochrome artwork** (e.g. tree outline, or bigger fill gaps).
   - Pros: can be tuned for the 48dp guideline.
   - Cons: needs design work nobody has scoped; the silhouette from option 1 already reads well.
4. **Won't fix.** Cons: request is cheap and standard for apps on Android 13+.

**Recommendation:** option 1 — explicit, cheap, and the existing geometry already produces a good
silhouette.

## Decision

Option 1, approved by gregory 2026-10-28.

## What landed

- `androidApp/src/main/res/drawable/ic_launcher_monochrome.xml` — the existing foreground vector with
  every `fillColor` set to `#FFFFFFFF` (only alpha matters; the system applies the tint).
- `<monochrome android:drawable="@drawable/ic_launcher_monochrome"/>` added to both
  `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`.
- Verified: `:androidApp:assembleFdroidDebug` builds. Not yet checked on a device with themed icons
  enabled.

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

- Both adaptive icon definitions declared only `<background>` and `<foreground>`, no `<monochrome>`
  (`androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`, `ic_launcher_round.xml`). So on
  Android 13+ with themed icons on, the launcher showed the full-colour icon.
- No flavor (`gplay`, `fdroid`, `debug`, …) overrides any launcher resource — the `main` set is the
  only one to change.
- The adaptive foreground is the raster `@mipmap/ic_launcher_foreground` (`.webp`, 5 densities): a
  forest illustration — tall green fir, purple firs behind, a pale moon disc, a grassy hill. There
  is no vector source for it in the repo.
- `drawable/ic_launcher_foreground.xml` (and a copy in `uikit`) is an unrelated, unused vector of the
  original project's logo — see `docs/revisit.md` entry 56.
- The page's requirements (verified against the linked doc): `<monochrome>` is the only theming
  layer; read on API 33+, ignored below, so it lives in the existing `mipmap-anydpi-v26` files.
  The logo must be ≥ 48×48dp and within the central 66×66dp. The system uses only the monochrome
  layer's alpha and applies its own tint. Android 16 QPR2+ auto-themes apps that provide none.
- The illustration's full alpha mask is unusable as a silhouette: the opaque moon disc fills in
  behind the trees into one dome. Dropping the moon's pale, low-saturation pixels
  (`r > 160 && g > 160 && b > 130`) leaves a clean silhouette of the trees and hill; a plain
  "drop light pixels" threshold would also punch holes where the light-green grass highlights are.

## Root cause

N/A — missing feature. The adaptive icon had no monochrome layer.

## Impact

Cosmetic. Users with themed icons on see this app as the one full-colour icon among tinted ones. No
functional effect.

## Options

1. **Bitmap silhouette:** derive the mask from the `.webp`, export `ic_launcher_monochrome.webp` at
   all 5 densities. Exact match to the artwork; small details soften when downscaled.
2. **Traced vector (chosen):** trace the cleaned mask with `potrace` into one VectorDrawable.
   Crisp at any size, one file; tracing can round off fine needle tips.
3. **Hand-drawn monochrome artwork** with gaps separating the trees. Best result, needs a designer.
4. **Won't fix.**

## Decision

Option 2, chosen by gregory 2026-10-28.

## What landed

- `androidApp/src/main/res/drawable/ic_launcher_monochrome.xml` — a single traced path (~11.5 KB),
  fill `#FFFFFFFF` (only alpha matters).
- `<monochrome android:drawable="@drawable/ic_launcher_monochrome"/>` in both adaptive icon XMLs.
- How it was generated (no script committed — rerun if the artwork changes): upscale
  `mipmap-xxxhdpi/ic_launcher_foreground.webp` (432px) 4× with Lanczos; mark as foreground every
  pixel with alpha > 128 that isn't moon-coloured (rule above); write a PBM;
  `potrace mask.pbm -s -t 400` (`-t 400` drops speckles and pinholes up to 400 px² at 4×); wrap the
  path in a VectorDrawable `group` with `scaleX=0.00625`, `scaleY=-0.00625`, `translateY=108`
  (potrace's `scale(0.1,-0.1)` × 108dp/1728px, flipped back to y-down).
- Verified: rendering the VectorDrawable's own path data overlaid on the colour foreground lines up
  exactly (everything but the moon covered), inside the 66dp safe circle; the silhouette's bounding
  box is 47×52dp (x 30.9–78.0, y 27.2–79.2), the same footprint as the colour artwork. `:androidApp:assembleFdroidDebug` builds. On-device check with themed icons: pending
  (gregory).

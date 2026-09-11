# 2026-09-10 — Mention popup dismisses/flickers the keyboard

**Status:** Approved
**Link:** none (reported directly by gregory, not a GitHub issue)
**Updated:** 2026-09-10

## Report

gregory, while comparing `MentionSuggestionsPopup` (the `DropdownMenu`-based mention
autocomplete shipped in
[docs/architecture/mention-tagging-support/](../architecture/mention-tagging-support/)
steps 3-5) against a horizontally-scrollable-row alternative used by another app: "i
presume it should not have a quirk we have that when i type @ the keyboard is removed
and dropdown flicker for a second."

- **Symptom, as reported:** typing `@` in a mention-enabled field (`CreateCommentBar`
  or the description editor) dismisses the soft keyboard, and the popup itself
  flickers for about a second.
- **Environment:** not stated — device, AVD vs. real hardware, and exact repro steps
  (which field, which screen) are all unspecified. gregory's phrasing ("i presume")
  suggests this may be recalled from general use rather than a fresh, deliberate
  repro just now.
- **Reporter's diagnosis:** implicit, not stated as a mechanism — the message frames
  it as a property of the *popup* implementation specifically ("for comparison, we can
  leave the dropdown option and have two"), i.e. gregory expects the row-based
  alternative would not have this quirk. Treated as a hypothesis below, not a premise.

## Findings

**The popup is built on a `focusable = true` `Popup`, and that setting is
independently documented, by the framework's own source, to receive IME events.**

- `MentionSuggestionsPopup` (`uikit/.../widgets/editor/MentionSuggestionsPopup.kt:47`)
  calls Material3's `DropdownMenu(...)` with no explicit `properties` override, so it
  gets the default.
- Confirmed by decompiling the exact resolved sources (project pins
  `org.jetbrains.compose.material3:material3:1.10.0-alpha05`, which redirects its
  Android target to the real `androidx.compose.material3:material3-android`; resolved
  version confirmed via `./gradlew :uikit:dependencies --configuration
  androidCompileClasspath` → `1.5.0-alpha08`, sources pulled from
  `~/.gradle/caches/modules-2/files-2.1/androidx.compose.material3/material3-android/1.5.0-alpha08/.../material3-android-1.5.0-alpha08-sources.jar`):
  `AndroidMenu.android.kt:197` — `internal actual val DefaultMenuProperties =
  PopupProperties(focusable = true)`. `DropdownMenu`'s own `Popup(...)` call
  (`AndroidMenu.android.kt:70-74`) passes this `properties` straight through with no
  IME-specific handling of its own.
- `androidx.compose.ui:ui-android` (resolved version `1.12.0`, same
  `:uikit:dependencies` check) — `AndroidPopup.android.kt`'s own doc comments state
  the mechanism explicitly, not as inference:
  - Line 245-250 (`Popup`'s `focusable` param doc): *"Whether the popup is focusable.
    When true, the popup will receive IME events and key presses ... popup must be
    \[focusable\] in order to receive key events such as the back button."*
  - Line 525-531 (`createFlags`): when `focusable` is true, the window's
    `WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE` bit is left **unset** — i.e. the
    popup is a real, independently-focusable Android window, not just a drawn overlay
    inside the host Activity's window.

A separate, real Android window taking input focus is exactly what causes the system
to detach the IME from whatever view previously held it (the `BasicTextField`) — this
is standard Android window-focus/IME behavior, not something Compose can silently work
around while `focusable = true` is set. The two are in direct tension:
`CHECKLIST-DONE.md`'s Step 4 note already records that `focusable = true` (the
default) is *load-bearing* — the delegate tried a plain non-`DropdownMenu` derivation
in Step 4 and found the popup needs it for back-press/outside-tap dismiss to work at
all (`onDismissRequest` firing). Turning `focusable` off to stop the keyboard dismiss
would trade one regression for another.

**The "flicker" is separately explained by the same framework source, not a distinct
bug.** `AndroidPopup.android.kt:848-851`'s own doc comment on
`pollForLocationOnScreenChange()`: *"The location can change without any callbacks
being fired if, for example, the soft keyboard is shown or hidden when the window is
in `adjustPan` mode. In that case, the window's root view (`ViewRootImpl`) will
'scroll' the view hierarchy in a special way that doesn't fire any callbacks"* — the
popup has a dedicated per-frame polling fallback (`pollForLocationOnScreenChange` →
`updateParentBounds` → `updatePosition`) specifically because a keyboard show/hide can
silently move its anchor. This app's `MainActivity` uses `adjustResize`, not
`adjustPan` (`androidApp/src/main/AndroidManifest.xml:27`), so the *normal* insets/
relayout callback path should fire too — but either way, the anchored text field's
on-screen position moves the instant the IME starts hiding (both `CreateCommentBar`
and the description editor's `Box` are `imePadding()`-affected), and the popup
repositions to follow it. That reposition landing on the same frame(s) the popup is
also playing its own enter transition (`MutableTransitionState` in
`AndroidMenu.android.kt:57-58`) is a plausible, source-grounded explanation for a
visible jump/flicker right as the popup opens.

## Root cause

Material3 `DropdownMenu`'s default `PopupProperties(focusable = true)` creates a
genuinely separate, focusable Android window for the popup. Android transfers input
focus to that window the moment it appears, which detaches the IME from the
`BasicTextField` that had it — this is standard OS behavior, not a Compose or
app-level defect. The reported flicker is very likely the same event's second-order
effect: the keyboard hiding shrinks the `imePadding()`-driven layout, moving the
anchored field, which the popup's position-tracking (either the normal relayout path
under `adjustResize`, or its own documented IME-pan polling fallback) then follows —
landing a visible reposition on top of the popup's own opening transition.

## Impact

Every mention-popup open (typing `@` in `CreateCommentBar` or the description editor)
hides the keyboard the user was actively typing in, which is a real UX regression on
a text-entry-heavy interaction — the user has to re-tap the field or the keyboard has
to auto-restore before they can keep typing past the inserted mention. Affects both
of the currently-shipped call sites (Steps 4 and 5). No workaround exists that keeps
both the current popup and normal keyboard behavior — `focusable = true` is required
for the popup's own back-press/outside-tap dismiss (Step 4's note), so this isn't
adjustable via a small property change without giving up that dismiss behavior.

## Open questions

- **Not personally reproduced with a real soft-keyboard tap sequence.** My own GUI
  verification for Step 5 used `adb shell input text` (synthetic text injection) and
  screenshotted only after the full string landed, not frame-by-frame at the moment
  `@` was typed — so I did not directly observe a flicker, and can't rule out a
  distinct, additional cause beyond the mechanism above. The keyboard-dismiss
  mechanism itself is confirmed from framework source regardless of what my own
  screenshots did or didn't catch.
- gregory didn't specify which field(s) or device the flicker was seen on — worth a
  real device/AVD tap-by-tap repro (not synthetic `input text`) if a fix is pursued,
  to confirm the flicker specifically (not just the keyboard dismiss, which is
  already confirmed by source).

## Options

1. **Keep the `DropdownMenu` popup, accept the keyboard-dismiss as an inherent
   `focusable=true` cost.** No code change. Cheapest, but ships a real UX regression
   on every mention lookup.
2. **Set `properties = PopupProperties(focusable = false)` on the existing
   `DropdownMenu`.** Removes the keyboard-dismiss/flicker (popup no longer takes
   window focus) at the cost of losing the popup's own back-press/outside-tap
   dismiss — Step 4's note already found this exact tradeoff when it tried a
   non-`DropdownMenu` approach and had to add `isMentionPopupDismissed` specifically
   to compensate for losing automatic dismiss. Back press while the popup is open
   would then fall through to whatever the screen's own back handler does (navigate
   back / show the discard dialog on the description editor) instead of just closing
   the popup — likely a worse regression than the one being fixed.
3. **Replace the popup with the inline horizontally-scrollable row gregory is
   proposing.** Not a `Popup` at all — it renders in the same window as the text
   field, in normal layout flow, so it cannot steal window focus and cannot trigger
   this specific keyboard-dismiss mechanism by construction. This is the option
   gregory's message already leans toward wanting compared side-by-side. Real cost:
   a rework of `MentionSuggestionsPopup` (`DropdownMenu` → `LazyRow`) and both call
   sites' wiring, plus their existing tests (`MentionSuggestionsPopupTest`,
   `CreateCommentBarTest`, `EditDescriptionContentTest`) — separate scope from this
   investigation, tracked as the design decision from the prior message rather than
   folded into this doc.

**Recommendation:** option 3. It's the one gregory was already leaning toward for
independent reasons (avoiding an unbounded-looking dropdown, fixed positioning under
the input), and this investigation confirms it also structurally avoids the
keyboard-dismiss mechanism — not just as an assumption but because a `LazyRow` in
normal layout flow never creates a second focusable Android window. Option 2 looks
cheaper but trades one confirmed regression (keyboard dismiss) for another confirmed
one (broken back-dismiss), so it isn't actually a smaller change once Step 4's own
finding is accounted for. Option 1 is only right if gregory decides the row rework
isn't worth doing at all.

## Decision

gregory, 2026-09-10: option 3 — replace the `DropdownMenu`-based popup with the
inline horizontally-scrollable row outright, not kept side-by-side with the current
popup for later comparison. Decomposed into
[CHECKLIST.md](../architecture/mention-tagging-support/CHECKLIST.md) steps 6-7 (new
row component, then rewiring both call sites), pushing the prior Step 6 (full-suite
verification) to Step 8. See
[IMPLEMENTATION_PLAN.md](../architecture/mention-tagging-support/IMPLEMENTATION_PLAN.md)'s
"Input-side mechanism" section for the recorded decision detail and what the
replacement changes.

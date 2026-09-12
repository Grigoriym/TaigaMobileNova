# @-Mention Tagging Support — Checklist

**Progress:** 8/8 done. Initiative complete.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for architecture, the server
contract this relies on, and the reasoning behind each mechanism choice — including
its "Team-member data source" section (added 2026-09-10) and the row-replacement
decision recorded at the end of "Input-side mechanism" (added 2026-09-10). Origin:
[docs/issues/414-mention-autocomplete-not-implemented.md](../../issues/414-mention-autocomplete-not-implemented.md),
approved by gregory 2026-09-09 (full support, not just the render-only minimal fix).
All 8 steps are done — see [CHECKLIST-DONE.md](CHECKLIST-DONE.md). Steps 6-7 replaced
the `DropdownMenu`-based popup built in steps 3-4 outright, per
[docs/issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md](../../issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md)'s
investigation and decision — not a second implementation kept alongside the first.

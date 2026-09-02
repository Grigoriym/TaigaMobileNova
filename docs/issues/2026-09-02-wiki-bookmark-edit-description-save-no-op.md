# Wiki: editing description of a page opened from Bookmarks silently does nothing

**Status:** Done
**Link:** none (reported directly by gregory during 2.2.0 release testing)
**Updated:** 2026-09-02

## Report

Repro steps as given:

1. Go to Bookmarks (Wiki section).
2. Press + to create a new one.
3. Write the title and save to create it.
4. A details screen opens.
5. Click "edit description".
6. Write something.
7. Click save.
8. Nothing happens — the only option is to go back.

The newly created page shows up in "All Pages" (not Bookmarks), and opening it from
there, editing the description works fine.

Environment: not stated (desktop app was being used for 2.2.0 release testing this
session, but nothing in the mechanism below is platform-specific — see Root cause).
Not stated whether this is new in 2.2.0 or pre-existing — that's what this doc
resolves.

## Findings

**A Taiga "wiki bookmark" (`wiki-links`) and a "wiki page" (`wiki`) are different
server entities with independent ids.** `WikiRepository.createWikiLink()` posts to
`wiki-links` and returns a `WikiLink(ref, id, order, title)`
(`feature/wiki/data/.../WikiRepositoryImpl.kt:52-59`) — `id` here is the wiki-link
row's own id, not any wiki page's id.

**The bookmark-creation flow navigates to the page details screen using that
wiki-link id, not the page id:**
`WikiCreateBookmarkScreen.kt:60-62` — `goToWikiPage(result.ref, result.id)` — `result`
is the `WikiLink` from creation, so `id` passed onward is `WikiLink.id`.

**The existing-bookmarks list has the identical problem, not just newly-created
ones:** `WikiBookmarksViewModel.fetchBookmarks()`
(`feature/wiki/ui/.../bookmark/list/WikiBookmarksViewModel.kt:130-135`) builds
`WikiUIItem(id = link.id, ...)` from `wikiRepository.getWikiLinks()` — every row in
the Bookmarks list carries the wiki-link id, not the page id. Contrast
`WikiPagesViewModel` (the "All Pages" list), which builds `WikiUIItem(id = page.id,
...)` from actual `WikiPage`s (`feature/wiki/ui/.../page/list/WikiPagesViewModel.kt:131-132`)
— correct there.

**`WikiPageNavDestination.id` (whatever was passed in) is what `WikiPageViewModel`
uses to key the description-edit session, for the entire lifetime of the screen:**
```kotlin
// WikiPageViewModel.kt:53
private val wikiId: Long = route.id
...
// :75-78, in init — runs before the real page has loaded
workItemEditStateRepository
    .getDescriptionFlow(wikiId, TaskIdentifier.Wiki)
    .onEach(::onNewDescriptionUpdate)
    .launchIn(viewModelScope)
```

**But the edit-description screen writes back using the real, loaded page id, not
the nav-route id:** `WikiPageScreen.kt:163-165` calls
`goToEditDescription(state.currentPage.content, state.currentPage.id)` —
`state.currentPage` is populated from `wikiPageUseCase.getWikiPageData(pageSlug)`
(`WikiPageViewModel.kt:177-191`), i.e. the real `WikiPage` fetched by slug, not from
the nav route. That real page id flows through
`WorkItemEditDescriptionNavDestination.workItemId`
(`.../editdescription/WorkItemEditDescriptionNavDestination.kt`) into
`EditDescriptionViewModel.onGoingBack()`'s
`workItemEditStateRepository.updateDescription(workItemId = route.workItemId, ...)`
(`EditDescriptionViewModel.kt:38-42`).

**`WorkItemEditStateRepository` sessions are keyed by that id, string-formatted per
type** (`getSessionKey`, `WorkItemEditStateRepository.kt:24-27`: `"WIKI_$workItemId"`
for Wiki). The listener (`WikiPageViewModel`, keyed on `route.id` = wiki-link id when
reached via Bookmarks) and the writer (`EditDescriptionViewModel`, keyed on
`currentPage.id` = real page id) land in **different sessions** whenever
`route.id != currentPage.id`.

**The write doesn't just get dropped — it hangs, and that hang is what produces the
exact reported symptom.** `descriptionChannel` is a plain rendezvous `Channel<String>()`
(`WorkItemEditSession.kt:13`, no buffer), so `send()` suspends until something
`receive()`s. `EditDescriptionViewModel.onGoingBack()` (`EditDescriptionViewModel.kt:38-47`)
runs, in order: `setIsDialogVisible(false)` → (if changed)
`workItemEditStateRepository.updateDescription(...)` → `_onBackAction.send(Unit)`.
When the session key is stale, `updateDescription`'s `descriptionChannel.send(...)`
(`WorkItemEditStateRepository.kt:60-64`) has no listener and suspends forever inside
that `viewModelScope.launch` — so the line after it, `_onBackAction.send(Unit)`, is
never reached. `_onBackAction` is what `ObserveAsEvents` on the edit screen uses to
navigate back after a save (`WorkItemEditDescriptionScreen.kt` — not opened in this
investigation, but implied by the pattern in CLAUDE.md's "One-off Events" section).
Concretely: the app's own "leave the editor now that the save is queued" step never
fires, so the screen just sits there — exactly "click save, nothing happens... the
only thing we can do is go back" (the user falls back to the top bar's independent
`NavigationIconConfig.Back()`, which is a separate, unaffected code path). Confirmed
directly: writing a test that calls `updateDescription` with a stale key against this
ViewModel setup does not return — it timed out the JVM test run's 1-minute watchdog
(`UncompletedCoroutinesError`) before being removed in favor of the (safe) positive
case below.

Reached via "All Pages" instead, `route.id` **is** `WikiPage.id`
(`WikiPagesViewModel.kt:131-132`), so listener and writer key to the same session and
the save works — matching the report's second half.

**No existing test catches this** because `WikiPageViewModelTest` always constructs
the route id and the fake page id as the same value by construction:
```kotlin
// WikiPageViewModelTest.kt:36,39,83
private val wikiId = getRandomLong()
private val route = WikiPageNavDestination(slug = slug, id = wikiId)
...
private fun makeWikiPage(id: Long = wikiId, ...)
```
so the mismatch this bug depends on never occurs in the suite.

**Not a new regression.** `WikiBookmarksViewModel`, `WikiPageNavDestination`, and
`WorkItemEditStateRepository` all predate 2.1.5 (git log shows their current shape
back through the `#221` KMP-migration commit, well before the current release
cycle) — this has been broken since the wiki-links/bookmarks feature existed in this
form, not introduced by anything in 2.2.0.

## Root cause

`WikiPageViewModel` uses `route.id` (`WikiPageNavDestination.id`, whatever the caller
passed) as the durable identity for the page's description-edit session, but that id
is only reliably the real `WikiPage.id` when the caller happens to already know it
(the "All Pages" list). When the caller only knows a *wiki-link* id — creating a
bookmark, or opening an existing one from the Bookmarks list — `route.id` is the
wiki-link's id, a different server entity, while the edit-description round trip
writes back keyed on the page id actually loaded from the server
(`state.currentPage.id`). The two ids silently diverge and the update is delivered to
a session no one reads.

## Impact

Every page reached via Bookmarks (not just freshly created ones) has a broken
description-save: the edit screen's own "save and go back" action hangs (see
Findings), so the user is stuck on the editor and has to use the top bar's back
button instead, and the edit is lost. No error is ever shown, because nothing fails —
the coroutine that would apply the patch just never resumes. Workaround exists: open
the same page from "All Pages" instead, where it works. Attachments and other actions
on the same screen are unaffected (they key off `currentPage.id` directly, not
`wikiId`/`route.id`).

## Open questions

- None blocking — the mechanism is fully traced end-to-end from UI action to the
  discarded write.

## Options

1. **Re-key the description session off the loaded page's real id, not the nav-route
   id.** Move the `getDescriptionFlow(...)` subscription (currently set up in `init`
   using `route.id`) to fire after `loadData()` succeeds, using `data.page.id`; do the
   same for `onCleared()`'s `clearSession(...)`. This makes `WikiPageViewModel` self-
   consistent regardless of what id the caller navigated in with — Bookmarks, All
   Pages, or anywhere else a wiki page link might be opened from in the future.
   - Pros: fixes the actual root cause (id source-of-truth mismatch) in the one place
     that owns both ends of the session key; no change needed to the Bookmarks list,
     bookmark-creation flow, or nav destinations; small, contained diff.
   - Cons: the description-flow subscription can no longer be wired unconditionally
     in `init` — it must wait on the first successful `loadData()`, which is a small
     structural change to the ViewModel's init order. Any description write staged
     before the page finishes loading would still be missed, but that's already
     impossible today (the edit-description entry point only exists once
     `currentPage` is loaded).
   - Blast radius: `WikiPageViewModel.kt` only.

2. **Make the Bookmarks-side ids carry the real page id instead of the wiki-link
   id.** `WikiLinkDTO`/`WikiLink` doesn't carry a page id at all (Taiga's wiki-link
   object only has its own id + `href`/slug + order + title), so this would mean an
   extra `getProjectWikiPageBySlug` lookup per bookmark row (list screen) and after
   creation (create-bookmark flow) purely to discover the real id before navigating.
   - Pros: `WikiPageNavDestination.id` would always be a real page id everywhere,
     closer to what the type probably should mean.
   - Cons: extra network round-trip(s) just to populate a list or finish a create
     flow; doesn't fix the actual defect (`WikiPageViewModel` trusting `route.id` as
     a stable key) so any future caller that navigates with a non-page id
     reintroduces the same bug; larger blast radius (bookmark list loading, bookmark
     creation, both nav call sites).
   - Blast radius: `WikiBookmarksViewModel`, `WikiCreateBookmarkViewModel`/`Screen`,
     `WikiRepository`.

3. **Do nothing / document the workaround.** Leaves a real, reproducible data-loss
   bug (silent discard of a user's edited description) in a flow the app explicitly
   offers (Bookmarks + edit description). Not recommended — the fix is small and
   contained (Option 1).

**Recommendation: Option 1.** It fixes the actual defect (relying on caller-supplied
id as a durable key when the ViewModel itself later loads the authoritative id),
touches one file, needs no extra network calls, and structurally prevents the same
bug for any other future entry point into `WikiPageScreen`. Regression test: extend
`WikiPageViewModelTest` with a case where `route.id != makeWikiPage().id` (the gap
the current suite doesn't cover) and assert the description update still lands.

## Decision

Approved: Option 1, fix in the current release branch (`release/v2.2.0`, PR #379).

## What landed

- `WikiPageViewModel.kt`: removed the `route.id`-derived `wikiId` field. The
  description-flow subscription now starts inside `loadData()`'s success branch,
  keyed on `data.page.id` (the real, just-loaded page id) instead of the nav-route
  id. `onCleared()` now reads the same real id off `_state.value.currentPage?.id`
  (no-op if the page never finished loading, since no session would exist for it).
- `WikiPageViewModelTest.kt`: added `description update keyed by page id succeeds
  even when route id differs` — constructs the page with a different id than
  `route.id` (the exact Bookmarks-navigation shape) and asserts the description
  update still lands. Existing tests were unaffected since they already default the
  fake page's id to the route's id.
- Deliberately did not add a test asserting the *old*, mismatched-key path hangs —
  confirmed by hand that it does (see Findings), but a test that calls
  `updateDescription` with no matching listener never returns, since
  `descriptionChannel` is an unbuffered rendezvous channel; encoding that as an
  automated test would just be a flaky/hanging test for a property of
  `WorkItemEditStateRepository` this fix doesn't change.

# 386 — Error Kanban Loading

**Status:** Done
**Link:** https://github.com/Grigoriym/TaigaMobileNova/issues/386   **Updated:** 2026-09-06

## Report

Reporter (@JoeHardi, F-Droid 2.2.0, Android 17): opening the Kanban tab fails with an
error screen reading "Waiting limit exceeded". Follow-up from the maintainer
established:

1. Reporter just started using Taiga, so there's no "worked on a previous version" data
   point.
2. No other screen (issues, epics, etc.) shows the same error — only Kanban.
3. Reporter believes their server is otherwise fine, since every other tab works.

What the report omits: which server (self-hosted vs. self-hosted-behind-slow-link vs.
taiga.io), and how many projects/user stories the account has. Both are relevant to the
root cause below and are open questions.

## Findings

- "Waiting limit exceeded" is this project's own string for a real client-side socket
  timeout, not a mistranslation: `strings/src/commonMain/composeResources/values/strings.xml:51`
  defines `timeout_exceeded = "Waiting limit exceeded"`, and it's reached only via
  `NetworkException(NetworkException.ERROR_TIMEOUT)`, thrown when
  `e is SocketTimeoutException` in
  `core/api/.../errors/NetworkErrorMapper.kt:31`. So the report is a genuine read
  timeout, not a bug in the error message itself.

- Kanban's data load (`GetKanbanDataUseCaseImpl.getData()`,
  `feature/kanban/domain/.../GetKanbanDataUseCase.kt:44-56`) fires four concurrent
  requests plus one more run inline in the same `coroutineScope` (so still concurrent):
  current project, statuses/filters, team members, swimlanes, and
  `userStoriesRepository.getUserStories()` — called with **no arguments at all**
  (`GetKanbanDataUseCase.kt:47`).

- `UserStoriesRepository.getUserStories()`'s `project` parameter defaults to `null`
  (`feature/userstories/domain/.../UserStoriesRepository.kt`), and
  `UserStoriesRepositoryImpl.getUserStories()`
  (`feature/userstories/data/.../UserStoriesRepositoryImpl.kt:59-80`) forwards it
  straight through to `UserStoriesApi.getUserStories()` with no substitution. That API
  method (`UserStoriesApi.kt:35-41`) builds `GET userstories` and additionally sets
  `x-disable-pagination: true` whenever `params.page == null` — which it always is here,
  since Kanban never sets it.

- Net effect: Kanban's user-stories request has **no `project` filter and pagination
  explicitly disabled**. Taiga's `/userstories` endpoint scopes to the caller's session
  project only when a `project` query param is supplied; omitting it returns every user
  story across every project the account can see. For an account with more than a
  handful of projects/stories this is an unbounded response — plausible to make a single
  socket read exceed the client's timeout, matching the observed error exactly.

- This is the **only** call site of this repository method
  (`grep -rn "getUserStories(" --include="*.kt"` across the repo, excluding the
  interface/impl declarations and tests, returns just this one caller plus the
  integration test, which does pass `project = 5`). So this is not shared code whose
  contract other screens rely on.

- Confirms the reporter's "no other screen has this problem": every sibling repository
  in the codebase resolves `taigaSessionStorage.getCurrentProjectId()` before calling
  its API — `SwimlanesRepositoryImpl.getSwimlanes()`, `FiltersRepositoryImpl`,
  `IssuesPagingSource`, `EpicsPagingSource`, `SprintsRepositoryImpl`,
  `TasksRepositoryImpl`, `WorkItemRepositoryImpl`, etc. all do this (confirmed via
  `grep -rn "taigaSessionStorage.getCurrentProjectId()"`, ~30 call sites). Kanban's
  `userStories.await()` call is the one place in the app that does not, and
  `UserStoriesRepositoryImpl` already has `taigaSessionStorage` injected (used a few
  lines below, in `createUserStory`) — the plumbing to fix it is already present, just
  unused for this call.

- Not a regression: `git blame` puts the `getUserStories()` call at
  `GetKanbanDataUseCase.kt:47` back to 2025-12-12 (commit `002d9e38c`), well before the
  KMP-migration commit this file currently lives under. The bug has existed since the
  Kanban use case was written; it likely surfaces only for accounts whose combined
  project/story count is large enough to make the unfiltered response slow.

## Root cause

`GetKanbanDataUseCaseImpl.getData()` (`feature/kanban/domain/src/commonMain/kotlin/com/grappim/taigamobile/feature/kanban/domain/GetKanbanDataUseCase.kt:47`)
calls `userStoriesRepository.getUserStories()` without a project id.
`UserStoriesRepositoryImpl.getUserStories()` (`feature/userstories/data/src/commonMain/kotlin/com/grappim/taigamobile/feature/userstories/data/UserStoriesRepositoryImpl.kt:59-80`)
does not default that to the current project the way every other repository in the app
does, so the resulting `GET userstories` request (with pagination explicitly disabled,
`UserStoriesApi.kt:40`) asks the server for every user story in every project the
account belongs to. On a server/account combination large enough for that response to
take a while to arrive, the client's socket read hits its timeout, which the app then
surfaces, correctly, as "Waiting limit exceeded."

## Impact

Any account whose combined story/project count is large enough to slow this single
unfiltered request past the client timeout cannot open Kanban at all — no partial
board, no retry path shown other than the generic error. Scope is presumably narrow
today (only one report so far) but grows with account size and is worse on a
self-hosted instance with a slower connection or DB. No workaround inside the app;
Kanban is simply unusable for affected accounts.

## Open questions

- Reporter's project/story count and whether their instance is self-hosted or Taiga's
  cloud — would confirm the "large unfiltered response" theory quantitatively, but the
  code-path evidence above doesn't depend on getting an answer.
- Whether `isDashboard`, `watcherId`, `epicId`, `assignedId` params on
  `UserStoriesRepository.getUserStories()` are dead (only Kanban calls this method, with
  none of them set) — worth a follow-up cleanup pass, out of scope for this fix.

## Options

1. **Pass the current project id from `GetKanbanDataUseCaseImpl`** — call
   `userStoriesRepository.getUserStories(project = taigaSessionStorage.getCurrentProjectId())`.
   Requires injecting `TaigaSessionStorage` into the Kanban use case (not currently a
   constructor param there).
   - Pro: smallest possible diff; matches how every other Kanban sub-request already
     gets its project scope (via `projectsRepository.getCurrentProjectSimple()` /
     `swimlanesRepository.getSwimlanes()` internally resolving it).
   - Con: adds a new dependency to a use case whose current dependencies are all
     feature repositories, not storage.

2. **Default the project id inside `UserStoriesRepositoryImpl.getUserStories()`** itself
   (mirroring `SwimlanesRepositoryImpl`/`FiltersRepositoryImpl`, which resolve
   `taigaSessionStorage.getCurrentProjectId()` internally rather than requiring the
   caller to pass it) — e.g. only when the caller passes `project == null`.
   - Pro: fixes the bug at the layer that already owns `taigaSessionStorage`, no new
     dependency in the Kanban use case, and closes the door on any future caller making
     the same mistake.
   - Con: changes behavior for a public repository method's default — currently
     confirmed only Kanban calls it, so this is low risk, but it does mean "no explicit
     project" silently stops meaning "all projects" for any future caller, which needs
     to be an intentional decision.

3. **Do nothing / leave as "won't fix"** — not viable; this makes an entire screen
   unusable for affected accounts with no workaround.

**Recommendation:** Option 2. It fixes the bug where the inconsistency actually lives
(every sibling repository already resolves the project internally; this one is the
outlier), needs no new dependency in the Kanban use case, and there's no evidence
anywhere in the codebase that "fetch across all projects" is an intentional, relied-upon
behavior of this method — it's simply unset because Kanban is the only caller and never
supplies it.

## Decision

**Option 2 — approved by gregory (2026-09-06).** Default the project id inside
`UserStoriesRepositoryImpl.getUserStories()` when the caller doesn't supply one, rather
than pushing `TaigaSessionStorage` into the Kanban use case.

## What landed

- `UserStoriesRepositoryImpl.getUserStories()`
  (`feature/userstories/data/src/commonMain/kotlin/com/grappim/taigamobile/feature/userstories/data/UserStoriesRepositoryImpl.kt:75`)
  now passes `project = project ?: taigaSessionStorage.getCurrentProjectId()` instead of
  forwarding the (usually null) `project` param straight through. Kanban's call site is
  unchanged — it still calls `getUserStories()` with no arguments — the fix lives at the
  layer that already resolves the current project for every other repository.
- Regression tests added to `UserStoriesRepositoryImplTest`
  (`feature/userstories/data/src/commonTest/.../UserStoriesRepositoryImplTest.kt`):
  one asserting the current project id is substituted when `project` is omitted (fails
  before the fix — confirmed, `project == null` reached the fake API), one asserting an
  explicitly-passed `project` is never overridden. `FakeUserStoriesApi` gained a
  `getUserStoriesCalls` list to make the passed `GetUserStoriesParams` assertable.
- Verified: `./gradlew :feature:userstories:data:jvmTest --tests
  "...UserStoriesRepositoryImplTest"` green (both new tests fail-then-pass across the
  fix); full `./gradlew jvmTest` and `./gradlew ktlintCheck` green with nothing else
  broken.
- Left out, deliberately: the `getEpicUserStoriesSimplified()` method on the same repo
  builds its own `GetUserStoriesParams(epic = epicId)` directly against the API and
  wasn't touched — out of scope for this issue, and not implicated by the report.

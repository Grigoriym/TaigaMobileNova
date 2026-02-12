# Offline Support Plan

Research and implementation plan for adding offline capabilities to TaigaMobileNova.

## Implementation Status

| Phase | Status | Notes |
|-------|--------|-------|
| **Phase 1: Infrastructure** | ✅ DONE | NetworkMonitor, DB v4 |
| **Phase 2: Core Entities** | ✅ DONE | SprintEntity, WorkItemEntity |
| **Phase 3: Repository Changes** | ✅ DONE | Cache-first in SprintsRepo, WorkItemRepo |
| **Phase 4: Paging with Cache** | ⚠️ PARTIAL | Sprint RemoteMediator done; WorkItem deferred |
| **Phase 5: UI Updates** | ✅ DONE | Offline banner implemented |
| **Phase 6: Cache Management** | ✅ DONE | CacheManager with 24h TTL |
| **Phase 7: Disable Write Actions** | ✅ DONE | CompositionLocal approach |

### What's Been Implemented

#### Phase 1: Infrastructure

- `core/storage/.../network/NetworkMonitor.kt` - Interface
- `core/storage/.../network/NetworkMonitorImpl.kt` - Uses ConnectivityManager, tracks multiple networks
- `testing/.../FakeNetworkMonitor.kt` - Test fake
- Database bumped to version 4 with auto-migration

#### Phase 2: Core Entities

- `core/storage/.../db/entities/SprintEntity.kt` - Sprint cache
- `core/storage/.../db/entities/WorkItemEntity.kt` - Generic work item (UserStory/Task/Issue/Epic)
- `core/storage/.../db/dao/SprintDao.kt` - Including `pagingSource()` for RemoteMediator
- `core/storage/.../db/dao/WorkItemDao.kt` - Including `pagingSource()` for RemoteMediator
- `core/storage/.../db/CacheTypeConverters.kt` - LocalDate, LocalDateTime, CommonTaskType

#### Phase 3: Repository Changes

- `feature/sprint/data/.../SprintsRepositoryImpl.kt` - Cache-first for getSprints(), getSprintUserStories(), etc.
- `feature/workitem/data/.../WorkItemRepositoryImpl.kt` - Cache-first for getWorkItems()
- `feature/sprint/data/.../SprintEntityMapper.kt` - Entity ↔ Domain conversion
- `feature/workitem/data/.../WorkItemEntityMapper.kt` - Entity ↔ Domain conversion

#### Phase 4: Paging with Cache

- `feature/sprint/data/.../SprintRemoteMediator.kt` - ✅ Implemented
- `SprintsRepositoryImpl.getSprintsPaging()` - Uses RemoteMediator + Room as source of truth
- WorkItem paging (Issues, UserStories, Epics) - ⏸️ **DEFERRED** due to complex server-side filters

#### Phase 5: UI Updates

- `uikit/.../widgets/banner/OfflineIndicatorBanner.kt` - Orange animated banner
- `strings/.../strings.xml` - Added `offline_banner_message`
- `app/.../MainViewModel.kt` - Exposes `isOffline: StateFlow<Boolean>`
- `app/.../MainScreen.kt` - Shows banner when offline

#### Phase 6: Cache Management

- `core/storage/.../cache/CacheManager.kt` - Interface
- `core/storage/.../cache/CacheManagerImpl.kt` - 24h TTL, project/all cleanup
- `core/storage/.../cleaner/DataCleanerImpl.kt` - Clears cache on logout
- `app/.../TaigaApp.kt` - Cleans expired cache on app start

#### Phase 7: Disable Write Actions When Offline (In Progress)

**Approach:** Use `CompositionLocal` to provide offline state to all screens.

**Key difference from permissions:**

- No permission → **hide** action (user can never do this)
- Offline → **disable** action (user can do this, just not right now)

**Infrastructure (✅ DONE):**

- `uikit/.../state/LocalOfflineState.kt` - CompositionLocal for offline state
- `app/.../MainScreen.kt` - Provides `LocalOfflineState` via `CompositionLocalProvider`
- `uikit/.../topbar/TopBarConfig.kt` - Added `enabled` parameter to action buttons
- `uikit/.../topbar/TaigaTopAppBar.kt` - Uses `enabled` on buttons
- `uikit/.../widgets/CreateCommentBar.kt` - Added `isOffline` parameter
- `uikit/.../widgets/editor/TextFieldWithHint.kt` - Added `enabled` parameter
- `feature/workitem/ui/.../WorkItemDropdownMenuWidget.kt` - Added `isOffline` parameter

**Pattern for screens:**
```kotlin
// List screen - disable add button
val isOffline = LocalOfflineState.current
LaunchedEffect(state.canAddX, isOffline) {
    topBarController.update(TopBarConfig(
        actions = buildList {
            if (state.canAddX) {
                add(TopBarActionIconButton(enabled = !isOffline, ...))
            }
        }
    ))
}

// Details screen - pass to widgets
WorkItemDropdownMenuWidget(..., isOffline = isOffline)
CreateCommentBar(..., isOffline = isOffline)
```

**Screens updated:**

- ✅ Epics: list (add button), details (dropdown, comment bar)
- ✅ User Stories: list, details
- ✅ Tasks: list, details
- ✅ Issues: list, details
- ✅ Sprints: list, details (Scrum screens)
- ✅ Wiki: pages list, page details, bookmarks
- ✅ Kanban: drag-and-drop actions
- ✅ Create screens
- ✅ Inline edit widgets

### Future Work (Optional)

**Kanban drag-and-drop:**

- Gesture-based, not button-based
- Would need to intercept drag events and prevent drop when offline
- Consider showing toast/snackbar if user tries to drag while offline

**WorkItem RemoteMediator (Complex)**
The WorkItem paging sources have complex server-side filters:

- Assignees, created by, watchers
- Statuses, priorities, severities, types
- Tags, epics, roles

To implement RemoteMediator for these would require:

1. Deciding filter caching strategy (cache per filter combo vs cache all + client filter)
2. Potentially significant changes to filter handling
3. Consider if the complexity is worth it vs current cache-first approach

#### Additional UI Polish

- "Last updated" timestamp display
- Stale data color coding

---

## Original Plan (Reference)

### Constraints

This is a **collaborative project management tool**. Multiple users edit the same data. Therefore:

- Full offline with sync/conflict resolution is **impractical**
- Write operations while offline require complex queuing and conflict resolution
- Stale data could cause confusion in a team environment

### Approach: Read-Only Cache

**"Last seen" data when offline** - cache what the user browses, show it when offline.

1. User opens a screen online → fetch from network → save to Room
2. User goes offline → show cached data with "offline" indicator
3. User comes online → refresh in background
4. Write operations disabled when offline

### Key Files Created/Modified

| File | Purpose |
|------|---------|
| `core/storage/.../network/NetworkMonitor.kt` | Connectivity monitoring |
| `core/storage/.../db/entities/SprintEntity.kt` | Sprint cache entity |
| `core/storage/.../db/entities/WorkItemEntity.kt` | WorkItem cache entity |
| `core/storage/.../cache/CacheManager.kt` | TTL and cleanup |
| `feature/sprint/data/.../SprintRemoteMediator.kt` | Paging + caching |
| `uikit/.../banner/OfflineIndicatorBanner.kt` | Offline UI indicator |
| `uikit/.../state/LocalOfflineState.kt` | CompositionLocal for offline state |
| `uikit/.../topbar/TopBarConfig.kt` | Added `enabled` to action buttons |
| `uikit/.../widgets/CreateCommentBar.kt` | Added `isOffline` parameter |
| `feature/workitem/ui/.../WorkItemDropdownMenuWidget.kt` | Added `isOffline` parameter |
| `app/.../MainViewModel.kt` | Exposes isOffline state |
| `app/.../MainScreen.kt` | Provides LocalOfflineState |

### Database Schema (v4)

```
sprint_table:
  - id (PK), projectId, name, order, start, end, storiesCount, isClosed, cachedAt
  - Indexes: projectId

work_item_table:
  - id + taskType (composite PK)
  - projectId, projectName, ref, title, createdDate
  - isClosed, isBlocked, blockedNote
  - statusId, statusName, statusColor
  - assigneeId, assigneeName, assigneePhoto
  - tagsJson, colorsJson, sprintId, cachedAt
  - Indexes: projectId, sprintId
```

## References

- [Android Paging with Network and Database](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [ConnectivityManager](https://developer.android.com/training/monitoring-device-state/connectivity-status-type)

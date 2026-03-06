# Settings-Project Implementation Plan

Based on `docs/SETTINGS_PROJECT_API.md` and the current state of `feature/settings/`.

## Current State

### Done
- `Settings → Attributes → Tags`: full CRUD + merge (admin-only gate via `project.isAdmin`)
- `Settings → User`: read-only profile info
- `Settings → Interface`: theme selector
- `Settings → About`: app info

### Architecture note
- Tags are in `ProjectsRepository` — project-settings endpoints belong there too (same `projects/{id}` base)
- Admin-only sections are gated in `SettingsViewModel` via `project.isAdmin`

---

## Planned Features

### Phase 1 — Project Details & Modules (Quick wins)

Both use the same `PATCH /projects/{id}` endpoint. No new domain models needed.

#### 1A. Edit Project Details
**Screen:** `Settings → Project Details`
**API:** `PATCH /projects/{id}`
**Fields to expose:**
- Name (text field, max 250 chars)
- Description (multiline text)
- Is private (toggle)
- Is looking for people (toggle) + note (text field, conditional)
- Is contact activated (toggle)

**Tasks:**
- [ ] Add `updateProject(fields: Map<String, Any?>)` to `ProjectsRepository`
- [ ] Add `ProjectDetailsNavDestination` + route
- [ ] `ProjectDetailsScreen` + `ProjectDetailsViewModel` + `ProjectDetailsState`
- [ ] Wire into `SettingsScreen` (admin-only)
- [ ] Wire into `SettingsNavGraph`

#### 1B. Module Toggles
**Screen:** `Settings → Modules`
**API:** `PATCH /projects/{id}`
**Fields:**
- Epics activated
- Backlog activated
- Kanban activated
- Issues activated
- Wiki activated
- Contact activated
- Total milestones (Int)
- Total story points (Double)
- Video conferencing (dropdown: none / Whereby / Jitsi / Talky / Custom) + extra data field

**Tasks:**
- [x] `ModulesNavDestination` + route
- [x] `ModulesScreen` + `ModulesViewModel` + `ModulesState`
- [x] Wire into `SettingsScreen` (admin-only)
- [x] Wire into `SettingsNavGraph`

---

### Phase 2 — Project Values (Statuses, Types, Priorities, etc.)

**Screen:** `Settings → Attributes → Project Values` (sub-menu listing all resource types)

**Resources:**
| Resource | Endpoint |
|---|---|
| Epic Statuses | `/epic-statuses` |
| US Statuses | `/userstory-statuses` |
| Task Statuses | `/task-statuses` |
| Issue Statuses | `/issue-statuses` |
| Issue Types | `/issue-types` |
| Priorities | `/priorities` |
| Severities | `/severities` |
| Points | `/points` |
| US Due Dates | `/userstory-due-dates` |
| Task Due Dates | `/task-due-dates` |
| Issue Due Dates | `/issue-due-dates` |

**Operations per resource:** List, Create, Edit (name, color, order, is_closed), Delete (with moveTo), Bulk reorder (drag-and-drop)

**Tasks:**
- [ ] New repository interface (or extend `ProjectsRepository`) for project values CRUD
- [ ] Generic `ProjectValueItem` domain model + per-resource subtypes
- [ ] `ProjectValuesNavDestination` (resource type param) + single reusable screen
- [ ] `ProjectValuesScreen` + `ProjectValuesViewModel` + state — parametrized by resource type
- [ ] Color picker for item color editing (reuse or add to uikit)
- [ ] Drag-and-drop reorder (uikit already has drag-and-drop support — check `uikit-guide`)
- [ ] Delete dialog with "move items to" selector
- [ ] Wire into `AttributesScreen` and `SettingsNavGraph`

---

### Phase 3 — Custom Attributes

**Screen:** `Settings → Attributes → Custom Attributes` (sub-menu: Epic / US / Task / Issue)

**Attribute types:** text, multiline, richtext, date, url, dropdown (+ options list), checkbox, number

**Operations:** List, Create, Edit (name, description, type, extra options for dropdown), Delete

**Key differences from Phase 2 (Project Values):**
- No color picker
- No `isClosed` / `isArchived` / `value` / `daysToDue` fields
- Has `description` (String) and `type` (String enum) fields
- `extra: List<String>?` — only populated / editable when `type == "dropdown"`
- Delete does **NOT** require `moveTo` — simple `DELETE /{endpoint}/{id}`
- `order` is `Long` (not `Int`)
- Validation: `CUSTOM_ATTR_NAME_MAX_LENGTH = 64`

#### API Endpoints (4 entity types)

| Entity | Endpoint |
|---|---|
| Epic | `/epic-custom-attributes` |
| User Story | `/userstory-custom-attributes` |
| Task | `/task-custom-attributes` |
| Issue | `/issue-custom-attributes` |

All support: `GET ?project=<id>`, `POST`, `PATCH /<id>`, `DELETE /<id>`.

No bulk-reorder endpoint needed for MVP.

#### Domain Layer (`feature/projects/domain`)

New file `CustomAttribute.kt`:
```kotlin
data class CustomAttribute(
    val id: Long,
    val name: String,
    val description: String,
    val type: String,  // see CustomAttributeType values below
    val order: Long,
    val project: Long,
    val extra: ImmutableList<String>  // non-null; empty when type != "dropdown"
)
```

New file `CustomAttributeEntityType.kt` — mirrors `ProjectValueType` pattern:
```kotlin
enum class CustomAttributeEntityType(val endpoint: String) {
    EPIC("epic-custom-attributes"),
    USER_STORY("userstory-custom-attributes"),
    TASK("task-custom-attributes"),
    ISSUE("issue-custom-attributes")
}
```

New file `CustomAttributesRepository.kt`:
```kotlin
interface CustomAttributesRepository {
    suspend fun getCustomAttributes(type: CustomAttributeEntityType): ImmutableList<CustomAttribute>
    suspend fun createCustomAttribute(type: CustomAttributeEntityType, name: String, description: String, attrType: String, extra: List<String>?): CustomAttribute
    suspend fun updateCustomAttribute(type: CustomAttributeEntityType, id: Long, name: String, description: String, attrType: String, extra: List<String>?): CustomAttribute
    suspend fun deleteCustomAttribute(type: CustomAttributeEntityType, id: Long)
}
```

#### DTO Layer (`feature/projects/dto`)

New file `CustomAttributeDTO.kt`:
```kotlin
@Serializable
data class CustomAttributeDTO(
    val id: Long,
    val name: String,
    val description: String = "",
    val type: String = "text",
    val order: Long = 1,
    val project: Long,
    val extra: List<String>? = null
    // Skip created_date / modified_date — not needed in UI
)
```

New file `CustomAttributeRequestDTO.kt`:
```kotlin
@Serializable
data class CustomAttributeRequestDTO(
    val project: Long? = null,  // only on create
    val name: String,
    val description: String? = null,
    val type: String? = null,
    val extra: List<String>? = null
)
```

#### Data Layer (`feature/projects/data`)

New `CustomAttributesApi` interface + `@Single(binds = [CustomAttributesApi::class])` impl — mirrors `ProjectValuesApiImpl`:
```kotlin
interface CustomAttributesApi {
    suspend fun getCustomAttributes(endpoint: String, projectId: Long): List<CustomAttributeDTO>
    suspend fun createCustomAttribute(endpoint: String, request: CustomAttributeRequestDTO): CustomAttributeDTO
    suspend fun updateCustomAttribute(endpoint: String, id: Long, request: CustomAttributeRequestDTO): CustomAttributeDTO
    suspend fun deleteCustomAttribute(endpoint: String, id: Long)  // no moveTo
}
```

`CustomAttributesRepositoryImpl` — `@Single(binds = [CustomAttributesRepository::class])`, uses `TaigaSessionStorage.getCurrentProjectId()` (same as `ProjectValuesRepositoryImpl`).

#### Navigation (two-level — same as Phase 2)

In `feature/settings/ui/.../attributes/customattributes/`:

- `CustomAttributesMenuNavDestination` — `@Serializable data object`
- `CustomAttributesNavDestination(entityTypeName: String)` — pass `type.name`, recover via `CustomAttributeEntityType.valueOf(route.entityTypeName)`
- Extension functions: `navigateToCustomAttributesMenu()`, `navigateToCustomAttributes(type)`
- `CustomAttributeEntityType.toTitleRes()` — maps each enum value to a string resource

Add "Custom Attributes" `ListItem` to `AttributesScreen` (third entry, after Project Values). Add `goToCustomAttributesMenuScreen: () -> Unit` parameter to `AttributesScreen`.

#### UI Files (`feature/settings/ui/.../attributes/customattributes/`)

**`CustomAttributesMenuScreen`** — identical pattern to `ProjectValuesMenuScreen`, iterates `CustomAttributeEntityType.entries`.

**`CustomAttributesState`**:
```kotlin
data class CustomAttributesState(
    val type: CustomAttributeEntityType,
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val isOperationLoading: Boolean = false,
    val items: ImmutableList<CustomAttribute> = persistentListOf(),
    val refresh: () -> Unit = {},
    // Edit dialog
    val isEditDialogVisible: Boolean = false,
    val editingItem: CustomAttribute? = null,
    val onAddClick: () -> Unit = {},
    val onEditClick: (CustomAttribute) -> Unit = {},
    val onSaveItem: (name: String, description: String, type: String, extra: List<String>) -> Unit = { _, _, _, _ -> },
    val onDismissEditDialog: () -> Unit = {},
    // Delete dialog
    val isDeleteDialogVisible: Boolean = false,
    val deletingItem: CustomAttribute? = null,
    val onDeleteClick: (CustomAttribute) -> Unit = {},
    val onConfirmDelete: () -> Unit = {},
    val onDismissDeleteDialog: () -> Unit = {}
)
```

**`CustomAttributesViewModel`** — same structure as `ProjectValuesViewModel`. On save, decides create vs update by checking `editingItem == null`.

**`CustomAttributesScreen`** — receives `showSnackbar: (NativeText) -> Unit`, same TopBar pattern with "Add" action icon.

**Edit dialog specifics:**
- Name: `OutlinedTextField` (required, max 64 chars)
- Description: `OutlinedTextField` (optional, multiline)
- Type: `ExposedDropdownMenuBox` with 8 options from `CustomAttributeType` values
- Extra options: `OutlinedTextField` (visible only when selected type == `"dropdown"`). Input: one option per line. Split on `\n` when calling `onSaveItem`. Display existing extras joined by `\n`.

**Delete dialog:** Simple `AlertDialog` confirm — no "move to" dropdown. Calls `onConfirmDelete()`.

#### SettingsNavGraph additions

```kotlin
composable<CustomAttributesMenuNavDestination> {
    CustomAttributesMenuScreen(
        goToCustomAttributes = { type -> navController.navigateToCustomAttributes(type) }
    )
}
composable<CustomAttributesNavDestination> {
    CustomAttributesScreen(showSnackbar = showSnackbar)
}
```

Also update `AttributesScreen` composable in the graph to pass `goToCustomAttributesMenuScreen`.

#### Strings to add (`strings.xml`)

```xml
<string name="settings_custom_attributes">Custom Attributes</string>
<string name="custom_attributes_epic">Epic</string>
<string name="custom_attributes_us">User Story</string>
<string name="custom_attributes_task">Task</string>
<string name="custom_attributes_issue">Issue</string>
<string name="custom_attributes_add_title">Add Custom Attribute</string>
<string name="custom_attributes_edit_title">Edit Custom Attribute</string>
<string name="custom_attributes_name_hint">Name</string>
<string name="custom_attributes_description_hint">Description</string>
<string name="custom_attributes_type_hint">Type</string>
<string name="custom_attributes_extra_hint">Options (one per line)</string>
<string name="custom_attributes_type_text">Text</string>
<string name="custom_attributes_type_multiline">Multiline</string>
<string name="custom_attributes_type_richtext">Rich Text</string>
<string name="custom_attributes_type_date">Date</string>
<string name="custom_attributes_type_url">URL</string>
<string name="custom_attributes_type_dropdown">Dropdown</string>
<string name="custom_attributes_type_checkbox">Checkbox</string>
<string name="custom_attributes_type_number">Number</string>
```

#### Tasks
- [ ] `CustomAttribute` domain model + `CustomAttributeEntityType` enum in `feature/projects/domain`
- [ ] `CustomAttributesRepository` interface in `feature/projects/domain`
- [ ] `CustomAttributeDTO` + `CustomAttributeRequestDTO` in `feature/projects/dto`
- [ ] `CustomAttributesApi` interface + impl in `feature/projects/data` (register `@Single`)
- [ ] `CustomAttributesRepositoryImpl` in `feature/projects/data` (register `@Single`)
- [ ] `CustomAttributesMenuNavDestination` + `CustomAttributesNavDestination` + extension functions + `toTitleRes()`
- [ ] `CustomAttributesMenuScreen`
- [ ] `CustomAttributesState` + `CustomAttributesViewModel` + `CustomAttributesScreen`
- [ ] Edit dialog: name, description, type dropdown, conditional extra field (newline-separated)
- [ ] Delete dialog: simple confirm, no moveTo
- [ ] Add "Custom Attributes" entry to `AttributesScreen` (third item)
- [ ] Wire 2 new routes in `SettingsNavGraph` + update `AttributesScreen` route to pass new callback

---

### Phase 4 — Swimlanes

**Screen:** `Settings → Swimlanes` (single screen, no sub-menu)

**Operations:** List, Create, Edit name, Delete (with moveTo), Bulk reorder, Set default swimlane, Edit WIP limit per swimlane-status

**Key differences from Phase 2/3:**
- Single flat screen — no type-selection menu needed
- Swimlane has nested `statuses` (list of `SwimlaneStatus`) rendered inline per swimlane
- Bulk reorder endpoint: `POST /swimlanes/bulk_update_order` with `bulk_swimlanes: [[id, order], ...]`
- Set default uses `PATCH /projects/{id}` with `{"default_swimlane": id}` — same endpoint as Phase 1/modules
- WIP limit: `PATCH /swimlane-userstory-statuses/{swimlaneUserstoryStatusId}` with `{"wip_limit": N}`
- Delete moveTo is nullable — **omit** (not send null) when deleting the last swimlane; items get swimlane=null
- `defaultSwimlaneId` must be loaded from project details (not returned by `GET /swimlanes`)
- Drag-and-drop reorder — check `uikit-guide` subagent for available DnD primitives

#### API Endpoints

| Operation | Method + Path |
|---|---|
| List | `GET /swimlanes?project={id}` |
| Create | `POST /swimlanes` |
| Edit name | `PATCH /swimlanes/{id}` |
| Delete | `DELETE /swimlanes/{id}?moveTo={targetId}` |
| Bulk reorder | `POST /swimlanes/bulk_update_order` |
| Set default | `PATCH /projects/{projectId}` |
| WIP limit | `PATCH /swimlane-userstory-statuses/{swimlaneStatusId}` |

#### Domain Layer (`feature/projects/domain`)

New file `Swimlane.kt`:
```kotlin
data class Swimlane(
    val id: Long,
    val name: String,
    val order: Long,
    val project: Long,
    val statuses: ImmutableList<SwimlaneStatus>
)

data class SwimlaneStatus(
    val id: Long,
    val name: String,
    val color: String,
    val order: Int,
    val isClosed: Boolean,
    val wipLimit: Int?,
    val swimlaneUserstoryStatusId: Long  // ID for PATCH /swimlane-userstory-statuses/{id}
)
```

New file `SwimlanesRepository.kt`:
```kotlin
interface SwimlanesRepository {
    suspend fun getSwimlanes(): ImmutableList<Swimlane>
    suspend fun createSwimlane(name: String): Swimlane
    suspend fun updateSwimlaneName(id: Long, name: String): Swimlane
    suspend fun deleteSwimlane(id: Long, moveTo: Long?)
    suspend fun bulkReorderSwimlanes(orderedIds: List<Long>)  // sends [[id, index], ...]
    suspend fun setDefaultSwimlane(swimlaneId: Long?)         // PATCH /projects/{id}
    suspend fun updateWipLimit(swimlaneUserstoryStatusId: Long, wipLimit: Int?)
}
```

#### DTO Layer (`feature/projects/dto`)

New file `SwimlaneDTO.kt`:
```kotlin
@Serializable
data class SwimlaneDTO(
    val id: Long,
    val name: String,
    val order: Long,
    val project: Long,
    val statuses: List<SwimlaneStatusDTO> = emptyList()
)

@Serializable
data class SwimlaneStatusDTO(
    val id: Long,
    val name: String,
    val color: String = "#999999",
    val order: Int = 0,
    @SerialName("is_closed") val isClosed: Boolean = false,
    @SerialName("wip_limit") val wipLimit: Int? = null,
    @SerialName("swimlane_userstory_status_id") val swimlaneUserstoryStatusId: Long
)
```

New file `SwimlaneRequestDTO.kt`:
```kotlin
// For create:
@Serializable
data class CreateSwimlaneRequestDTO(val project: Long, val name: String)

// For bulk reorder:
@Serializable
data class BulkReorderSwimlanesDTO(
    val project: Long,
    @SerialName("bulk_swimlanes") val bulkSwimlanes: List<List<Long>>  // [[id, order], ...]
)
```

#### Data Layer (`feature/projects/data`)

New `SwimlanesApi` interface + `@Single(binds = [SwimlanesApi::class])` impl:
```kotlin
interface SwimlanesApi {
    suspend fun getSwimlanes(projectId: Long): List<SwimlaneDTO>
    suspend fun createSwimlane(request: CreateSwimlaneRequestDTO): SwimlaneDTO
    suspend fun updateSwimlaneName(id: Long, name: String): SwimlaneDTO
    suspend fun deleteSwimlane(id: Long, moveTo: Long?)
    suspend fun bulkReorderSwimlanes(request: BulkReorderSwimlanesDTO)
    suspend fun updateWipLimit(swimlaneStatusId: Long, wipLimit: Int?)
    // Set default swimlane → PATCH /projects/{projectId} {"default_swimlane": id}
    suspend fun setDefaultSwimlane(projectId: Long, swimlaneId: Long?)
}
```

`SwimlanesRepositoryImpl` — `@Single(binds = [SwimlanesRepository::class])`, uses `TaigaSessionStorage.getCurrentProjectId()`.

`bulkReorderSwimlanes` implementation: takes `orderedIds: List<Long>`, maps to `orderedIds.mapIndexed { index, id -> listOf(id, index.toLong()) }`, passes to `api.bulkReorderSwimlanes(...)`.

`setDefaultSwimlane` needs to also load project — but for the PATCH it only needs the projectId, which comes from `TaigaSessionStorage`.

#### Navigation

- `SwimlanesNavDestination` — `@Serializable data object` (no params)
- `navigateToSwimlanes()` extension on `NavController`
- Wire into `SettingsScreen` as new `goToSwimlanesScreen: () -> Unit` param (inside `if (state.canSeeAttributes)` block)
- Wire into `SettingsNavGraph`: add `composable<SwimlanesNavDestination>` entry

#### UI (`feature/settings/ui/.../swimlanes/`)

**`SwimlanesState`**:
```kotlin
data class SwimlanesState(
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val isOperationLoading: Boolean = false,
    val swimlanes: ImmutableList<Swimlane> = persistentListOf(),
    val defaultSwimlaneId: Long? = null,
    val refresh: () -> Unit = {},
    // Create/edit swimlane
    val isEditDialogVisible: Boolean = false,
    val editingSwimlane: Swimlane? = null,
    val onAddClick: () -> Unit = {},
    val onEditClick: (Swimlane) -> Unit = {},
    val onSaveSwimlane: (name: String) -> Unit = {},
    val onDismissEditDialog: () -> Unit = {},
    // Delete swimlane
    val isDeleteDialogVisible: Boolean = false,
    val deletingSwimlane: Swimlane? = null,
    val onDeleteClick: (Swimlane) -> Unit = {},
    val onConfirmDelete: (moveTo: Long?) -> Unit = {},
    val onDismissDeleteDialog: () -> Unit = {},
    // Reorder
    val onReorder: (newOrder: ImmutableList<Swimlane>) -> Unit = {},
    // Set default
    val onSetDefault: (swimlane: Swimlane) -> Unit = {},
    // WIP limit
    val onWipLimitSave: (swimlaneUserstoryStatusId: Long, wipLimit: Int?) -> Unit = { _, _ -> }
)
```

**`SwimlanesViewModel`** init:
1. Launches two parallel calls: `getSwimlanes()` + `ProjectsRepository.getProjectDetails()` to get `defaultSwimlaneId`
2. Or: call only `getSwimlanes()` and load `defaultSwimlaneId` from `TaigaSessionStorage` if it already caches project details. Check existing storage before adding a second API call.

**`SwimlanesScreen`** layout (single `LazyColumn`):
- TopBar with "Add" action icon (same as Phase 2)
- Each swimlane row: name, "is default" chip if `id == defaultSwimlaneId`, edit/delete icon buttons, "Set as default" icon button
- Below each swimlane row: indent + list of `SwimlaneStatus` rows, each showing name, color dot, "WIP: N" chip (tappable to edit)

**Edit dialog** — name only:
- `OutlinedTextField` for name (required)
- Cancel / Save buttons

**Delete dialog** — same pattern as Phase 2's `deleteRequiresMoveTo = true` case:
- `ExposedDropdownMenuBox` for moveTo selection (other swimlanes)
- If swimlane count is 1 (last swimlane), no moveTo required — show simple confirm

**WIP limit dialog** — simple `AlertDialog`:
- `OutlinedTextField(keyboardType = KeyboardType.Number)` pre-filled with current wipLimit
- Empty input = `null` (no limit)
- Confirm calls `onWipLimitSave(swimlaneUserstoryStatusId, input.toIntOrNull())`

**Bulk reorder:** wrap `LazyColumn` items in the DnD primitives from uikit. After drag ends, call `onReorder(newList)`. ViewModel sends `bulkReorderSwimlanes` and updates local state optimistically.

#### SettingsNavGraph addition

```kotlin
composable<SwimlanesNavDestination> {
    SwimlanesScreen(showSnackbar = showSnackbar)
}
```

Also add `goToSwimlanesScreen` param to `SettingsScreen` composable in the graph entry.

#### SettingsScreen change

Add inside `if (state.canSeeAttributes)`:
```kotlin
ListItem(
    modifier = Modifier.clickable { goToSwimlanesScreen() },
    headlineContent = { Text(stringResource(RString.settings_swimlanes)) },
    leadingContent = { Icon(Icons.Default.ViewKanban, contentDescription = null) }
)
```

#### Strings to add (`strings.xml`)

```xml
<string name="settings_swimlanes">Swimlanes</string>
<string name="swimlanes_add_title">Add Swimlane</string>
<string name="swimlanes_edit_title">Edit Swimlane</string>
<string name="swimlanes_name_hint">Name</string>
<string name="swimlanes_default">Default</string>
<string name="swimlanes_set_as_default">Set as default</string>
<string name="swimlanes_wip_limit">WIP Limit</string>
<string name="swimlanes_wip_limit_hint">Max items (empty = no limit)</string>
<string name="swimlanes_delete_text">Delete this swimlane?</string>
<string name="swimlanes_move_to">Move user stories to</string>
```

#### Tasks
- [ ] `Swimlane` + `SwimlaneStatus` domain models in `feature/projects/domain`
- [ ] `SwimlanesRepository` interface in `feature/projects/domain`
- [ ] `SwimlaneDTO` + `SwimlaneStatusDTO` + `CreateSwimlaneRequestDTO` + `BulkReorderSwimlanesDTO` in `feature/projects/dto`
- [ ] `SwimlanesApi` interface + impl in `feature/projects/data` (register `@Single`)
- [ ] `SwimlanesRepositoryImpl` in `feature/projects/data` (register `@Single`)
- [ ] `SwimlanesNavDestination` + `navigateToSwimlanes()`
- [ ] `SwimlanesState` + `SwimlanesViewModel` + `SwimlanesScreen`
- [ ] Edit dialog (name only)
- [ ] Delete dialog (moveTo dropdown, or simple confirm when last swimlane)
- [ ] WIP limit dialog (numeric input, nullable)
- [ ] Drag-and-drop reorder (consult `uikit-guide` subagent for DnD primitives)
- [ ] Add `goToSwimlanesScreen` to `SettingsScreen` + `SettingsState` (no new field needed — gate is already `canSeeAttributes`)
- [ ] Wire `SwimlanesNavDestination` in `SettingsNavGraph`
- [ ] Add string resources to `strings.xml`

---

### Phase 5 — Default Values / Presets

**Screen:** `Settings → Default Values`
**API:** `PATCH /projects/{id}`
**Fields:** Default status for each entity type, default priority, severity, points, swimlane (all nullable Long IDs via dropdowns)

**Dependency:** Requires Phase 2 data (status lists) to populate dropdowns.

**Tasks:**
- [ ] `DefaultValuesNavDestination`
- [ ] `DefaultValuesScreen` + `DefaultValuesViewModel` + state
- [ ] Load all status/type lists + current project defaults
- [ ] Dropdown selectors per field (reuse `DropdownSelector` from uikit)
- [ ] Wire into `SettingsScreen` (admin-only) and `SettingsNavGraph`

---

### Phase 6 — CSV Export (Low priority)

**Screen:** `Settings → CSV Export`

**Operations:** Regenerate UUID and delete UUID for each entity type (epics, user stories, tasks, issues). Display current shareable CSV link.

**Tasks:**
- [ ] Repository methods: regenerate/delete CSV UUID (4 types)
- [ ] `CsvExportNavDestination`
- [ ] `CsvExportScreen` + `CsvExportViewModel` + state
- [ ] Copy-to-clipboard button for CSV URL
- [ ] Wire into `SettingsScreen` (admin-only) and `SettingsNavGraph`

---

### Deferred — Change Project Logo

**API:** `POST /projects/{id}/change_logo` (multipart), `POST /projects/{id}/remove_logo`

High complexity due to multipart file upload + image picker (platform-specific). Defer until after Phase 1.

---

## Screen Navigation Map (target state)

```
Settings
├── User (existing)
├── Interface (existing)
├── About (existing)
└── [admin only]
    ├── Project Details       ← Phase 1A
    ├── Modules               ← Phase 1B
    ├── Default Values        ← Phase 5 (depends on Phase 2)
    ├── Swimlanes             ← Phase 4
    ├── CSV Export            ← Phase 6
    └── Attributes (existing)
        ├── Tags (existing)
        ├── Project Values    ← Phase 2
        └── Custom Attributes ← Phase 3
```

---

## Implementation Order

1. **Phase 1A** — Project Details editing (fastest, highest value)
2. **Phase 1B** — Module toggles
3. **Phase 2** — Project Values (statuses/types/priorities)
4. **Phase 3** — Custom Attributes
5. **Phase 5** — Default Values (needs Phase 2 data)
6. **Phase 4** — Swimlanes
7. **Phase 6** — CSV Export
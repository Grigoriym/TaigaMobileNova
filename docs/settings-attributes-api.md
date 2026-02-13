# Settings-Attributes Feature API Documentation

This document describes the API endpoints and data models used by the **Settings-Attributes** feature in Taiga. This is intended to help implement the same functionality in a mobile application (Android/Kotlin).

> **Note:** Field types have been verified against the Django backend source code. This document is accurate for the Taiga backend as of this writing.

## Table of Contents

1. [Overview](#overview)
2. [Statuses](#statuses)
   - [Epic Statuses](#epic-statuses)
   - [User Story Statuses](#user-story-statuses)
   - [Task Statuses](#task-statuses)
   - [Issue Statuses](#issue-statuses)
3. [Points](#points)
4. [Priorities](#priorities)
5. [Severities](#severities)
6. [Issue Types](#issue-types)
7. [Custom Fields](#custom-fields)
8. [Tags](#tags)
9. [Due Dates](#due-dates)
10. [Swimlanes](#swimlanes)

---

## Overview

The Settings-Attributes screen allows project administrators to configure various project attributes:

- **Statuses**: Define workflow states for Epics, User Stories, Tasks, and Issues
- **Points**: Define story point values for estimation
- **Priorities**: Define priority levels for Issues
- **Severities**: Define severity levels for Issues
- **Issue Types**: Define types/categories for Issues
- **Custom Fields**: Define custom attributes for Epics, User Stories, Tasks, and Issues
- **Tags**: Manage project-wide tags with colors
- **Due Dates**: Configure due date status thresholds
- **Swimlanes**: Configure Kanban board swimlanes

All endpoints require authentication and project admin permissions.

**Base URL Pattern**: `{API_BASE_URL}/api/v1`

---

## Statuses

### Epic Statuses

#### Epic Status Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/epic-statuses?project={projectId}` | List all epic statuses for a project |
| POST | `/epic-statuses` | Create a new epic status |
| PATCH | `/epic-statuses/{id}` | Update an epic status |
| DELETE | `/epic-statuses/{id}?moveTo={replacementId}` | Delete an epic status (must specify replacement) |

#### Model: `EpicStatus`

```kotlin
data class EpicStatus(
    val id: Int,                    // Unique identifier (AutoField - 32-bit)
    val name: String,               // Status name (max 255 chars, required)
    val slug: String,               // URL-friendly identifier (auto-generated from name, max 255 chars)
    val color: String,              // Hex color code (max 20 chars, default "#999999")
    val is_closed: Boolean,         // Whether this status marks epic as closed (default false)
    val order: Int,                 // Display order (default 10)
    val project: Int                // Project ID (required)
)
```

#### Epic Status Create/Update Request

```kotlin
data class EpicStatusRequest(
    val name: String,               // Required (max 255 chars)
    val color: String? = null,      // Optional, defaults to "#999999" (max 20 chars)
    val is_closed: Boolean = false, // Optional, defaults to false
    val order: Int? = null,         // Optional (default 10), read-only in some contexts
    val project: Int                // Required for create
)
```

---

### User Story Statuses

#### US Status Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/userstory-statuses?project={projectId}` | List all US statuses |
| POST | `/userstory-statuses` | Create a new US status |
| PATCH | `/userstory-statuses/{id}` | Update a US status |
| DELETE | `/userstory-statuses/{id}?moveTo={replacementId}` | Delete a US status |

#### Model: `UserStoryStatus`

```kotlin
data class UserStoryStatus(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Max 255 chars, required
    val slug: String,               // Auto-generated from name, max 255 chars
    val color: String,              // Max 20 chars, default "#999999"
    val is_closed: Boolean,         // Marks US as closed, default false
    val is_archived: Boolean,       // Hides from Kanban (US-specific), default false
    val wip_limit: Int?,            // WIP limit for Kanban column (nullable)
    val order: Int,                 // Default 10
    val project: Int                // Project ID
)
```

#### US Status Create/Update Request

```kotlin
data class UserStoryStatusRequest(
    val name: String,               // Required (max 255 chars)
    val color: String? = null,      // Optional, defaults to "#999999"
    val is_closed: Boolean = false, // Optional, defaults to false
    val is_archived: Boolean = false, // US-specific field, defaults to false
    val order: Int? = null,         // Optional (default 10)
    val project: Int                // Required for create
)
```

#### Additional Endpoint: Update WIP Limit

| Method | Endpoint | Description |
|--------|----------|-------------|
| PATCH | `/userstory-statuses/{id}` | Update WIP limit |

```kotlin
data class WipLimitRequest(
    val wip_limit: Int?             // null to remove limit
)
```

---

### Task Statuses

#### Task Status Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/task-statuses?project={projectId}` | List all task statuses |
| POST | `/task-statuses` | Create a new task status |
| PATCH | `/task-statuses/{id}` | Update a task status |
| DELETE | `/task-statuses/{id}?moveTo={replacementId}` | Delete a task status |

#### Model: `TaskStatus`

```kotlin
data class TaskStatus(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Max 255 chars, required
    val slug: String,               // Auto-generated from name, max 255 chars
    val color: String,              // Max 20 chars, default "#999999"
    val is_closed: Boolean,         // Default false
    val order: Int,                 // Default 10
    val project: Int                // Project ID
)
```

---

### Issue Statuses

#### Issue Status Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/issue-statuses?project={projectId}` | List all issue statuses |
| POST | `/issue-statuses` | Create a new issue status |
| PATCH | `/issue-statuses/{id}` | Update an issue status |
| DELETE | `/issue-statuses/{id}?moveTo={replacementId}` | Delete an issue status |

#### Model: `IssueStatus`

```kotlin
data class IssueStatus(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Max 255 chars, required
    val slug: String,               // Auto-generated from name, max 255 chars
    val color: String,              // Max 20 chars, default "#999999"
    val is_closed: Boolean,         // Default false
    val order: Int,                 // Default 10
    val project: Int                // Project ID
)
```

---

## Points

Points are used for story point estimation in User Stories.

### Points Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/points?project={projectId}` | List all points |
| POST | `/points` | Create a new point value |
| PATCH | `/points/{id}` | Update a point value |
| DELETE | `/points/{id}?moveTo={replacementId}` | Delete a point value |

### Model: `Point`

```kotlin
data class Point(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Display name (e.g., "1", "2", "3", "?"), max 255 chars
    val value: Float?,              // Numeric value (nullable for "?" points), Django FloatField
    val order: Int,                 // Default 10
    val project: Int                // Project ID
)
```

### Points Create/Update Request

```kotlin
data class PointRequest(
    val name: String,               // Required (max 255 chars)
    val value: Float? = null,       // Optional/nullable
    val order: Int? = null,         // Optional (default 10)
    val project: Int                // Required for create
)
```

---

## Priorities

Priorities are used for Issues only.

### Priorities Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/priorities?project={projectId}` | List all priorities |
| POST | `/priorities` | Create a new priority |
| PATCH | `/priorities/{id}` | Update a priority |
| DELETE | `/priorities/{id}?moveTo={replacementId}` | Delete a priority |

### Model: `Priority`

```kotlin
data class Priority(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Max 255 chars, required
    val color: String,              // Max 20 chars, default "#999999"
    val order: Int,                 // Default 10
    val project: Int                // Project ID
)
```

### Priorities Create/Update Request

```kotlin
data class PriorityRequest(
    val name: String,               // Required (max 255 chars)
    val color: String? = null,      // Optional, defaults to "#999999"
    val order: Int? = null,         // Optional (default 10)
    val project: Int                // Required for create
)
```

---

## Severities

Severities are used for Issues only.

### Severities Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/severities?project={projectId}` | List all severities |
| POST | `/severities` | Create a new severity |
| PATCH | `/severities/{id}` | Update a severity |
| DELETE | `/severities/{id}?moveTo={replacementId}` | Delete a severity |

### Model: `Severity`

```kotlin
data class Severity(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Max 255 chars, required
    val color: String,              // Max 20 chars, default "#999999"
    val order: Int,                 // Default 10
    val project: Int                // Project ID
)
```

---

## Issue Types

Issue types categorize Issues (e.g., Bug, Enhancement, Question).

### Issue Types Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/issue-types?project={projectId}` | List all issue types |
| POST | `/issue-types` | Create a new issue type |
| PATCH | `/issue-types/{id}` | Update an issue type |
| DELETE | `/issue-types/{id}?moveTo={replacementId}` | Delete an issue type |

### Model: `IssueType`

```kotlin
data class IssueType(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Max 255 chars, required
    val color: String,              // Max 20 chars, default "#999999"
    val order: Int,                 // Default 10
    val project: Int                // Project ID
)
```

---

## Custom Fields

Custom fields allow defining additional attributes for Epics, User Stories, Tasks, and Issues.

### Custom Fields Endpoints

#### Custom Field Definitions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/epic-custom-attributes?project={projectId}` | List epic custom fields |
| GET | `/userstory-custom-attributes?project={projectId}` | List US custom fields |
| GET | `/task-custom-attributes?project={projectId}` | List task custom fields |
| GET | `/issue-custom-attributes?project={projectId}` | List issue custom fields |
| POST | `/{type}-custom-attributes` | Create custom field |
| PATCH | `/{type}-custom-attributes/{id}` | Update custom field |
| DELETE | `/{type}-custom-attributes/{id}` | Delete custom field |

Where `{type}` is one of: `epic`, `userstory`, `task`, `issue`

#### Custom Field Values

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/epics/custom-attributes-values/{epicId}` | Get epic's custom values |
| GET | `/userstories/custom-attributes-values/{usId}` | Get US's custom values |
| GET | `/tasks/custom-attributes-values/{taskId}` | Get task's custom values |
| GET | `/issues/custom-attributes-values/{issueId}` | Get issue's custom values |
| PATCH | `/{type}/custom-attributes-values/{objectId}` | Update custom values |

### Model: `CustomAttribute`

```kotlin
data class CustomAttribute(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Max 64 chars, required
    val description: String,        // NOT nullable (can be empty string "")
    val type: String,               // Field type (max 16 chars), see enum below
    val extra: Any?,                // JSONField - nullable, dropdown options for dropdown type
    val order: Long,                // BigIntegerField - 64-bit! (uses timestamp_ms as default)
    val project: Int,               // Project ID
    val created_date: String,       // ISO 8601 datetime: "yyyy-MM-dd'T'HH:mm:ssZ"
    val modified_date: String       // ISO 8601 datetime: "yyyy-MM-dd'T'HH:mm:ssZ"
)

/**
 * Custom field types - stored as string in backend
 */
object CustomFieldType {
    const val TEXT = "text"           // Single line text
    const val MULTILINE = "multiline" // Multi-line text
    const val RICHTEXT = "richtext"   // Rich text (markdown)
    const val DATE = "date"           // Date picker
    const val URL = "url"             // URL field
    const val DROPDOWN = "dropdown"   // Dropdown selection (uses `extra` for options)
    const val CHECKBOX = "checkbox"   // Boolean checkbox
    const val NUMBER = "number"       // Numeric field
}
```

### Custom Fields Create/Update Request

```kotlin
data class CustomAttributeRequest(
    val name: String,               // Required, max 64 chars
    val description: String? = null, // Optional (defaults to empty string "")
    val type: String,               // Required, one of CustomFieldType values (max 16 chars)
    val extra: List<String>? = null, // Required for dropdown type only
    val order: Long? = null,        // Optional (defaults to timestamp in milliseconds)
    val project: Int                // Required for create
)
```

### Custom Attribute Values Model

```kotlin
data class CustomAttributesValues(
    val attributes_values: Map<String, Any?>,  // key is attribute ID as string
    val version: Int                           // For optimistic locking
)
```

**Note:** The `attributes_values` map uses the custom attribute ID as the key (as a string), and the value type depends on the field type:

- `text`, `multiline`, `richtext`, `url` -> `String`
- `date` -> `String` (ISO date format)
- `dropdown` -> `String` (selected option)
- `checkbox` -> `Boolean`
- `number` -> `Number`

---

## Tags

Tags are project-wide labels that can be applied to any item.

### Tags Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/projects/{projectId}/tags_colors` | Get all tags with colors |
| POST | `/projects/{projectId}/create_tag` | Create a new tag |
| POST | `/projects/{projectId}/edit_tag` | Edit tag name/color |
| POST | `/projects/{projectId}/delete_tag` | Delete a tag |
| POST | `/projects/{projectId}/mix_tags` | Merge multiple tags into one |

### Response: Tags Colors

```kotlin
// Response is a Map<String, String?> where key is tag name, value is color
// Example: {"bug": "#FF0000", "feature": "#00FF00", "urgent": null}
typealias TagsColors = Map<String, String?>
```

### Model: `Tag` (Frontend representation)

```kotlin
data class Tag(
    val name: String,               // Tag name (TextField - no enforced max length in backend)
    val color: String?              // Hex color, nullable
)
```

> **Note:** Tags are stored in the database as `ArrayField(TextField)`. There's no enforced maximum length in the backend, but reasonable limits should be applied client-side for UX.

### Create Tag Request

```kotlin
data class CreateTagRequest(
    val tag: String,                // Tag name
    val color: String?              // Optional hex color
)
```

### Edit Tag Request

```kotlin
data class EditTagRequest(
    val from_tag: String,           // Original tag name
    val to_tag: String?,            // New tag name (null to keep same)
    val color: String?              // New color (null to keep same)
)
```

### Delete Tag Request

```kotlin
data class DeleteTagRequest(
    val tag: String                 // Tag name to delete
)
```

### Mix (Merge) Tags Request

```kotlin
data class MixTagsRequest(
    val to_tag: String,             // Target tag name (will be kept)
    val from_tags: List<String>     // Tags to merge into target (will be deleted)
)
```

---

## Due Dates

Due date statuses define visual indicators based on proximity to due date.

### Due Dates Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/userstory-due-dates?project={projectId}` | List US due date statuses |
| GET | `/task-due-dates?project={projectId}` | List task due date statuses |
| GET | `/issue-due-dates?project={projectId}` | List issue due date statuses |
| POST | `/{type}-due-dates` | Create due date status |
| POST | `/{type}-due-dates/create_default` | Create default due date statuses |
| PATCH | `/{type}-due-dates/{id}` | Update due date status |
| DELETE | `/{type}-due-dates/{id}` | Delete due date status |

Where `{type}` is one of: `userstory`, `task`, `issue`

### Model: `DueDateStatus`

```kotlin
data class DueDateStatus(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // Status name (max 255 chars, required)
    val color: String,              // Hex color (max 20 chars, default "#999999")
    val days_to_due: Int?,          // Days threshold (nullable), positive = before, negative = after
    val by_default: Boolean,        // True for the default "no due date" status, default false
    val order: Int,                 // Default 10
    val project: Int                // Project ID
)
```

### Due Dates Create/Update Request

```kotlin
data class DueDateStatusRequest(
    val name: String,               // Required (max 255 chars)
    val color: String? = null,      // Optional, defaults to "#999999"
    val days_to_due: Int? = null,   // e.g., 7 = "7 days before due", -1 = "1 day past due"
    val order: Int? = null,         // Optional (default 10)
    val project: Int                // Required for create
)
```

### Create Default Due Dates Request

```kotlin
data class CreateDefaultDueDatesRequest(
    val project_id: Int
)
```

**Note:** The `by_default` status cannot be deleted and typically has `days_to_due = null`.

---

## Swimlanes

Swimlanes are horizontal lanes in the Kanban board for organizing User Stories.

### Swimlanes Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/swimlanes?project={projectId}` | List all swimlanes |
| POST | `/swimlanes` | Create a new swimlane |
| PATCH | `/swimlanes/{id}` | Update swimlane name |
| DELETE | `/swimlanes/{id}?moveTo={replacementId}` | Delete swimlane |
| POST | `/swimlanes/bulk_update_order` | Reorder swimlanes |
| PATCH | `/projects/{projectId}` | Set default swimlane |

### Model: `Swimlane`

```kotlin
data class Swimlane(
    val id: Int,                    // AutoField - 32-bit
    val name: String,               // TextField - NO max length in backend (use reasonable limit client-side)
    val order: Long,                // BigIntegerField - 64-bit! (uses timestamp_ms as default)
    val project: Int,               // Project ID
    val statuses: List<SwimlaneUserStoryStatus>?  // WIP limits per status (optional in response)
)

data class SwimlaneUserStoryStatus(
    val id: Int,                    // AutoField - 32-bit
    val status: Int,                // UserStoryStatus ID
    val swimlane: Int,              // Swimlane ID
    val wip_limit: Int?             // WIP limit for this combination (nullable)
)
```

### Create Swimlane Request

```kotlin
data class CreateSwimlaneRequest(
    val project: Int,               // Required
    val name: String,               // Required (no max length in backend)
    val order: Long? = null         // Optional (defaults to timestamp_ms), read-only in validator
)
```

### Update Swimlane Request

```kotlin
data class UpdateSwimlaneRequest(
    val name: String
)
```

### Bulk Update Order Request

```kotlin
data class BulkUpdateSwimlaneOrderRequest(
    val project: Int,
    val bulk_swimlanes: List<List<Long>>  // [[swimlaneId, newOrder], ...] - use Long for order values
)
```

### Set Default Swimlane

```kotlin
// PATCH /projects/{projectId}
data class SetDefaultSwimlaneRequest(
    val default_swimlane: Int?      // Swimlane ID, or null for no default
)
```

### Update Swimlane WIP Limit

| Method | Endpoint | Description |
|--------|----------|-------------|
| PATCH | `/swimlane-userstory-statuses/{id}` | Update WIP limit for swimlane-status |

```kotlin
data class SwimlaneWipLimitRequest(
    val wip_limit: Int?             // null to remove limit
)
```

---

## Bulk Operations: Reordering

All attribute types support drag-and-drop reordering. The frontend sends updates to all items with new order values.

### General Pattern

```kotlin
// After drag-and-drop, save all items with updated order values
suspend fun reorderItems(items: List<T>) {
    items.forEachIndexed { index, item ->
        item.order = index
        updateItem(item)  // PATCH request
    }
}
```

---

## Error Handling

### Common Error Responses

```kotlin
// 400 Bad Request - Validation errors
data class ValidationError(
    val _error_message: String?,        // General error
    val field_name: List<String>?       // Field-specific errors
)

// 403 Forbidden - Permission denied
// 404 Not Found - Resource not found
// 409 Conflict - Optimistic locking conflict (for custom attributes values)
```

### Delete Constraints

When deleting statuses, priorities, severities, or issue types, you **must** provide a `moveTo` parameter specifying which value should replace the deleted one on existing items. The API will reject deletion if:

- No `moveTo` is specified
- The `moveTo` value doesn't exist
- This is the last remaining value (cannot delete all)

---

## Default Color

When no color is specified for items that support colors, the default color is: `#999999`

> **Note:** The default color `#999999` is set in the Django model definitions (`default="#999999"`). Some frontend implementations may use `#A9AABC` but the backend default is `#999999`.

---

## Notes for Mobile Implementation (Kotlin/Android)

1. **Optimistic Updates**: Consider implementing optimistic UI updates for better UX
2. **Offline Support**: Cache attribute values locally for offline viewing
3. **Color Picker**: Implement a color picker UI component for color selection
4. **Drag-and-Drop**: Use RecyclerView with ItemTouchHelper for reordering
5. **Validation**: Enforce max length constraints client-side before API calls
6. **Permissions**: Only project admins can access these settings; check `i_am_admin` on project object

### Kotlin-Specific Recommendations

1. **JSON Serialization**: Use `@SerialName` annotations for snake_case field mapping:
   ```kotlin
   @Serializable
   data class EpicStatus(
       val id: Int,
       val name: String,
       val slug: String,
       val color: String,
       @SerialName("is_closed") val isClosed: Boolean,
       val order: Int,
       val project: Int
   )
   ```

2. **DateTime Parsing**: Use `java.time.OffsetDateTime` or `kotlinx-datetime` for parsing ISO 8601 dates:
   ```kotlin
   val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
   val dateTime = OffsetDateTime.parse(dateString, formatter)
   ```

3. **Nullable vs Non-Null**: Pay attention to nullable fields:
   - Use `Int?` for: `wip_limit`, `days_to_due`, `value` (Points)
   - Use `Long` for: `order` in Swimlane and CustomAttributes
   - Use `String?` for: `color` (in responses it's non-null, but requests accept null)

4. **Rate Limiting**: Implement retry with exponential backoff for 429 responses:
    ```kotlin
    sealed class ApiResult<T> {
        data class Success<T>(val data: T) : ApiResult<T>()
        data class RateLimited<T>(val retryAfter: Int?) : ApiResult<T>()
        data class Error<T>(val message: String) : ApiResult<T>()
    }
    ```

---

## Backend Verification (Resolved)

The following details have been verified against the Django backend source code:

### 1. Data Types for IDs (`Int` vs `Long`)

| Field | Kotlin Type | Django Field | Notes |
|-------|-------------|--------------|-------|
| `id` (all models) | `Int` | `AutoField` | 32-bit signed integer |
| `project` | `Int` | `ForeignKey` → `AutoField` | 32-bit signed integer |
| `order` (most models) | `Int` | `IntegerField` | 32-bit, default=10 |
| `order` (Swimlane) | `Long` | `BigIntegerField` | 64-bit, default=timestamp_ms |
| `order` (CustomAttributes) | `Long` | `BigIntegerField` | 64-bit, default=timestamp_ms |

### 2. Nullable Fields Summary

**Truly Nullable (`null=True` in Django → use nullable type in Kotlin):**

- `wip_limit` (UserStoryStatus, SwimlaneUserStoryStatus)
- `days_to_due` (DueDateStatus models)
- `value` (Points)
- `extra` (CustomAttributes - JSONField)

**Not Nullable but has Default Value:**

- `is_closed` → `Boolean` (default `false`)
- `is_archived` → `Boolean` (default `false`)
- `by_default` → `Boolean` (default `false`)
- `color` → `String` (default `"#999999"`)
- `order` → `Int`/`Long` (default `10` or `timestamp_ms`)

**Not Nullable, Can Be Empty String:**

- `description` (CustomAttributes) → `String` (default `""`)

### 3. Maximum String Lengths

| Field | Max Length | Model(s) |
|-------|------------|----------|
| `name` | 255 chars | All status models, Points, Priority, Severity, IssueType, DueDateStatus |
| `name` | 64 chars | CustomAttributes only |
| `name` | **Unlimited** | Swimlane (TextField) |
| `slug` | 255 chars | All status models |
| `color` | 20 chars | All models with color |
| `type` | 16 chars | CustomAttributes |

### 4. Date Format

```
Format: "yyyy-MM-dd'T'HH:mm:ssZ"
Example: "2024-01-15T14:30:00+0000"
```

From Django REST Framework settings: `"DATETIME_FORMAT": "%Y-%m-%dT%H:%M:%S%z"`

This is ISO 8601 format with timezone offset.

### 5. Slug Auto-Generation

**`slug` is ALWAYS auto-generated from `name`** - it cannot be set manually.

The backend overwrites any provided slug value in the `save()` method:
```python
def save(self, *args, **kwargs):
    self.slug = slugify_uniquely_for_queryset(self.name, qs)
    return super().save(*args, **kwargs)
```

### 6. Pagination Support

**Pagination is enabled** for all list endpoints:

| Setting | Value |
|---------|-------|
| Default page size | 30 items |
| Max page size | 1000 items |
| Query parameter | `page_size` |

Example: `GET /api/v1/epic-statuses?project=123&page_size=50`

Response headers include pagination metadata.

### 7. Rate Limiting

**Rate limiting is configurable but mostly disabled by default:**

```python
"DEFAULT_THROTTLE_RATES": {
    "anon-write": None,    # Unlimited
    "user-write": None,    # Unlimited
    "anon-read": None,     # Unlimited
    "user-read": None,     # Unlimited
    # ... other specific throttles
}
```

Production deployments may enable rate limiting via configuration. The mobile app should handle `429 Too Many Requests` responses gracefully with exponential backoff.

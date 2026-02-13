# Taiga Settings-Project Features API Documentation

Complete API documentation for **Settings-Project** features in Taiga, with verified Kotlin types for Android development.

> **Base URL**: `{API_HOST}/api/v1`
>
> **Authentication**: All endpoints require `Authorization: Bearer {token}` header

---

## Table of Contents
- [1. Project Details](#1-project-details)
- [2. Presets / Default Values](#2-presets--default-values)
- [3. Modules](#3-modules)
- [4. Swimlanes](#4-swimlanes)
- [5. Project Values (Statuses, Types, etc.)](#5-project-values)
- [6. Custom Attributes](#6-custom-attributes)
- [7. Tags Management](#7-tags-management)
- [8. CSV Export](#8-csv-export)
- [9. Kotlin Models](#9-kotlin-models)
- [10. Validation Rules](#10-validation-rules)
- [11. Error Handling](#11-error-handling)
- [12. Permissions](#12-permissions)
- [13. Retrofit Interface](#13-retrofit-interface)

---

## 1. Project Details

### 1.1 Get Project

**Endpoint**: `GET /projects/{projectId}`

**Alternative**: `GET /projects/by_slug?slug={projectSlug}`

**Response**: [ProjectDetail](#projectdetail)

---

### 1.2 Update Project Details

**Endpoint**: `PATCH /projects/{projectId}`

**Request Body**:
```json
{
  "name": "string",
  "description": "string",
  "is_private": true,
  "is_looking_for_people": true,
  "looking_for_people_note": "string",
  "is_contact_activated": true,
  "tags": ["string"],
  "tags_colors": {"tag1": "#ff0000"}
}
```

**Response**: Updated [ProjectDetail](#projectdetail)

**Read-only fields** (cannot be set):
- `created_date`, `modified_date`, `slug`, `blocked_code`, `owner`

---

### 1.3 Change Project Logo

**Endpoint**: `POST /projects/{projectId}/change_logo`

**Content-Type**: `multipart/form-data`

**Request**: `FormData` with `logo` file

**Response**: Updated [ProjectDetail](#projectdetail)

---

### 1.4 Remove Project Logo

**Endpoint**: `POST /projects/{projectId}/remove_logo`

**Request Body**: Empty `{}`

**Response**: Updated [ProjectDetail](#projectdetail)

---

## 2. Presets / Default Values

### 2.1 Update Default Values

**Endpoint**: `PATCH /projects/{projectId}`

**Request Body**:
```json
{
  "default_epic_status": 123,
  "default_us_status": 123,
  "default_task_status": 456,
  "default_issue_status": 789,
  "default_issue_type": 101,
  "default_priority": 102,
  "default_severity": 103,
  "default_points": 104,
  "default_swimlane": 105
}
```

All values are `Long?` (nullable) IDs referencing the respective entities.

**Response**: Updated [ProjectDetail](#projectdetail)

---

## 3. Modules

### 3.1 Update Modules Settings

**Endpoint**: `PATCH /projects/{projectId}`

**Request Body**:
```json
{
  "is_epics_activated": true,
  "is_backlog_activated": true,
  "is_kanban_activated": true,
  "is_issues_activated": true,
  "is_wiki_activated": true,
  "is_contact_activated": true,
  "total_milestones": 4,
  "total_story_points": 100.0,
  "videoconferences": "jitsi",
  "videoconferences_extra_data": "room-prefix"
}
```

**Videoconference Options**:
| Value | Description |
|-------|-------------|
| `null` | Disabled |
| `"whereby-com"` | Whereby |
| `"jitsi"` | Jitsi |
| `"talky"` | Talky |
| `"custom"` | Custom URL (full URL in `videoconferences_extra_data`) |

**Response**: Updated [ProjectDetail](#projectdetail)

---

## 4. Swimlanes

### 4.1 List Swimlanes

**Endpoint**: `GET /swimlanes?project={projectId}`

**Response**: `List<Swimlane>`

---

### 4.2 Create Swimlane

**Endpoint**: `POST /swimlanes`

**Request Body**:
```json
{
  "project": 123,
  "name": "string",
  "order": 1
}
```

**Response**: Created [Swimlane](#swimlane)

---

### 4.3 Edit Swimlane

**Endpoint**: `PATCH /swimlanes/{swimlaneId}`

**Request Body**:
```json
{
  "name": "string"
}
```

**Response**: Updated [Swimlane](#swimlane)

---

### 4.4 Delete Swimlane

**Endpoint**: `DELETE /swimlanes/{swimlaneId}?moveTo={targetSwimlaneId}`

**Query Parameters**:
- `moveTo`: **REQUIRED** (except when deleting the last swimlane)

**Response**: `HTTP 204 No Content`

**Note**: If deleting the last swimlane, `moveTo` can be omitted and all user stories will have their swimlane set to `null`.

---

### 4.5 Bulk Update Swimlane Order

**Endpoint**: `POST /swimlanes/bulk_update_order`

**Request Body**:
```json
{
  "project": 123,
  "bulk_swimlanes": [[1, 0], [2, 1], [3, 2]]
}
```

Format: `[[swimlaneId, newOrder], ...]`

**Response**: `HTTP 204 No Content`

---

### 4.6 Set Default Swimlane

**Endpoint**: `PATCH /projects/{projectId}`

**Request Body**:
```json
{
  "default_swimlane": 123
}
```

---

### 4.7 Update Swimlane WIP Limit

**Endpoint**: `PATCH /swimlane-userstory-statuses/{swimlaneStatusId}`

**Request Body**:
```json
{
  "wip_limit": 5
}
```

**Response**: Updated [SwimlaneUserStoryStatus](#swimlaneuserstorystatus)

---

## 5. Project Values

Manage statuses, types, priorities, severities, and points.

### 5.1 Endpoints

| Resource | Endpoint |
|----------|----------|
| Epic Statuses | `/epic-statuses` |
| User Story Statuses | `/userstory-statuses` |
| Task Statuses | `/task-statuses` |
| Issue Statuses | `/issue-statuses` |
| Issue Types | `/issue-types` |
| Priorities | `/priorities` |
| Severities | `/severities` |
| Points | `/points` |
| Due Dates (US) | `/userstory-due-dates` |
| Due Dates (Task) | `/task-due-dates` |
| Due Dates (Issue) | `/issue-due-dates` |

### 5.2 List Values

**Endpoint**: `GET /{resource}?project={projectId}`

**Response**: Array of value objects

---

### 5.3 Create Value

**Endpoint**: `POST /{resource}`

**Request Body**:
```json
{
  "project": 123,
  "name": "string",
  "color": "#A9AABC",
  "order": 1,
  "is_closed": false,
  "is_archived": false
}
```

**Response**: Created value object

---

### 5.4 Update Value

**Endpoint**: `PATCH /{resource}/{valueId}`

**Request Body**: Fields to update

**Response**: Updated value object

---

### 5.5 Delete Value

**Endpoint**: `DELETE /{resource}/{valueId}?moveTo={replacementValueId}`

**Query Parameters**:
- `moveTo`: **REQUIRED** - ID of value to reassign items to

**Response**: `HTTP 204 No Content`

---

### 5.6 Bulk Update Order

**Endpoint**: `POST /{resource}/bulk_update_order`

**Request Body**:
```json
{
  "project": 123,
  "bulk_{resource}": [[1, 0], [2, 1], [3, 2]]
}
```

Use the appropriate key: `bulk_userstory_statuses`, `bulk_epic_statuses`, `bulk_task_statuses`, `bulk_issue_statuses`, `bulk_issue_types`, `bulk_priorities`, `bulk_severities`, `bulk_points`

**Response**: `HTTP 204 No Content`

---

## 6. Custom Attributes

### 6.1 Endpoints per Type

| Type | Endpoint |
|------|----------|
| Epic | `/epic-custom-attributes` |
| User Story | `/userstory-custom-attributes` |
| Task | `/task-custom-attributes` |
| Issue | `/issue-custom-attributes` |

### 6.2 List Custom Attributes

**Endpoint**: `GET /{endpoint}?project={projectId}`

**Response**: `List<CustomAttribute>`

---

### 6.3 Create Custom Attribute

**Endpoint**: `POST /{endpoint}`

**Request Body**:
```json
{
  "project": 123,
  "name": "string",
  "description": "string",
  "type": "text",
  "order": 1,
  "extra": ["option1", "option2"]
}
```

**Attribute Types**:
| Type | Description |
|------|-------------|
| `"text"` | Single line text |
| `"multiline"` | Multi-line text |
| `"richtext"` | Rich text (HTML) |
| `"date"` | Date picker |
| `"url"` | URL field |
| `"dropdown"` | Dropdown (use `extra` for options) |
| `"checkbox"` | Boolean checkbox |
| `"number"` | Numeric field |

**Response**: Created [CustomAttribute](#customattribute)

---

### 6.4 Update Custom Attribute

**Endpoint**: `PATCH /{endpoint}/{attributeId}`

**Request Body**: Fields to update

**Response**: Updated [CustomAttribute](#customattribute)

---

### 6.5 Delete Custom Attribute

**Endpoint**: `DELETE /{endpoint}/{attributeId}`

**Response**: `HTTP 204 No Content`

---

## 7. Tags Management

### 7.1 Get Tags Colors

**Endpoint**: `GET /projects/{projectId}/tags_colors`

**Response**: `Map<String, String?>` (tag name to hex color)

---

### 7.2 Create Tag

**Endpoint**: `POST /projects/{projectId}/create_tag`

**Request Body**:
```json
{
  "tag": "string",
  "color": "#hexcolor"
}
```

**Response**: `HTTP 200 OK` (empty body)

---

### 7.3 Edit Tag

**Endpoint**: `POST /projects/{projectId}/edit_tag`

**Request Body**:
```json
{
  "from_tag": "string",
  "to_tag": "string",
  "color": "#hexcolor"
}
```

**Response**: `HTTP 200 OK` (empty body)

---

### 7.4 Delete Tag

**Endpoint**: `POST /projects/{projectId}/delete_tag`

**Request Body**:
```json
{
  "tag": "string"
}
```

**Response**: `HTTP 200 OK` (empty body)

---

### 7.5 Mix/Merge Tags

**Endpoint**: `POST /projects/{projectId}/mix_tags`

**Request Body**:
```json
{
  "from_tags": ["string"],
  "to_tag": "string"
}
```

**Response**: `HTTP 200 OK` (empty body)

---

## 8. CSV Export

### 8.1 Regenerate CSV UUID

**Endpoints**:
- `POST /projects/{projectId}/regenerate_epics_csv_uuid`
- `POST /projects/{projectId}/regenerate_userstories_csv_uuid`
- `POST /projects/{projectId}/regenerate_tasks_csv_uuid`
- `POST /projects/{projectId}/regenerate_issues_csv_uuid`

**Response**:
```json
{
  "uuid": "string"
}
```

---

### 8.2 Delete CSV UUID

**Endpoints**:
- `POST /projects/{projectId}/delete_epics_csv_uuid`
- `POST /projects/{projectId}/delete_userstories_csv_uuid`
- `POST /projects/{projectId}/delete_tasks_csv_uuid`
- `POST /projects/{projectId}/delete_issues_csv_uuid`

**Response**:
```json
{
  "uuid": null
}
```

---

### 8.3 Access CSV

**URL**: `GET /{resource}/csv?uuid={uuid}`

Example: `GET /userstories/csv?uuid=abc123-def456`

---

## 9. Kotlin Models

### Project

```kotlin
data class Project(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String,
    val createdDate: Instant,
    val modifiedDate: Instant,
    val owner: UserBasicInfo,
    val members: List<Long>,

    // Backlog settings
    val totalMilestones: Int?,
    val totalStoryPoints: Double?,

    // Module activation
    val isContactActivated: Boolean,
    val isEpicsActivated: Boolean,
    val isBacklogActivated: Boolean,
    val isKanbanActivated: Boolean,
    val isWikiActivated: Boolean,
    val isIssuesActivated: Boolean,

    // Video conferencing
    val videoconferences: String?,
    val videoconferencesExtraData: String?,

    // Privacy & permissions
    val isPrivate: Boolean,
    val anonPermissions: List<String>?,
    val publicPermissions: List<String>?,

    // Status
    val blockedCode: String?,
    val isFeatured: Boolean,
    val isLookingForPeople: Boolean,
    val lookingForPeopleNote: String,

    // Computed totals
    val totalsUpdatedDatetime: Instant,
    val totalFans: Int,
    val totalFansLastWeek: Int,
    val totalFansLastMonth: Int,
    val totalFansLastYear: Int,
    val totalActivity: Int,
    val totalActivityLastWeek: Int,
    val totalActivityLastMonth: Int,
    val totalActivityLastYear: Int,

    // Tags
    val tags: List<String>,
    val tagsColors: Map<String, String?>,

    // Defaults (IDs)
    val defaultEpicStatus: Long?,
    val defaultPoints: Long?,
    val defaultUsStatus: Long?,
    val defaultTaskStatus: Long?,
    val defaultPriority: Long?,
    val defaultSeverity: Long?,
    val defaultIssueStatus: Long?,
    val defaultIssueType: Long?,
    val defaultSwimlane: Long?,

    // User-specific computed fields
    val myPermissions: List<String>,
    val iAmOwner: Boolean,
    val iAmAdmin: Boolean,
    val iAmMember: Boolean,
    val isFan: Boolean,
    val isWatcher: Boolean,
    val totalWatchers: Int,
    val notifyLevel: Int?,
    val totalClosedMilestones: Int,

    // Logo
    val logoSmallUrl: String?,
    val logoBigUrl: String?,

    val creationTemplate: Long?,
    val myHomepage: Boolean,
)
```

### ProjectDetail

```kotlin
data class ProjectDetail(
    // ... all Project fields plus:
    val epicStatuses: List<EpicStatus>,
    val swimlanes: List<Swimlane>,
    val usStatuses: List<UserStoryStatus>,
    val usDuedates: List<DueDate>,
    val points: List<Points>,
    val taskStatuses: List<TaskStatus>,
    val taskDuedates: List<DueDate>,
    val issueStatuses: List<IssueStatus>,
    val issueTypes: List<IssueType>,
    val issueDuedates: List<DueDate>,
    val priorities: List<Priority>,
    val severities: List<Severity>,
    val epicCustomAttributes: List<CustomAttribute>,
    val userstoryCustomAttributes: List<CustomAttribute>,
    val taskCustomAttributes: List<CustomAttribute>,
    val issueCustomAttributes: List<CustomAttribute>,
    val roles: List<Role>,
    val members: List<MembershipInfo>,
    val totalMemberships: Int,
    val isOutOfOwnerLimits: Boolean,

    // Admin-only fields (null if not admin)
    val isPrivateExtraInfo: PrivacyExtraInfo?,
    val maxMemberships: Int?,
    val epicsCsvUuid: String?,
    val userstoriesCsvUuid: String?,
    val tasksCsvUuid: String?,
    val issuesCsvUuid: String?,
    val transferToken: String?,
    val milestones: List<MilestoneInfo>,
)

data class PrivacyExtraInfo(
    val canBeUpdated: Boolean,
    val reason: String?,
)
```

### Swimlane

```kotlin
data class Swimlane(
    val id: Long,
    val name: String,
    val order: Long,
    val project: Long,
    val statuses: List<SwimlaneStatus>,
)

data class SwimlaneStatus(
    val id: Long,
    val name: String,
    val slug: String,
    val order: Int,
    val isClosed: Boolean,
    val isArchived: Boolean,
    val color: String,
    val wipLimit: Int?,
    val swimlaneUserstoryStatusId: Long,
)

data class SwimlaneUserStoryStatus(
    val id: Long,
    val status: Long,
    val swimlane: Long,
    val wipLimit: Int?,
)
```

### Status Models

```kotlin
data class EpicStatus(
    val id: Long,
    val name: String,
    val slug: String,
    val order: Int,
    val isClosed: Boolean,
    val color: String,
    val project: Long,
)

data class UserStoryStatus(
    val id: Long,
    val name: String,
    val slug: String,
    val order: Int,
    val isClosed: Boolean,
    val isArchived: Boolean,
    val color: String,
    val wipLimit: Int?,
    val project: Long,
)

data class TaskStatus(
    val id: Long,
    val name: String,
    val slug: String,
    val order: Int,
    val isClosed: Boolean,
    val color: String,
    val project: Long,
)

data class IssueStatus(
    val id: Long,
    val name: String,
    val slug: String,
    val order: Int,
    val isClosed: Boolean,
    val color: String,
    val project: Long,
)

data class IssueType(
    val id: Long,
    val name: String,
    val order: Int,
    val color: String,
    val project: Long,
)

data class Priority(
    val id: Long,
    val name: String,
    val order: Int,
    val color: String,
    val project: Long,
)

data class Severity(
    val id: Long,
    val name: String,
    val order: Int,
    val color: String,
    val project: Long,
)

data class DueDate(
    val id: Long,
    val name: String,
    val order: Int,
    val byDefault: Boolean,
    val daysToDue: Int?,
    val color: String,
    val project: Long,
)
```

### Points

```kotlin
data class Points(
    val id: Long,
    val name: String,
    val order: Int,
    val value: Double?,  // Nullable, can be fractional (0.5, 1.5, etc.)
    val project: Long,
)
```

### CustomAttribute

```kotlin
data class CustomAttribute(
    val id: Long,
    val name: String,
    val description: String,
    val type: String,
    val order: Long,
    val project: Long,
    val extra: List<String>?,  // For dropdown type
    val createdDate: Instant,
    val modifiedDate: Instant,
)

enum class CustomAttributeType(val value: String) {
    TEXT("text"),
    MULTILINE("multiline"),
    RICHTEXT("richtext"),
    DATE("date"),
    URL("url"),
    DROPDOWN("dropdown"),
    CHECKBOX("checkbox"),
    NUMBER("number"),
}
```

### Enums

```kotlin
enum class BlockedCode(val value: String) {
    BLOCKED_BY_NONPAYMENT("blocked-by-nonpayment"),
    BLOCKED_BY_STAFF("blocked-by-staff"),
    BLOCKED_BY_OWNER_LEAVING("blocked-by-owner-leaving"),
    BLOCKED_BY_DELETING("blocked-by-deleting"),
}

enum class VideoconferenceType(val value: String) {
    WHEREBY("whereby-com"),
    JITSI("jitsi"),
    TALKY("talky"),
    CUSTOM("custom"),
}
```

### Request Models

```kotlin
data class CreateTagRequest(
    val tag: String,
    val color: String?,
)

data class EditTagRequest(
    val fromTag: String,
    val toTag: String?,
    val color: String?,
)

data class DeleteTagRequest(
    val tag: String,
)

data class MixTagsRequest(
    val fromTags: List<String>,
    val toTag: String,
)

data class CreateSwimlaneRequest(
    val project: Long,
    val name: String,
    val order: Long? = null,
)

data class BulkUpdateOrderRequest(
    val project: Long,
    val bulkSwimlanes: List<List<Long>>? = null,
    val bulkUserstoryStatuses: List<List<Long>>? = null,
    val bulkEpicStatuses: List<List<Long>>? = null,
    val bulkTaskStatuses: List<List<Long>>? = null,
    val bulkIssueStatuses: List<List<Long>>? = null,
    val bulkIssueTypes: List<List<Long>>? = null,
    val bulkPriorities: List<List<Long>>? = null,
    val bulkSeverities: List<List<Long>>? = null,
    val bulkPoints: List<List<Long>>? = null,
)

data class CsvUuidResponse(
    val uuid: String?,
)
```

---

## 10. Validation Rules

```kotlin
object ValidationRules {
    // Project
    const val PROJECT_NAME_MAX_LENGTH = 250
    const val PROJECT_SLUG_MAX_LENGTH = 250
    const val PROJECT_VIDEOCONF_EXTRA_MAX_LENGTH = 250
    // Note: description and looking_for_people_note have NO max length

    // Status names
    const val STATUS_NAME_MAX_LENGTH = 255
    const val STATUS_SLUG_MAX_LENGTH = 255
    const val STATUS_COLOR_MAX_LENGTH = 20

    // Points
    const val POINTS_NAME_MAX_LENGTH = 255

    // Custom Attributes
    const val CUSTOM_ATTR_NAME_MAX_LENGTH = 64
    const val CUSTOM_ATTR_TYPE_MAX_LENGTH = 16

    // Membership
    const val MEMBERSHIP_EMAIL_MAX_LENGTH = 255
    const val MEMBERSHIP_TOKEN_MAX_LENGTH = 60
    const val MEMBERSHIP_INVITATION_TEXT_MAX_LENGTH = 255
}
```

---

## 11. Error Handling

### Error Response Structure

```kotlin
data class TaigaError(
    @SerializedName("_error_message")
    val errorMessage: String,
    @SerializedName("_error_type")
    val errorType: String,
)

// Validation errors return Map<String, List<String>>
// Example: {"name": ["This field is required"]}
```

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 204 | No Content (successful delete/bulk update) |
| 400 | Bad Request, Validation Error |
| 401 | Unauthorized |
| 403 | Permission Denied |
| 404 | Not Found |
| 405 | Method Not Allowed |
| 415 | Unsupported Media Type |
| 429 | Too Many Requests |
| 451 | Blocked (project blocked) |
| 500 | Internal Server Error |

---

## 12. Permissions

### Anonymous Permissions

```kotlin
val ANON_PERMISSIONS = listOf(
    "view_project",
    "view_milestones",
    "view_epics",
    "view_us",
    "view_tasks",
    "view_issues",
    "view_wiki_pages",
    "view_wiki_links",
)
```

### Member Permissions

```kotlin
val MEMBER_PERMISSIONS = listOf(
    // View
    "view_project", "view_milestones", "view_epics", "view_us",
    "view_tasks", "view_issues", "view_wiki_pages", "view_wiki_links",
    // Milestone
    "add_milestone", "modify_milestone", "delete_milestone",
    // Epic
    "add_epic", "modify_epic", "comment_epic", "delete_epic",
    // User Story
    "add_us", "modify_us", "comment_us", "delete_us",
    // Task
    "add_task", "modify_task", "comment_task", "delete_task",
    // Issue
    "add_issue", "modify_issue", "comment_issue", "delete_issue",
    // Wiki Page
    "add_wiki_page", "modify_wiki_page", "comment_wiki_page", "delete_wiki_page",
    // Wiki Link
    "add_wiki_link", "modify_wiki_link", "delete_wiki_link",
)
```

### Admin Permissions

```kotlin
val ADMIN_PERMISSIONS = listOf(
    "modify_project",
    "delete_project",
    "add_member",
    "remove_member",
    "admin_project_values",
    "admin_roles",
)
```

---

## 13. Retrofit Interface

```kotlin
interface TaigaProjectSettingsApi {

    // ==================== Project ====================

    @GET("projects/{id}")
    suspend fun getProject(@Path("id") projectId: Long): ProjectDetail

    @GET("projects/by_slug")
    suspend fun getProjectBySlug(@Query("slug") slug: String): ProjectDetail

    @PATCH("projects/{id}")
    suspend fun updateProject(
        @Path("id") projectId: Long,
        @Body updates: Map<String, @JvmSuppressWildcards Any?>
    ): ProjectDetail

    @Multipart
    @POST("projects/{id}/change_logo")
    suspend fun changeLogo(
        @Path("id") projectId: Long,
        @Part logo: MultipartBody.Part
    ): ProjectDetail

    @POST("projects/{id}/remove_logo")
    suspend fun removeLogo(@Path("id") projectId: Long): ProjectDetail

    // ==================== Tags ====================

    @GET("projects/{id}/tags_colors")
    suspend fun getTagsColors(@Path("id") projectId: Long): Map<String, String?>

    @POST("projects/{id}/create_tag")
    suspend fun createTag(
        @Path("id") projectId: Long,
        @Body body: CreateTagRequest
    ): ResponseBody

    @POST("projects/{id}/edit_tag")
    suspend fun editTag(
        @Path("id") projectId: Long,
        @Body body: EditTagRequest
    ): ResponseBody

    @POST("projects/{id}/delete_tag")
    suspend fun deleteTag(
        @Path("id") projectId: Long,
        @Body body: DeleteTagRequest
    ): ResponseBody

    @POST("projects/{id}/mix_tags")
    suspend fun mixTags(
        @Path("id") projectId: Long,
        @Body body: MixTagsRequest
    ): ResponseBody

    // ==================== Swimlanes ====================

    @GET("swimlanes")
    suspend fun getSwimlanes(@Query("project") projectId: Long): List<Swimlane>

    @POST("swimlanes")
    suspend fun createSwimlane(@Body body: CreateSwimlaneRequest): Swimlane

    @PATCH("swimlanes/{id}")
    suspend fun updateSwimlane(
        @Path("id") swimlaneId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Swimlane

    @DELETE("swimlanes/{id}")
    suspend fun deleteSwimlane(
        @Path("id") swimlaneId: Long,
        @Query("moveTo") moveTo: Long? = null
    ): ResponseBody

    @POST("swimlanes/bulk_update_order")
    suspend fun bulkUpdateSwimlaneOrder(@Body body: BulkUpdateOrderRequest): ResponseBody

    @PATCH("swimlane-userstory-statuses/{id}")
    suspend fun updateSwimlaneWipLimit(
        @Path("id") swimlaneStatusId: Long,
        @Body body: Map<String, Int?>
    ): SwimlaneUserStoryStatus

    // ==================== Statuses ====================

    @GET("userstory-statuses")
    suspend fun getUserStoryStatuses(@Query("project") projectId: Long): List<UserStoryStatus>

    @POST("userstory-statuses")
    suspend fun createUserStoryStatus(@Body body: Map<String, @JvmSuppressWildcards Any?>): UserStoryStatus

    @PATCH("userstory-statuses/{id}")
    suspend fun updateUserStoryStatus(
        @Path("id") statusId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): UserStoryStatus

    @DELETE("userstory-statuses/{id}")
    suspend fun deleteUserStoryStatus(
        @Path("id") statusId: Long,
        @Query("moveTo") moveTo: Long
    ): ResponseBody

    @POST("userstory-statuses/bulk_update_order")
    suspend fun bulkUpdateUserStoryStatusOrder(@Body body: BulkUpdateOrderRequest): ResponseBody

    // Similar endpoints for: epic-statuses, task-statuses, issue-statuses,
    // issue-types, priorities, severities, points

    // ==================== Custom Attributes ====================

    @GET("userstory-custom-attributes")
    suspend fun getUSCustomAttributes(@Query("project") projectId: Long): List<CustomAttribute>

    @POST("userstory-custom-attributes")
    suspend fun createUSCustomAttribute(@Body body: Map<String, @JvmSuppressWildcards Any?>): CustomAttribute

    @PATCH("userstory-custom-attributes/{id}")
    suspend fun updateUSCustomAttribute(
        @Path("id") attrId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): CustomAttribute

    @DELETE("userstory-custom-attributes/{id}")
    suspend fun deleteUSCustomAttribute(@Path("id") attrId: Long): ResponseBody

    // Similar endpoints for: epic-custom-attributes, task-custom-attributes, issue-custom-attributes

    // ==================== CSV ====================

    @POST("projects/{id}/regenerate_userstories_csv_uuid")
    suspend fun regenerateUSCsvUuid(@Path("id") projectId: Long): CsvUuidResponse

    @POST("projects/{id}/delete_userstories_csv_uuid")
    suspend fun deleteUSCsvUuid(@Path("id") projectId: Long): CsvUuidResponse

    // Similar endpoints for: epics, tasks, issues
}
```

---

## Type Mapping Reference

| Python Type | Kotlin Type |
|-------------|-------------|
| `IntegerField` | `Int` |
| `PositiveIntegerField` | `Int` (>=0) |
| `BigIntegerField` | `Long` |
| `FloatField` | `Double` |
| `CharField` | `String` |
| `TextField` | `String` |
| `BooleanField` | `Boolean` |
| `DateTimeField` | `Instant` or `String` (ISO 8601) |
| `ArrayField(TextField)` | `List<String>` |
| `JSONField` | `JsonElement` or specific type |
| `ForeignKey` | `Long` (ID reference) |
| Nullable fields | Add `?` suffix |

# Taiga REST API Specification for Test Data Population

This document describes the Taiga backend REST API endpoints needed to populate a database with test data. All endpoints are under the base URL `{BASE_URL}/api/v1/`. All request/response bodies are JSON. Kotlin type mappings are provided for each field.

## Table of Contents

1. [Authentication](#1-authentication)
2. [Projects](#2-projects)
3. [Milestones (Sprints)](#3-milestones-sprints)
4. [Epics](#4-epics)
5. [User Stories](#5-user-stories)
6. [Tasks](#6-tasks)
7. [Issues](#7-issues)
8. [Wiki Pages](#8-wiki-pages)
9. [Wiki Links](#9-wiki-links)
10. [Execution Order](#10-execution-order)
11. [Kotlin Data Models](#11-kotlin-data-models)

---

## General Notes

- **Authentication:** After login, include the token in all subsequent requests:
  ```
  Authorization: Bearer {auth_token}
  ```
- **Content-Type:** All POST/PUT/PATCH requests must include:
  ```
  Content-Type: application/json
  ```
- **OCC (Optimistic Concurrency Control):** When updating entities that have a `version` field (user stories, tasks, issues, epics, wiki pages), you must include the current `version` value in the PATCH/PUT body. The version increments on every update.
- **Date format:** Dates use `YYYY-MM-DD`, datetimes use ISO 8601 (`YYYY-MM-DDTHH:MM:SSZ`).
- **Foreign keys:** All relationship fields accept integer IDs.
- **Auto-defaults:** Status fields (issue status, US status, task status, epic status) auto-default to the project's configured defaults if omitted during creation.

---

## 1. Authentication

### Login

```
POST /api/v1/auth
```

**Request Body:**

| Field      | Kotlin Type | Required | Description                          |
|------------|-------------|----------|--------------------------------------|
| `type`     | String      | Yes      | Must be `"normal"` for password auth |
| `username` | String      | Yes      | Username or email address            |
| `password` | String      | Yes      | User password                        |

**Example Request:**
```json
{
  "type": "normal",
  "username": "admin",
  "password": "123123"
}
```

**Response (200):**

| Field        | Kotlin Type | Description                               |
|--------------|-------------|-------------------------------------------|
| `id`         | Int         | User ID                                   |
| `username`   | String      | Username                                  |
| `email`      | String      | Email address                             |
| `full_name`  | String      | Display name                              |
| `auth_token` | String      | **JWT access token for Bearer auth**      |
| `refresh`    | String      | JWT refresh token                         |
| `photo`      | String?     | Avatar URL or null                        |
| `big_photo`  | String?     | Large avatar URL or null                  |
| `is_active`  | Boolean     | Account active flag                       |
| `roles`      | List\<String\> | Role names across projects             |

**Key:** Save `auth_token` from the response. Use it as `Authorization: Bearer {auth_token}` in all subsequent requests.

---

## 2. Projects

### List Projects

```
GET /api/v1/projects
```

Returns a paginated list of projects the authenticated user has access to.

**Response (200):** Array of project objects.

### Create Project

```
POST /api/v1/projects
```

**Request Body:**

| Field                    | Kotlin Type    | Required | Default | Description                        |
|--------------------------|----------------|----------|---------|------------------------------------|
| `name`                   | String         | **Yes**  | -       | Project name (max 250 chars)       |
| `description`            | String         | **Yes**  | -       | Project description                |
| `is_private`             | Boolean        | No       | `true`  | Private project flag               |
| `is_backlog_activated`   | Boolean        | No       | `true`  | Enable Scrum backlog               |
| `is_kanban_activated`    | Boolean        | No       | `false` | Enable Kanban board                |
| `is_issues_activated`    | Boolean        | No       | `true`  | Enable Issues module               |
| `is_wiki_activated`      | Boolean        | No       | `true`  | Enable Wiki module                 |
| `is_epics_activated`     | Boolean        | No       | `false` | Enable Epics module                |

**Example Request:**
```json
{
  "name": "Test Project",
  "description": "A project for testing",
  "is_private": false,
  "is_backlog_activated": true,
  "is_kanban_activated": true,
  "is_issues_activated": true,
  "is_wiki_activated": true,
  "is_epics_activated": true
}
```

**Response (201):** Full project object.

**Key Response Fields:**

| Field                      | Kotlin Type | Description                            |
|----------------------------|-------------|----------------------------------------|
| `id`                       | Int         | Project ID (use as FK everywhere)      |
| `name`                     | String      | Project name                           |
| `slug`                     | String      | URL slug (auto-generated)              |
| `default_epic_status`      | Int?        | Default EpicStatus ID                  |
| `default_us_status`        | Int?        | Default UserStoryStatus ID             |
| `default_task_status`      | Int?        | Default TaskStatus ID                  |
| `default_issue_status`     | Int?        | Default IssueStatus ID                 |
| `default_issue_type`       | Int?        | Default IssueType ID                   |
| `default_priority`         | Int?        | Default Priority ID                    |
| `default_severity`         | Int?        | Default Severity ID                    |
| `default_points`           | Int?        | Default Points ID                      |
| `members`                  | List\<Int\> | Member user IDs                        |

### Get Project Detail

```
GET /api/v1/projects/{id}
```

Returns the full project detail including all configured statuses, types, priorities, severities, and points. **You need this response to obtain the IDs for status/type/priority/severity fields** when creating issues, user stories, etc.

**Key nested arrays in detail response:**

| Array Field        | Object Fields                            | Purpose                          |
|--------------------|------------------------------------------|----------------------------------|
| `epic_statuses`    | `id`, `name`, `slug`, `is_closed`, `color`, `order` | Available epic statuses  |
| `us_statuses`      | `id`, `name`, `slug`, `is_closed`, `color`, `order` | Available US statuses    |
| `task_statuses`    | `id`, `name`, `slug`, `is_closed`, `color`, `order` | Available task statuses  |
| `issue_statuses`   | `id`, `name`, `slug`, `is_closed`, `color`, `order` | Available issue statuses |
| `issue_types`      | `id`, `name`, `color`, `order`           | Available issue types            |
| `priorities`       | `id`, `name`, `color`, `order`           | Available priorities             |
| `severities`       | `id`, `name`, `color`, `order`           | Available severities             |
| `points`           | `id`, `name`, `value`, `order`           | Available story point values     |
| `roles`            | `id`, `name`, `slug`, `permissions`, `order`, `computable` | Project roles |
| `members`          | `id`, `user`, `role`, `role_name`, `is_admin` | Project memberships       |

---

## 3. Milestones (Sprints)

### Create Milestone

```
POST /api/v1/milestones
```

**Request Body:**

| Field              | Kotlin Type | Required | Default      | Description                             |
|--------------------|-------------|----------|--------------|-----------------------------------------|
| `project`          | Int         | **Yes**  | -            | Project ID                              |
| `name`             | String      | **Yes**  | -            | Sprint name (max 200 chars, unique per project) |
| `estimated_start`  | String      | **Yes**  | -            | Start date (`YYYY-MM-DD`)               |
| `estimated_finish`  | String      | **Yes**  | -            | End date (`YYYY-MM-DD`)                 |
| `disponibility`    | Double      | No       | `0.0`        | Team availability (float)               |
| `order`            | Int         | No       | `1`          | Display order                           |
| `closed`           | Boolean     | No       | `false`      | Whether sprint is closed                |

**Validation:** `estimated_start` must be before `estimated_finish`.

**Example Request:**
```json
{
  "project": 1,
  "name": "Sprint 1",
  "estimated_start": "2026-02-10",
  "estimated_finish": "2026-02-24",
  "disponibility": 40.0,
  "order": 1
}
```

**Response (201):**

| Field              | Kotlin Type | Description                    |
|--------------------|-------------|--------------------------------|
| `id`               | Int         | Milestone ID                   |
| `name`             | String      | Sprint name                    |
| `slug`             | String      | Auto-generated slug            |
| `project`          | Int         | Project ID                     |
| `owner`            | Int         | Creator user ID                |
| `estimated_start`  | String      | Start date                     |
| `estimated_finish`  | String      | End date                       |
| `closed`           | Boolean     | Closed flag                    |
| `created_date`     | String      | ISO datetime                   |
| `modified_date`    | String      | ISO datetime                   |
| `user_stories`     | List\<Any\> | Nested user stories            |
| `total_points`     | Double?     | Computed total points          |
| `closed_points`    | Double?     | Computed closed points         |

### List Milestones

```
GET /api/v1/milestones?project={project_id}
```

---

## 4. Epics

### Create Epic

```
POST /api/v1/epics
```

**Request Body:**

| Field                | Kotlin Type    | Required | Default            | Description                           |
|----------------------|----------------|----------|--------------------|---------------------------------------|
| `project`            | Int            | **Yes**  | -                  | Project ID                            |
| `subject`            | String         | **Yes**  | -                  | Epic title                            |
| `description`        | String         | No       | `""`               | Markdown description                  |
| `status`             | Int            | No       | Project default    | EpicStatus ID                         |
| `assigned_to`        | Int?           | No       | `null`             | Assigned user ID (must be member)     |
| `color`              | String         | No       | Random hex         | Hex color code (e.g. `"#FF6B6B"`)    |
| `client_requirement` | Boolean        | No       | `false`            | Client requirement flag               |
| `team_requirement`   | Boolean        | No       | `false`            | Team requirement flag                 |
| `tags`               | List\<String\> | No       | `[]`               | Tag names                             |
| `watchers`           | List\<Int\>    | No       | `[]`               | Watcher user IDs (must be members)    |

**Example Request:**
```json
{
  "project": 1,
  "subject": "User Authentication System",
  "description": "Implement complete auth flow",
  "color": "#FF6B6B"
}
```

**Response (201):**

| Field                  | Kotlin Type    | Description                       |
|------------------------|----------------|-----------------------------------|
| `id`                   | Int            | Epic ID                           |
| `ref`                  | Int            | Reference number (per project)    |
| `project`              | Int            | Project ID                        |
| `subject`              | String         | Epic title                        |
| `description`          | String         | Raw markdown                      |
| `description_html`     | String         | Rendered HTML                     |
| `status`               | Int            | EpicStatus ID                     |
| `assigned_to`          | Int?           | Assigned user ID                  |
| `owner`                | Int            | Creator user ID                   |
| `color`                | String         | Hex color                         |
| `epics_order`          | Long           | Ordering value                    |
| `is_closed`            | Boolean        | Derived from status               |
| `is_blocked`           | Boolean        | Blocked flag                      |
| `blocked_note`         | String         | Block reason                      |
| `version`              | Int            | OCC version                       |
| `created_date`         | String         | ISO datetime                      |
| `modified_date`        | String         | ISO datetime                      |
| `client_requirement`   | Boolean        | Flag                              |
| `team_requirement`     | Boolean        | Flag                              |
| `tags`                 | List\<String\> | Tags                              |
| `watchers`             | List\<Int\>    | Watcher IDs                       |

### Link User Story to Epic

```
POST /api/v1/epics/{epic_id}/related_userstories
```

| Field        | Kotlin Type | Required | Description          |
|--------------|-------------|----------|----------------------|
| `user_story` | Int         | **Yes**  | User Story ID        |
| `order`      | Long        | No       | Ordering value       |

### List Epics

```
GET /api/v1/epics?project={project_id}
```

---

## 5. User Stories

### Create User Story

```
POST /api/v1/userstories
```

**Request Body:**

| Field                | Kotlin Type         | Required | Default            | Description                              |
|----------------------|---------------------|----------|--------------------|------------------------------------------|
| `project`            | Int                 | **Yes**  | -                  | Project ID                               |
| `subject`            | String              | **Yes**  | -                  | User story title                         |
| `description`        | String              | No       | `""`               | Markdown description                     |
| `status`             | Int                 | No       | Project default    | UserStoryStatus ID                       |
| `milestone`          | Int?                | No       | `null`             | Milestone/Sprint ID (must be same project) |
| `assigned_to`        | Int?                | No       | `null`             | Assigned user ID (must be member)        |
| `assigned_users`     | List\<Int\>         | No       | `[]`               | Multiple assigned user IDs               |
| `client_requirement` | Boolean             | No       | `false`            | Client requirement flag                  |
| `team_requirement`   | Boolean             | No       | `false`            | Team requirement flag                    |
| `is_blocked`         | Boolean             | No       | `false`            | Blocked flag                             |
| `blocked_note`       | String              | No       | `""`               | Block reason (cleared if not blocked)    |
| `backlog_order`      | Long                | No       | Timestamp          | Backlog ordering                         |
| `sprint_order`       | Long                | No       | Timestamp          | Sprint ordering                          |
| `swimlane`           | Int?                | No       | `null`             | Swimlane ID                              |
| `due_date`           | String?             | No       | `null`             | Due date (`YYYY-MM-DD`)                  |
| `due_date_reason`    | String              | No       | `""`               | Due date reason                          |
| `tags`               | List\<String\>      | No       | `[]`               | Tag names                                |
| `watchers`           | List\<Int\>         | No       | `[]`               | Watcher user IDs (must be members)       |
| `points`             | Map\<String, Int\>  | No       | Project defaults   | Map of `"roleId"` to `pointsId`         |

**About `points`:** This is a map where keys are Role IDs (as strings) and values are Points IDs. You get these IDs from the project detail response. Example: `{"1": 3, "2": 3}` means role 1 and role 2 both get points value with ID 3.

**Example Request:**
```json
{
  "project": 1,
  "subject": "As a user I want to login",
  "description": "Login with username and password",
  "milestone": 1,
  "points": {"1": 3, "2": 3}
}
```

**Response (201):**

| Field                  | Kotlin Type         | Description                          |
|------------------------|---------------------|--------------------------------------|
| `id`                   | Int                 | User Story ID                        |
| `ref`                  | Int                 | Reference number (per project)       |
| `project`              | Int                 | Project ID                           |
| `subject`              | String              | Title                                |
| `description`          | String              | Raw markdown                         |
| `description_html`     | String              | Rendered HTML                        |
| `status`               | Int                 | UserStoryStatus ID                   |
| `milestone`            | Int?                | Milestone ID                         |
| `assigned_to`          | Int?                | Assigned user ID                     |
| `assigned_users`       | List\<Int\>         | Multiple assigned users              |
| `owner`                | Int                 | Creator user ID                      |
| `points`               | Map\<String, Int\>  | Role ID -> Points ID mapping         |
| `is_closed`            | Boolean             | Derived from status                  |
| `is_blocked`           | Boolean             | Blocked flag                         |
| `blocked_note`         | String              | Block reason                         |
| `version`              | Int                 | OCC version                          |
| `backlog_order`        | Long                | Ordering value                       |
| `sprint_order`         | Long                | Ordering value                       |
| `kanban_order`         | Long                | Ordering value                       |
| `created_date`         | String              | ISO datetime                         |
| `modified_date`        | String              | ISO datetime                         |
| `finish_date`          | String?             | ISO datetime or null                 |
| `total_points`         | Double?             | Computed sum of points               |
| `client_requirement`   | Boolean             | Flag                                 |
| `team_requirement`     | Boolean             | Flag                                 |
| `tags`                 | List\<Any\>         | Tags with colors                     |
| `watchers`             | List\<Int\>         | Watcher IDs                          |

### List User Stories

```
GET /api/v1/userstories?project={project_id}
```

---

## 6. Tasks

### Create Task

```
POST /api/v1/tasks
```

**Request Body:**

| Field              | Kotlin Type    | Required | Default         | Description                                  |
|--------------------|----------------|----------|-----------------|----------------------------------------------|
| `project`          | Int            | **Yes**  | -               | Project ID                                   |
| `subject`          | String         | **Yes**  | -               | Task title                                   |
| `description`      | String         | No       | `""`            | Markdown description                         |
| `status`           | Int            | No       | Project default | TaskStatus ID                                |
| `user_story`       | Int?           | No       | `null`          | Parent UserStory ID (same project)           |
| `milestone`        | Int?           | No       | `null`          | Milestone ID (auto-set from US if US is set) |
| `assigned_to`      | Int?           | No       | `null`          | Assigned user ID (must be member)            |
| `is_iocaine`       | Boolean        | No       | `false`         | Iocaine flag                                 |
| `is_blocked`       | Boolean        | No       | `false`         | Blocked flag                                 |
| `blocked_note`     | String         | No       | `""`            | Block reason (cleared if not blocked)        |
| `due_date`         | String?        | No       | `null`          | Due date (`YYYY-MM-DD`)                      |
| `due_date_reason`  | String         | No       | `""`            | Due date reason                              |
| `tags`             | List\<String\> | No       | `[]`            | Tag names                                    |
| `watchers`         | List\<Int\>    | No       | `[]`            | Watcher user IDs (must be members)           |
| `taskboard_order`  | Long           | No       | Timestamp       | Taskboard ordering                           |
| `us_order`         | Long           | No       | Timestamp       | Order within user story                      |

**Example Request:**
```json
{
  "project": 1,
  "subject": "Implement login form",
  "user_story": 1,
  "status": 1
}
```

**Response (201):**

| Field                  | Kotlin Type    | Description                    |
|------------------------|----------------|--------------------------------|
| `id`                   | Int            | Task ID                        |
| `ref`                  | Int            | Reference number (per project) |
| `project`              | Int            | Project ID                     |
| `subject`              | String         | Title                          |
| `description`          | String         | Raw markdown                   |
| `description_html`     | String         | Rendered HTML                  |
| `status`               | Int            | TaskStatus ID                  |
| `user_story`           | Int?           | Parent US ID                   |
| `milestone`            | Int?           | Milestone ID                   |
| `assigned_to`          | Int?           | Assigned user ID               |
| `owner`                | Int            | Creator user ID                |
| `is_iocaine`           | Boolean        | Flag                           |
| `is_closed`            | Boolean        | Derived from status            |
| `is_blocked`           | Boolean        | Blocked flag                   |
| `blocked_note`         | String         | Block reason                   |
| `version`              | Int            | OCC version                    |
| `taskboard_order`      | Long           | Ordering value                 |
| `us_order`             | Long           | Ordering value                 |
| `created_date`         | String         | ISO datetime                   |
| `modified_date`        | String         | ISO datetime                   |
| `finished_date`        | String?        | ISO datetime or null           |
| `tags`                 | List\<Any\>    | Tags                           |
| `watchers`             | List\<Int\>    | Watcher IDs                    |

### List Tasks

```
GET /api/v1/tasks?project={project_id}
```

---

## 7. Issues

### Create Issue

```
POST /api/v1/issues
```

**Request Body:**

| Field              | Kotlin Type    | Required | Default         | Description                              |
|--------------------|----------------|----------|-----------------|------------------------------------------|
| `project`          | Int            | **Yes**  | -               | Project ID                               |
| `subject`          | String         | **Yes**  | -               | Issue title                              |
| `description`      | String         | No       | `""`            | Markdown description                     |
| `status`           | Int            | No       | Project default | IssueStatus ID                           |
| `type`             | Int            | No       | Project default | IssueType ID                             |
| `severity`         | Int            | No       | Project default | Severity ID                              |
| `priority`         | Int            | No       | Project default | Priority ID                              |
| `milestone`        | Int?           | No       | `null`          | Milestone ID (must be same project)      |
| `assigned_to`      | Int?           | No       | `null`          | Assigned user ID (must be member)        |
| `is_blocked`       | Boolean        | No       | `false`         | Blocked flag                             |
| `blocked_note`     | String         | No       | `""`            | Block reason (cleared if not blocked)    |
| `due_date`         | String?        | No       | `null`          | Due date (`YYYY-MM-DD`)                  |
| `due_date_reason`  | String         | No       | `""`            | Due date reason                          |
| `tags`             | List\<String\> | No       | `[]`            | Tag names                                |
| `watchers`         | List\<Int\>    | No       | `[]`            | Watcher user IDs (must be members)       |

**Example Request:**
```json
{
  "project": 1,
  "subject": "Login button not working on mobile",
  "type": 1,
  "severity": 3,
  "priority": 2
}
```

**Response (201):**

| Field                  | Kotlin Type    | Description                    |
|------------------------|----------------|--------------------------------|
| `id`                   | Int            | Issue ID                       |
| `ref`                  | Int            | Reference number (per project) |
| `project`              | Int            | Project ID                     |
| `subject`              | String         | Title                          |
| `description`          | String         | Raw markdown                   |
| `description_html`     | String         | Rendered HTML                  |
| `status`               | Int            | IssueStatus ID                 |
| `type`                 | Int            | IssueType ID                   |
| `severity`             | Int            | Severity ID                    |
| `priority`             | Int            | Priority ID                    |
| `milestone`            | Int?           | Milestone ID                   |
| `assigned_to`          | Int?           | Assigned user ID               |
| `owner`                | Int            | Creator user ID                |
| `is_closed`            | Boolean        | Derived from status            |
| `is_blocked`           | Boolean        | Blocked flag                   |
| `blocked_note`         | String         | Block reason                   |
| `version`              | Int            | OCC version                    |
| `created_date`         | String         | ISO datetime                   |
| `modified_date`        | String         | ISO datetime                   |
| `finished_date`        | String?        | ISO datetime or null           |
| `tags`                 | List\<Any\>    | Tags                           |
| `watchers`             | List\<Int\>    | Watcher IDs                    |

### List Issues

```
GET /api/v1/issues?project={project_id}
```

---

## 8. Wiki Pages

### Create Wiki Page

```
POST /api/v1/wiki
```

**Request Body:**

| Field      | Kotlin Type    | Required | Default | Description                                   |
|------------|----------------|----------|---------|-----------------------------------------------|
| `project`  | Int            | **Yes**  | -       | Project ID                                    |
| `slug`     | String         | **Yes**  | -       | URL slug (max 500, unique per project)        |
| `content`  | String         | No       | `""`    | Markdown content                              |
| `watchers` | List\<Int\>    | No       | `[]`    | Watcher user IDs                              |

**Example Request:**
```json
{
  "project": 1,
  "slug": "getting-started",
  "content": "# Getting Started\n\nWelcome to the project wiki."
}
```

**Response (201):**

| Field              | Kotlin Type | Description                   |
|--------------------|-------------|-------------------------------|
| `id`               | Int         | Wiki Page ID                  |
| `project`          | Int         | Project ID                    |
| `slug`             | String      | URL slug                      |
| `content`          | String      | Raw markdown                  |
| `html`             | String      | Rendered HTML                 |
| `owner`            | Int         | Creator user ID               |
| `last_modifier`    | Int         | Last editor user ID           |
| `created_date`     | String      | ISO datetime                  |
| `modified_date`    | String      | ISO datetime                  |
| `version`          | Int         | OCC version                   |
| `editions`         | Int         | Number of edits               |
| `is_watcher`       | Boolean     | Whether current user watches  |
| `total_watchers`   | Int         | Count of watchers             |

### List Wiki Pages

```
GET /api/v1/wiki?project={project_id}
```

---

## 9. Wiki Links

### Create Wiki Link

```
POST /api/v1/wiki-links
```

**Request Body:**

| Field     | Kotlin Type | Required | Default   | Description                            |
|-----------|-------------|----------|-----------|----------------------------------------|
| `project` | Int         | **Yes**  | -         | Project ID                             |
| `title`   | String      | **Yes**  | -         | Display title (max 500 chars)          |
| `href`    | String      | No       | From title | URL slug (auto-generated from title)  |
| `order`   | Long        | No       | Timestamp | Sort order                             |

**Note:** If the wiki page for the `href` slug doesn't exist, and the user has `add_wiki_page` permission, it will be auto-created.

**Example Request:**
```json
{
  "project": 1,
  "title": "Getting Started",
  "href": "getting-started",
  "order": 1
}
```

**Response (201):**

| Field     | Kotlin Type | Description       |
|-----------|-------------|-------------------|
| `id`      | Int         | Wiki Link ID      |
| `project` | Int         | Project ID        |
| `title`   | String      | Display title     |
| `href`    | String      | URL slug          |
| `order`   | Long        | Sort order        |

### List Wiki Links

```
GET /api/v1/wiki-links?project={project_id}
```

---

## 10. Execution Order

To populate test data, execute requests in this order (respecting FK dependencies):

```
1. POST /api/v1/auth                    -> get auth_token
2. POST /api/v1/projects                -> get project_id
3. GET  /api/v1/projects/{id}           -> get status/type/priority/severity/points/role IDs
4. POST /api/v1/milestones              -> get milestone_id(s)
5. POST /api/v1/epics                   -> get epic_id(s)
6. POST /api/v1/userstories             -> get userstory_id(s), reference milestone_id
7. POST /api/v1/epics/{id}/related_userstories  -> link USs to epics
8. POST /api/v1/tasks                   -> reference userstory_id, milestone_id
9. POST /api/v1/issues                  -> reference milestone_id, use type/severity/priority IDs
10. POST /api/v1/wiki                   -> create wiki pages
11. POST /api/v1/wiki-links             -> create wiki sidebar links
```

---

## 11. Kotlin Data Models

Below are suggested Kotlin data classes for request and response bodies.

### Auth

```kotlin
// Request
data class LoginRequest(
    val type: String = "normal",
    val username: String,
    val password: String
)

// Response
data class LoginResponse(
    val id: Int,
    val username: String,
    val email: String,
    val full_name: String,
    val auth_token: String,
    val refresh: String,
    val photo: String?,
    val big_photo: String?,
    val is_active: Boolean,
    val roles: List<String>
)
```

### Project

```kotlin
// Request
data class CreateProjectRequest(
    val name: String,
    val description: String,
    val is_private: Boolean = false,
    val is_backlog_activated: Boolean = true,
    val is_kanban_activated: Boolean = false,
    val is_issues_activated: Boolean = true,
    val is_wiki_activated: Boolean = true,
    val is_epics_activated: Boolean = false
)

// Response (key fields)
data class ProjectResponse(
    val id: Int,
    val name: String,
    val slug: String,
    val default_epic_status: Int?,
    val default_us_status: Int?,
    val default_task_status: Int?,
    val default_issue_status: Int?,
    val default_issue_type: Int?,
    val default_priority: Int?,
    val default_severity: Int?,
    val default_points: Int?,
    val members: List<Int>
)

// Detail includes nested config arrays
data class ProjectDetailResponse(
    val id: Int,
    val name: String,
    val slug: String,
    val epic_statuses: List<StatusItem>,
    val us_statuses: List<StatusItem>,
    val task_statuses: List<StatusItem>,
    val issue_statuses: List<StatusItem>,
    val issue_types: List<TypeItem>,
    val priorities: List<TypeItem>,
    val severities: List<TypeItem>,
    val points: List<PointItem>,
    val roles: List<RoleItem>,
    val default_epic_status: Int?,
    val default_us_status: Int?,
    val default_task_status: Int?,
    val default_issue_status: Int?,
    val default_issue_type: Int?,
    val default_priority: Int?,
    val default_severity: Int?,
    val default_points: Int?
)

data class StatusItem(
    val id: Int,
    val name: String,
    val slug: String,
    val is_closed: Boolean,
    val color: String,
    val order: Int
)

data class TypeItem(
    val id: Int,
    val name: String,
    val color: String,
    val order: Int
)

data class PointItem(
    val id: Int,
    val name: String,
    val value: Double?,
    val order: Int
)

data class RoleItem(
    val id: Int,
    val name: String,
    val slug: String,
    val order: Int,
    val computable: Boolean,
    val permissions: List<String>
)
```

### Milestone (Sprint)

```kotlin
// Request
data class CreateMilestoneRequest(
    val project: Int,
    val name: String,
    val estimated_start: String,  // "YYYY-MM-DD"
    val estimated_finish: String, // "YYYY-MM-DD"
    val disponibility: Double = 0.0,
    val order: Int = 1
)

// Response
data class MilestoneResponse(
    val id: Int,
    val name: String,
    val slug: String,
    val project: Int,
    val owner: Int,
    val estimated_start: String,
    val estimated_finish: String,
    val closed: Boolean,
    val disponibility: Double?,
    val order: Int,
    val created_date: String,
    val modified_date: String,
    val total_points: Double?,
    val closed_points: Double?
)
```

### Epic

```kotlin
// Request
data class CreateEpicRequest(
    val project: Int,
    val subject: String,
    val description: String = "",
    val status: Int? = null,
    val assigned_to: Int? = null,
    val color: String? = null,
    val tags: List<String> = emptyList(),
    val watchers: List<Int> = emptyList()
)

// Response
data class EpicResponse(
    val id: Int,
    val ref: Int,
    val project: Int,
    val subject: String,
    val description: String,
    val description_html: String,
    val status: Int,
    val assigned_to: Int?,
    val owner: Int,
    val color: String,
    val epics_order: Long,
    val is_closed: Boolean,
    val is_blocked: Boolean,
    val blocked_note: String,
    val version: Int,
    val created_date: String,
    val modified_date: String,
    val tags: List<String>,
    val watchers: List<Int>
)

// Link US to Epic
data class EpicRelatedUserStoryRequest(
    val user_story: Int,
    val order: Long? = null
)
```

### User Story

```kotlin
// Request
data class CreateUserStoryRequest(
    val project: Int,
    val subject: String,
    val description: String = "",
    val status: Int? = null,
    val milestone: Int? = null,
    val assigned_to: Int? = null,
    val assigned_users: List<Int> = emptyList(),
    val client_requirement: Boolean = false,
    val team_requirement: Boolean = false,
    val is_blocked: Boolean = false,
    val blocked_note: String = "",
    val due_date: String? = null,       // "YYYY-MM-DD"
    val due_date_reason: String = "",
    val tags: List<String> = emptyList(),
    val watchers: List<Int> = emptyList(),
    val points: Map<String, Int>? = null // "roleId" -> pointsId
)

// Response
data class UserStoryResponse(
    val id: Int,
    val ref: Int,
    val project: Int,
    val subject: String,
    val description: String,
    val status: Int,
    val milestone: Int?,
    val assigned_to: Int?,
    val assigned_users: List<Int>,
    val owner: Int,
    val points: Map<String, Int>,
    val is_closed: Boolean,
    val is_blocked: Boolean,
    val blocked_note: String,
    val version: Int,
    val backlog_order: Long,
    val sprint_order: Long,
    val kanban_order: Long,
    val created_date: String,
    val modified_date: String,
    val finish_date: String?,
    val total_points: Double?,
    val tags: List<Any>,
    val watchers: List<Int>
)
```

### Task

```kotlin
// Request
data class CreateTaskRequest(
    val project: Int,
    val subject: String,
    val description: String = "",
    val status: Int? = null,
    val user_story: Int? = null,
    val milestone: Int? = null,
    val assigned_to: Int? = null,
    val is_iocaine: Boolean = false,
    val is_blocked: Boolean = false,
    val blocked_note: String = "",
    val due_date: String? = null,       // "YYYY-MM-DD"
    val due_date_reason: String = "",
    val tags: List<String> = emptyList(),
    val watchers: List<Int> = emptyList()
)

// Response
data class TaskResponse(
    val id: Int,
    val ref: Int,
    val project: Int,
    val subject: String,
    val description: String,
    val status: Int,
    val user_story: Int?,
    val milestone: Int?,
    val assigned_to: Int?,
    val owner: Int,
    val is_iocaine: Boolean,
    val is_closed: Boolean,
    val is_blocked: Boolean,
    val blocked_note: String,
    val version: Int,
    val taskboard_order: Long,
    val us_order: Long,
    val created_date: String,
    val modified_date: String,
    val finished_date: String?,
    val tags: List<Any>,
    val watchers: List<Int>
)
```

### Issue

```kotlin
// Request
data class CreateIssueRequest(
    val project: Int,
    val subject: String,
    val description: String = "",
    val status: Int? = null,
    val type: Int? = null,
    val severity: Int? = null,
    val priority: Int? = null,
    val milestone: Int? = null,
    val assigned_to: Int? = null,
    val is_blocked: Boolean = false,
    val blocked_note: String = "",
    val due_date: String? = null,       // "YYYY-MM-DD"
    val due_date_reason: String = "",
    val tags: List<String> = emptyList(),
    val watchers: List<Int> = emptyList()
)

// Response
data class IssueResponse(
    val id: Int,
    val ref: Int,
    val project: Int,
    val subject: String,
    val description: String,
    val status: Int,
    val type: Int,
    val severity: Int,
    val priority: Int,
    val milestone: Int?,
    val assigned_to: Int?,
    val owner: Int,
    val is_closed: Boolean,
    val is_blocked: Boolean,
    val blocked_note: String,
    val version: Int,
    val created_date: String,
    val modified_date: String,
    val finished_date: String?,
    val tags: List<Any>,
    val watchers: List<Int>
)
```

### Wiki

```kotlin
// Wiki Page Request
data class CreateWikiPageRequest(
    val project: Int,
    val slug: String,
    val content: String = "",
    val watchers: List<Int> = emptyList()
)

// Wiki Page Response
data class WikiPageResponse(
    val id: Int,
    val project: Int,
    val slug: String,
    val content: String,
    val html: String,
    val owner: Int,
    val last_modifier: Int,
    val created_date: String,
    val modified_date: String,
    val version: Int,
    val editions: Int,
    val is_watcher: Boolean,
    val total_watchers: Int
)

// Wiki Link Request
data class CreateWikiLinkRequest(
    val project: Int,
    val title: String,
    val href: String? = null,  // auto-generated from title if omitted
    val order: Long? = null
)

// Wiki Link Response
data class WikiLinkResponse(
    val id: Int,
    val project: Int,
    val title: String,
    val href: String,
    val order: Long
)
```

---

## Error Responses

All endpoints may return these error codes:

| Status | Meaning                | When                                      |
|--------|------------------------|-------------------------------------------|
| 400    | Bad Request            | Missing required fields, validation errors |
| 401    | Unauthorized           | Missing/invalid auth token                |
| 403    | Forbidden              | Insufficient permissions                  |
| 404    | Not Found              | Resource doesn't exist                    |
| 429    | Too Many Requests      | Rate limit exceeded                       |

Error response body format:
```json
{
  "_error_message": "Error description",
  "_error_type": "taiga.base.exceptions.ErrorType"
}
```

Or field-specific validation errors:
```json
{
  "field_name": ["Error message for this field"]
}
```

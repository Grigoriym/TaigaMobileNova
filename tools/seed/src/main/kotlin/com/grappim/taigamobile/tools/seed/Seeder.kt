package com.grappim.taigamobile.tools.seed

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Seeder(private val api: TaigaApi) {

    suspend fun seed(
        username: String,
        password: String,
        projectName: String,
        milestoneCount: Int = 3,
        epicCount: Int = 5,
        userStoryCount: Int = 20,
        taskCount: Int = 30,
        issueCount: Int = 15
    ) {
        // 1. Authenticate
        println("=== Authenticating as '$username' ===")
        val loginResponse = api.login(LoginRequest(username = username, password = password, type = "normal"))
        println("Authenticated. User ID: ${loginResponse.id}")

        // 2. Create project
        println("\n=== Creating project '$projectName' ===")
        val project = api.createProject(
            CreateProjectRequest(
                name = projectName,
                description = "Test project seeded with realistic data for development and QA."
            )
        )
        val projectId = project.id
        println("Created project '${project.name}' (ID: $projectId, slug: ${project.slug})")

        // 3. Get project detail
        println("\n=== Fetching project detail ===")
        val detail = api.getProjectDetail(projectId)
        println("Epic statuses: ${detail.epicStatuses.map { it.name }}")
        println("US statuses: ${detail.usStatuses.map { it.name }}")
        println("Task statuses: ${detail.taskStatuses.map { it.name }}")
        println("Issue statuses: ${detail.issueStatuses.map { it.name }}")
        println("Issue types: ${detail.issueTypes.map { it.name }}")
        println("Priorities: ${detail.priorities.map { it.name }}")
        println("Severities: ${detail.severities.map { it.name }}")
        println("Points: ${detail.points.map { "${it.name}(${it.value})" }}")
        println("Roles: ${detail.roles.map { "${it.name}(computable=${it.computable})" }}")

        val computableRoles = detail.roles.filter { it.computable }
        val pointValues = detail.points.filter { it.value != null }

        // 4. Create milestones
        println("\n=== Creating $milestoneCount milestones ===")
        val today = LocalDate.now()
        val milestones = (0 until milestoneCount).map { i ->
            val start = today.plusWeeks((i * 2).toLong())
            val finish = start.plusDays(13)
            val response = api.createMilestone(
                CreateMilestoneRequest(
                    project = projectId,
                    name = milestoneNames[i % milestoneNames.size],
                    estimatedStart = start.format(DATE_FORMAT),
                    estimatedFinish = finish.format(DATE_FORMAT),
                    order = i + 1
                )
            )
            println("  Created milestone '${response.name}' (ID: ${response.id})")
            response
        }

        // 5. Create epics
        println("\n=== Creating $epicCount epics ===")
        val epics = (0 until epicCount).map { i ->
            val statusId = detail.epicStatuses[i % detail.epicStatuses.size].id
            val response = api.createEpic(
                CreateEpicRequest(
                    project = projectId,
                    subject = epicSubjects[i % epicSubjects.size],
                    description = epicDescriptions[i % epicDescriptions.size],
                    status = statusId,
                    color = epicColors[i % epicColors.size],
                    tags = listOf(tagPool[i % tagPool.size])
                )
            )
            println("  Created epic #${response.ref} '${response.subject}' (ID: ${response.id})")
            response
        }

        // 6. Create user stories
        println("\n=== Creating $userStoryCount user stories ===")
        val userStories = (0 until userStoryCount).map { i ->
            val statusId = detail.usStatuses[i % detail.usStatuses.size].id
            val milestone = if (i % 5 < 3 && milestones.isNotEmpty()) {
                milestones[i % milestones.size].id
            } else {
                null
            }
            val points = if (computableRoles.isNotEmpty() && pointValues.isNotEmpty()) {
                computableRoles.associate { role ->
                    role.id.toString() to pointValues[i % pointValues.size].id
                }
            } else {
                null
            }
            val response = api.createUserStory(
                CreateUserStoryRequest(
                    project = projectId,
                    subject = userStorySubjects[i % userStorySubjects.size],
                    description = userStoryDescriptions[i % userStoryDescriptions.size],
                    status = statusId,
                    milestone = milestone,
                    tags = listOf(tagPool[(i + 1) % tagPool.size]),
                    points = points
                )
            )
            println("  Created US #${response.ref} '${response.subject}' (ID: ${response.id})")
            response
        }

        // 7. Link user stories to epics
        if (epics.isNotEmpty()) {
            val storiesToLink = userStories.take((userStories.size * 4) / 5) // ~80%
            println("\n=== Linking ${storiesToLink.size} user stories to epics ===")
            storiesToLink.forEachIndexed { i, us ->
                val epic = epics[i % epics.size]
                api.linkUserStoryToEpic(epic.id, EpicRelatedUserStoryRequest(epic = epic.id, userStory = us.id))
                println("  Linked US #${us.ref} -> Epic #${epic.ref}")
            }
        }

        // 8. Create tasks
        println("\n=== Creating $taskCount tasks ===")
        (0 until taskCount).forEach { i ->
            val statusId = detail.taskStatuses[i % detail.taskStatuses.size].id
            val linkedUs = if (i % 10 < 7 && userStories.isNotEmpty()) {
                userStories[i % userStories.size]
            } else {
                null
            }
            val response = api.createTask(
                CreateTaskRequest(
                    project = projectId,
                    subject = taskSubjects[i % taskSubjects.size],
                    description = taskDescriptions[i % taskDescriptions.size],
                    status = statusId,
                    userStory = linkedUs?.id,
                    milestone = linkedUs?.milestone,
                    tags = listOf(tagPool[(i + 2) % tagPool.size])
                )
            )
            println("  Created task #${response.ref} '${response.subject}' (ID: ${response.id})")
        }

        // 9. Create issues
        println("\n=== Creating $issueCount issues ===")
        (0 until issueCount).forEach { i ->
            val statusId = detail.issueStatuses[i % detail.issueStatuses.size].id
            val typeId = detail.issueTypes[i % detail.issueTypes.size].id
            val severityId = detail.severities[i % detail.severities.size].id
            val priorityId = detail.priorities[i % detail.priorities.size].id
            val response = api.createIssue(
                CreateIssueRequest(
                    project = projectId,
                    subject = issueSubjects[i % issueSubjects.size],
                    description = issueDescriptions[i % issueDescriptions.size],
                    status = statusId,
                    type = typeId,
                    severity = severityId,
                    priority = priorityId,
                    tags = listOf(tagPool[(i + 3) % tagPool.size])
                )
            )
            println("  Created issue #${response.ref} '${response.subject}' (ID: ${response.id})")
        }

        // 10. Create wiki pages
        println("\n=== Creating wiki pages ===")
        wikiPages.forEach { (slug, content) ->
            val response = api.createWikiPage(
                CreateWikiPageRequest(
                    project = projectId,
                    slug = slug,
                    content = content
                )
            )
            println("  Created wiki page '${response.slug}' (ID: ${response.id})")
        }

        // 11. Create wiki links
        println("\n=== Creating wiki links ===")
        wikiLinks.forEachIndexed { i, (title, href) ->
            val response = api.createWikiLink(
                CreateWikiLinkRequest(
                    project = projectId,
                    title = title,
                    href = href,
                    order = (i + 1).toLong()
                )
            )
            println("  Created wiki link '${response.title}' -> ${response.href} (ID: ${response.id})")
        }

        println("\n=== Seeding complete! ===")
        println("Project: ${project.name} (ID: $projectId)")
        println("Created: $milestoneCount milestones, $epicCount epics, $userStoryCount user stories, $taskCount tasks, $issueCount issues, ${wikiPages.size} wiki pages, ${wikiLinks.size} wiki links")
    }

    companion object {
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        private val tagPool = listOf(
            "backend", "frontend", "design", "infrastructure", "security",
            "performance", "testing", "documentation", "ux", "mobile"
        )

        private val epicColors = listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"
        )

        private val milestoneNames = listOf(
            "Sprint 1 - Foundation",
            "Sprint 2 - Core Features",
            "Sprint 3 - Polish & QA",
            "Sprint 4 - Performance",
            "Sprint 5 - Release Prep"
        )

        private val epicSubjects = listOf(
            "User Authentication & Authorization",
            "Dashboard & Analytics",
            "Notification System",
            "Search & Filtering",
            "Data Export & Reporting",
            "Mobile Responsive Design",
            "API Integration Layer",
            "User Profile Management"
        )

        private val epicDescriptions = listOf(
            "Implement complete authentication flow including login, registration, password reset, and role-based access control.",
            "Build an interactive dashboard with real-time charts, KPIs, and customizable widgets for project metrics.",
            "Design and implement a notification system supporting email, in-app, and push notifications with user preferences.",
            "Add full-text search across all project entities with advanced filtering, saved searches, and recent search history.",
            "Enable data export in multiple formats (CSV, PDF, JSON) with scheduled reports and email delivery.",
            "Ensure all views are fully responsive and optimized for mobile devices and tablets.",
            "Create a unified API layer for third-party integrations with webhook support and API key management.",
            "Allow users to manage their profiles, avatars, notification preferences, and connected accounts."
        )

        private val userStorySubjects = listOf(
            "As a user I want to log in with my credentials",
            "As a user I want to reset my forgotten password",
            "As a user I want to see my project dashboard",
            "As an admin I want to manage team members",
            "As a user I want to receive email notifications",
            "As a user I want to search across all items",
            "As a user I want to filter issues by status",
            "As a user I want to export data to CSV",
            "As a user I want to customize my profile",
            "As a user I want to view activity history",
            "As a user I want to create new tasks quickly",
            "As a user I want to drag and drop tasks on the board",
            "As an admin I want to configure project settings",
            "As a user I want to comment on user stories",
            "As a user I want to attach files to issues",
            "As a user I want to set due dates on tasks",
            "As a user I want to mention team members in comments",
            "As a user I want to view sprint burndown charts",
            "As a user I want to bulk edit user stories",
            "As a user I want to create subtasks for user stories",
            "As a user I want to mark tasks as blocked",
            "As a user I want to assign story points",
            "As a user I want to tag items for categorization",
            "As a user I want to archive completed sprints",
            "As a user I want to see a timeline view"
        )

        private val userStoryDescriptions = listOf(
            "Users should be able to log in using their username/email and password. Include remember me option and session management.",
            "Implement a password reset flow with email verification link. Token should expire after 24 hours.",
            "The dashboard should show project progress, recent activity, sprint status, and key metrics at a glance.",
            "Admins need to invite, remove, and change roles of team members. Include bulk operations.",
            "Send configurable email notifications for mentions, assignments, status changes, and due date reminders.",
            "Implement full-text search that queries across user stories, tasks, issues, and wiki pages with result ranking.",
            "Add filter controls for issue list with status, type, severity, priority, and assignee filters. Support combining filters.",
            "Allow exporting filtered data to CSV format with column selection and date range filtering.",
            "Users should be able to update their display name, avatar, bio, and notification preferences.",
            "Show a chronological feed of all changes made to items the user is watching or assigned to.",
            "Provide a quick-add form for creating tasks with just a subject, auto-assigning to current sprint.",
            "Implement drag and drop on the kanban board for changing task status with smooth animations.",
            "Settings page for project name, description, modules, default values, and member permissions.",
            "Add a comment section to user stories with markdown support, mentions, and edit/delete capabilities.",
            "Support file attachments on issues with drag-and-drop upload, preview, and size limits.",
            "Add a date picker for due dates with visual indicators for overdue, due today, and upcoming items.",
            "Implement @mention autocomplete in comment fields that notifies mentioned users.",
            "Display a burndown chart showing ideal vs actual progress for the current and past sprints.",
            "Allow selecting multiple user stories and updating status, milestone, or assignee in bulk.",
            "Enable creating sub-tasks within a user story with their own status and assignee tracking.",
            "Users can mark tasks as blocked with a reason. Blocked items should be visually distinct on the board.",
            "Add story point estimation with planning poker values. Points should roll up to epics and milestones.",
            "Implement a tagging system with autocomplete for existing tags and color coding.",
            "Allow archiving completed sprints to keep the backlog clean while preserving historical data.",
            "Create a timeline/Gantt view showing user stories plotted across sprints with dependencies."
        )

        private val taskSubjects = listOf(
            "Set up authentication middleware",
            "Create login form component",
            "Implement JWT token refresh",
            "Design database schema for users",
            "Write unit tests for auth service",
            "Configure CI/CD pipeline",
            "Set up error logging service",
            "Implement password hashing",
            "Create API documentation",
            "Design notification templates",
            "Implement email sending service",
            "Create search index configuration",
            "Build filter dropdown components",
            "Implement CSV export endpoint",
            "Design responsive navigation",
            "Create user avatar upload handler",
            "Set up WebSocket connections",
            "Implement rate limiting middleware",
            "Create database migration scripts",
            "Write integration tests for API",
            "Implement caching layer",
            "Create loading skeleton components",
            "Set up feature flags system",
            "Implement audit log tracking",
            "Create data validation utilities",
            "Design error boundary components",
            "Implement pagination for lists",
            "Create date formatting utilities",
            "Set up automated backups",
            "Implement health check endpoint",
            "Create modal dialog component",
            "Implement drag and drop handler",
            "Create tooltip component",
            "Design color theme system",
            "Implement keyboard shortcuts"
        )

        private val taskDescriptions = listOf(
            "Set up the authentication middleware to validate JWT tokens on protected routes.",
            "Build the login form with email/password fields, validation, and error handling.",
            "Implement automatic token refresh before expiration with retry logic.",
            "Design and create the database tables for user accounts and sessions.",
            "Write comprehensive unit tests covering all auth service methods and edge cases.",
            "Configure the CI/CD pipeline with build, test, lint, and deploy stages.",
            "Set up structured error logging with log levels and external service integration.",
            "Implement secure password hashing using bcrypt with configurable salt rounds.",
            "Generate OpenAPI/Swagger documentation for all REST endpoints.",
            "Design HTML email templates for notifications with responsive layout.",
            "Create a service for sending transactional emails with template rendering.",
            "Configure search indexing for full-text search across project entities.",
            "Build reusable filter dropdown components with multi-select and search.",
            "Create an API endpoint that generates CSV files from filtered query results.",
            "Design the responsive navigation component with mobile hamburger menu.",
            "Handle avatar image upload with resizing, format validation, and storage.",
            "Set up WebSocket connections for real-time updates on the kanban board.",
            "Add rate limiting to API endpoints to prevent abuse and ensure fair usage.",
            "Write database migration scripts for schema changes with rollback support.",
            "Write integration tests that test API endpoints with a test database.",
            "Implement a caching layer for frequently accessed data with TTL configuration.",
            "Create skeleton loading components for improved perceived performance.",
            "Set up a feature flags system for gradual rollout of new features.",
            "Implement audit log tracking for all entity modifications with user attribution.",
            "Create reusable data validation utilities for common field types.",
            "Design error boundary components to gracefully handle UI errors.",
            "Implement cursor-based pagination for all list endpoints.",
            "Create date formatting utilities supporting relative and absolute formats.",
            "Set up automated database backups with configurable retention policy.",
            "Create a health check endpoint that verifies database and service connectivity.",
            "Build a reusable modal dialog component with customizable content and actions.",
            "Implement the drag and drop handler for reordering items on the board.",
            "Create a tooltip component with configurable placement and content.",
            "Design a color theme system with light and dark mode support.",
            "Implement keyboard shortcuts for common actions with a help overlay."
        )

        private val issueSubjects = listOf(
            "Login button not responding on mobile Safari",
            "Dashboard charts not loading with large datasets",
            "Email notifications sent with wrong timezone",
            "Search results don't include wiki pages",
            "CSV export missing header row",
            "Avatar upload fails for PNG files over 2MB",
            "Kanban board flickers during drag operation",
            "Password reset link expires too quickly",
            "Filter state lost on page refresh",
            "Comment markdown preview not rendering code blocks",
            "Sprint burndown chart shows incorrect dates",
            "Bulk edit doesn't update modified timestamp",
            "Task due date shows wrong day in some timezones",
            "Notification badge count doesn't update in real time",
            "API rate limit returns HTML instead of JSON error",
            "File attachment download fails for filenames with spaces",
            "User story points not recalculated after task removal",
            "Dark mode colors have insufficient contrast ratio",
            "WebSocket reconnection fails silently after network change",
            "Search indexing delays cause stale results"
        )

        private val issueDescriptions = listOf(
            "The login button on the authentication page does not respond to taps on mobile Safari (iOS 17). Works fine on Chrome.",
            "When a project has more than 1000 items, the dashboard charts fail to render and show a blank area.",
            "Email notification timestamps are showing UTC instead of the user's configured timezone preference.",
            "Full-text search only returns results from user stories and tasks but not from wiki page content.",
            "When exporting filtered data to CSV, the header row with column names is missing from the output file.",
            "Uploading PNG avatar images larger than 2MB results in a 500 error. JPEG files of the same size work fine.",
            "The kanban board flickers and briefly shows items in the wrong column during a drag and drop operation.",
            "Password reset tokens expire after 1 hour instead of the documented 24-hour window.",
            "Applied filters on the issue list are lost when navigating away and returning to the page.",
            "The markdown preview in comments doesn't render fenced code blocks with syntax highlighting.",
            "The sprint burndown chart displays dates offset by one day, showing progress for the wrong dates.",
            "Using bulk edit to change multiple user story statuses doesn't update the modified_date field.",
            "Tasks with due dates near midnight show the wrong day depending on the user's timezone offset.",
            "The notification badge count in the header doesn't update until a full page refresh.",
            "When the API rate limit is exceeded, the response Content-Type is text/html instead of application/json.",
            "Downloading attached files with spaces in the filename results in a 404 error.",
            "Removing a task from a user story doesn't trigger recalculation of total story points.",
            "Several UI elements in dark mode have contrast ratios below WCAG AA requirements.",
            "After a network interruption, WebSocket connections don't attempt to reconnect automatically.",
            "Newly created items don't appear in search results for several minutes due to indexing delays."
        )

        private val wikiPages = listOf(
            "home" to """# Project Home

Welcome to the project wiki! This is the central knowledge base for our team.

## Quick Links

- [Getting Started](getting-started) - Setup guide for new team members
- [Architecture](architecture) - System architecture and design decisions
- [API Reference](api-reference) - REST API documentation

## Project Overview

This project is built with a modern technology stack focusing on developer experience, maintainability, and performance. We follow agile methodologies with 2-week sprints.

## Team Agreements

- **Code reviews** are required for all changes
- **Tests** must pass before merging
- **Documentation** should be updated alongside code changes
- **Daily standups** at 10:00 AM in the team channel
""",
            "getting-started" to """# Getting Started

This guide will help you set up your development environment and get the project running locally.

## Prerequisites

- JDK 21 or later
- Docker and Docker Compose
- Git

## Setup Steps

1. Clone the repository
2. Copy `.env.example` to `.env` and fill in your local configuration
3. Run `docker-compose up -d` to start dependencies
4. Run `./gradlew build` to compile and run tests
5. Run `./gradlew run` to start the development server

## Project Structure

```
├── app/          - Main application module
├── feature/      - Feature modules (data/domain/ui)
├── core/         - Shared core modules
├── utils/        - Utility modules
└── build-logic/  - Gradle convention plugins
```

## Common Tasks

- **Run tests:** `./gradlew test`
- **Check code style:** `./gradlew lint`
- **Generate docs:** `./gradlew dokka`

## Getting Help

If you're stuck, check the [Architecture](architecture) page or ask in the team channel.
""",
            "architecture" to """# Architecture

## Overview

The application follows a modular MVVM + Clean Architecture pattern with clear separation of concerns.

## Module Structure

### Feature Modules

Each feature is self-contained with three layers:

- **data** - API clients, DTOs, repository implementations, Hilt modules
- **domain** - Models, repository interfaces, use cases
- **ui** - Screens, ViewModels, state classes, navigation

### Core Modules

Shared functionality used across features:

- **network** - HTTP client configuration and interceptors
- **database** - Local database and DAOs
- **common** - Shared models and utilities

## Design Patterns

### State Management

ViewModels expose a single `StateFlow` containing all UI state. State classes include callback functions for user actions.

### Navigation

Type-safe navigation using Compose Navigation with `@Serializable` route classes.

### Dependency Injection

Hilt is used for DI with module-level `@Module` and `@InstallIn` annotations.

## Data Flow

```
UI -> ViewModel -> UseCase/Repository -> API/Database
```

Data flows down through state, events flow up through callbacks and channels.
""",
            "api-reference" to """# API Reference

## Authentication

All API requests require a Bearer token obtained from the login endpoint.

```
POST /api/v1/auth
Authorization: Bearer {token}
```

## Endpoints Overview

| Module       | Endpoints                                  |
|-------------|-------------------------------------------|
| Projects    | CRUD operations on projects                |
| Milestones  | Sprint management                          |
| Epics       | Epic creation and user story linking       |
| User Stories | Backlog and sprint management             |
| Tasks       | Task tracking within user stories          |
| Issues      | Bug and issue tracking                     |
| Wiki        | Knowledge base pages and navigation links  |

## Rate Limiting

API requests are rate-limited to 100 requests per minute per user. Exceeding this limit returns a 429 status code.

## Error Handling

All errors return JSON with `_error_message` and `_error_type` fields. Field validation errors return a map of field names to error message arrays.

## Pagination

List endpoints support pagination via `page` and `page_size` query parameters. Default page size is 50, maximum is 200.
"""
        )

        private val wikiLinks = listOf(
            "Home" to "home",
            "Getting Started" to "getting-started",
            "Architecture" to "architecture",
            "API Reference" to "api-reference"
        )
    }
}

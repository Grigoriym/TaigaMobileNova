# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TaigaMobileNova is an unofficial Android client for the Taiga.io agile project management system. It's built using Kotlin, Jetpack Compose, and follows a modular architecture with clean separation of concerns.

## Architecture

### Module Structure
The project follows a feature-based modular architecture:

- **app/**: Main application module that aggregates all features
- **core/**: Shared infrastructure modules
  - `core:api`: Network layer and API definitions
  - `core:domain`: Domain models and business logic
  - `core:storage`: Data persistence layer
  - `core:async`: Coroutines and async utilities
  - `core:navigation`: Navigation definitions
- **feature/**: Feature-specific modules (each with data/domain/ui layers)
  - `feature:login`: Authentication
  - `feature:dashboard`: Main dashboard
  - `feature:projects`: Project management
  - `feature:wiki`: Wiki functionality
  - `feature:epics`, `feature:userstories`, `feature:tasks`, `feature:issues`: Agile work items
  - `feature:kanban`: Kanban board
  - `feature:sprint`: Sprint management
- **uikit/**: Shared UI components and design system
- **utils/**: Utility modules for formatting and UI helpers
- **strings/**: Localization resources
- **testing/**: Shared testing utilities

### Technology Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Dagger Hilt
- **Navigation**: Jetpack Navigation Compose
- **Networking**: Retrofit + OkHttp + kotlin Serialization
- **Async**: Kotlin Coroutines
- **Image Loading**: Coil

### Build Configuration
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Build Tool**: Gradle with Kotlin DSL
- **Custom Plugins**: Located in `build-logic/` for shared build configuration

### Code Quality Tools
- **Ktlint**: Enforces Kotlin style guide with Compose rules
- **Detekt**: Static code analysis with custom configuration in `config/detekt/detekt.yml`
- **Jacoco**: Code coverage reporting with exclusions for generated code
- **Module Graph Assertion**: Ensures modular architecture constraints (max height: 10)

### Key Patterns
- Each feature module follows data/domain/ui layering and sometimes dto/mapper
- Domain layer contains use cases and repository interfaces
- Data layer implements repositories and handles API/storage
- UI layer uses Compose with ViewModel + State pattern
- Navigation destinations are defined per feature
- Dependency injection configured at module level

## Development Setup
- Project uses Gradle Version Catalogs (`gradle/libs.versions.toml`) for dependency management

## Testing

### Running Tests
To run unit tests for a specific module and test class:
```bash
./gradlew :module:path:testFdroidDebugUnitTest --tests "com.package.TestClassName"
```

Examples:
```bash
# Run all tests in a module
./gradlew :feature:workitem:mapper:testFdroidDebugUnitTest

# Run specific test class
./gradlew :feature:workitem:mapper:testFdroidDebugUnitTest --tests "com.grappim.taigamobile.feature.workitem.mapper.JsonObjectMapperTest"

# Run specific test method
./gradlew :feature:workitem:mapper:testFdroidDebugUnitTest --tests "com.grappim.taigamobile.feature.workitem.mapper.JsonObjectMapperTest.toJsonObject should map null value to JsonNull"
```

Note: Use `testFdroidDebugUnitTest` or `testGplayDebugUnitTest` variants. The generic `test` task does not support `--tests` filter.

### Testing Utilities
- The `:testing` module contains shared testing utilities and fake data generators
- **TestUtils.kt**: Provides random data generators:
  - `getRandomString()`, `getRandomLong()`, `getRandomInt()`, `getRandomBoolean()`
  - `getRandomUri()`, `getRandomFile()`, `getRandomColor()`
  - `nowLocalDate` for date testing
  - `testException` for exception testing
- **Fake data generators** (in separate files like `AttachmentFakes.kt`, `UserFakes.kt`, etc.):
  - `getAttachment()`, `getAttachmentDTO()`
  - `getUser()`, `getStatus()`, `getTag()`, `getType()`, `getSeverity()`, `getPriority()`
  - `getFiltersData()`, `getWorkItemResponseDTO()`, `getProjectExtraInfo()`
- **Test rules**: `MainDispatcherRule`, `SavedStateHandleRule`
- Use `UnconfinedTestDispatcher()` for coroutine testing
- Tests use JUnit 4 with `kotlin.test` assertions and MockK for mocking
- Test dependencies are automatically added via convention plugins - no need to check or modify build.gradle.kts when writing tests

# important-instruction-reminders

- Do what has been asked; nothing more, nothing less.
- Do not use early returns in Composable functions - use conditional wrapping instead
- NEVER create files unless they're absolutely necessary for achieving your goal.
- ALWAYS prefer editing an existing file to creating a new one.
- NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.
- DO NOT add comments to code unless explicitly requested by the user
- Write clean, self-documenting code without unnecessary comments
- If an action requires creating a module, then it is better to be done manually by the user

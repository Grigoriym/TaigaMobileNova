# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TaigaMobileNova is an unofficial Android client for the Taiga.io agile project management system. It's built using Kotlin, Jetpack Compose, and follows a modular architecture with clean separation of concerns.

## Development Commands

### Build & Test
- `./gradlew build` - Build the entire project
- `./gradlew app:assembleDebug` - Build debug APK
- `./gradlew app:assembleRelease` - Build release APK
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests

### Code Quality
- `./gradlew ktlintCheck` - Check Kotlin code style
- `./gradlew ktlintFormat` - Auto-format Kotlin code
- `./gradlew detekt` - Run static code analysis
- `./gradlew testAggregatedCoverage` - Generate test coverage report

### Testing Requirements
Tests require a local Taiga instance. The instance is automatically created and stopped for every Test gradle task. See `taiga-test-instance` folder for helper scripts and configurations.

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
- **Language**: Kotlin 2.2.0
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Dagger Hilt
- **Navigation**: Jetpack Navigation Compose
- **Networking**: Retrofit + OkHttp + Moshi
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
- Each feature module follows data/domain/ui layering
- Domain layer contains use cases and repository interfaces
- Data layer implements repositories and handles API/storage
- UI layer uses Compose with ViewModel + State pattern
- Navigation destinations are defined per feature
- Dependency injection configured at module level

## Development Setup
- Requires JDK 17+
- Use latest Android Studio version
- Project uses Gradle Version Catalogs (`gradle/libs.versions.toml`) for dependency management

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.

## Code Style
- DO NOT add comments to code unless explicitly requested by the user
- Write clean, self-documenting code without unnecessary comments
# tools:seed

Standalone Kotlin module for populating a Taiga test server with test data.

This module is **not** depended on by any other module — it is excluded from the APK build.

## Usage

```bash
./gradlew :tools:seed:run
```

## Configuration

Set environment variables before running:

```bash
TAIGA_BASE_URL=https://your-taiga-instance.com ./gradlew :tools:seed:run
```

## Notes

- Uses Ktor (CIO engine) for HTTP, keeping it a pure Kotlin/JVM module with no Android dependencies.
- Listed in `settings.gradle.kts` but not included in the app dependency graph.

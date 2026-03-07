[![Codacy Badge](https://app.codacy.com/project/badge/Grade/df9b05b34af1456fbb8fe75fbab0f6f2)](https://app.codacy.com/gh/Grigoriym/TaigaMobileNova/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) [![codecov](https://codecov.io/gh/Grigoriym/TaigaMobileNova/branch/master/graph/badge.svg?token=8SI5NVSBNF)](https://codecov.io/gh/Grigoriym/TaigaMobileNova)

# Taiga Mobile Nova

This is the **unofficial** Kotlin Multiplatform client for the agile project management system [taiga.io](https://www.taiga.io/), targeting **Android**, **iOS**, and **Desktop** (Linux/macOS/Windows).

The previous author archived the original project. This version has been completely rewritten using Kotlin Multiplatform and Compose Multiplatform.

## Platforms

| Platform | Status | Distribution |
|----------|--------|--------------|
| Android  | Released | Google Play & F-Droid |
| iOS      | Builds & runs | Distribution TBD |
| Desktop (Linux / macOS / Windows) | Builds & runs | Distribution TBD |

### Android

[<img src="docs/google-badge.png"
alt="Get it on Google Play"
height="80">](https://play.google.com/store/apps/details?id=com.grappim.taigamobile)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
alt="Get it on F-Droid"
height="80">](https://f-droid.org/en/packages/com.grappim.taigamobile.fdroid/)

### iOS & Desktop

iOS and Desktop builds are functional but distribution channels are not yet set up. If you want to try them, clone the repo and build locally — see [Build Commands](#build-commands) below.

[Project board](https://tasks.gregstuff.click/project/taigamobilenova/kanban)

## Screenshots

| Dark Mode | Light Mode |
|-----------|------------|
| <img width="400" height="900" alt="Login - Dark" src="./info/art/login-dark.png" /> | <img width="400" height="900" alt="Login - Light" src="./info/art/login-light.png" /> |
| <img width="400" height="900" alt="Dashboard - Dark" src="./info/art/dashboard-dark.png" /> | <img width="400" height="900" alt="Dashboard - Light" src="./info/art/dashboard-light.png" /> |
| <img width="400" height="900" alt="Bookmarks - Dark" src="./info/art/bookmarks-dark.png" /> | <img width="400" height="900" alt="Bookmarks - Light" src="./info/art/bookmarks-light.png" /> |
| <img width="400" height="900" alt="Drawer - Dark" src="./info/art/drawer-dark.png" /> | <img width="400" height="900" alt="Drawer - Light" src="./info/art/drawer-light.png" /> |
| <img width="400" height="900" alt="Kanban - Dark" src="./info/art/kanban-dark.png" /> | <img width="400" height="900" alt="Kanban - Light" src="./info/art/kanban-light.png" /> |
| <img width="400" height="900" alt="Issue Details - Dark" src="./info/art/issue-dark.png" /> | <img width="400" height="900" alt="Issue Comments - Dark" src="./info/art/issue-2-dark.png" /> |
| <img width="400" height="900" alt="Issues List - Dark" src="./info/art/issues-dark.png" /> | |

## Features

### View & Browse
* Projects
* Epics
* User stories
* Tasks
* Issues
* Sprints
* Profiles
* Wiki
* Dashboard

### Create, Edit & Delete
* Epics
* User stories
* Tasks
* Issues
* Sprints
* Wiki pages

### Additional Features
* Leave and delete comments
* Kanban board (for sprints and user stories)
* Filters for user stories, epics, and issues
* Permissions validation

## Build Commands

```bash
# Android
./gradlew :androidApp:assembleGplayDebug
./gradlew :androidApp:assembleFdroidDebug

# Desktop — run or package
./gradlew :composeApp:run
./gradlew :composeApp:packageDistributionForCurrentOS   # .deb / .dmg / .msi

# iOS framework (called automatically by Xcode)
./gradlew :composeApp:linkReleaseFrameworkIosSimulatorArm64
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```

## About
This project is a complete rewrite of the [original TaigaMobile app](https://github.com/EugeneTheDev/TaigaMobile) (now archived), rebuilt from scratch with Kotlin Multiplatform, Compose Multiplatform, and modern architecture.

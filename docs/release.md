# Release Process

## Overview

The release flow is partially automated via two GitHub Actions workflows. You trigger the start, review the PR, merge it — everything else runs automatically.

```
[You] Trigger release-prepare  →  PR created  →  [You] Review & merge
      ↓                                                     ↓
  release-finalize fires automatically
      ↓
  Tag pushed  →  release.yml builds APKs/AAB + GitHub Release
      ↓
  master back-merged into dev
```

---

## Step-by-step

### 1. Trigger `Release Prepare`

Go to **Actions → Release Prepare → Run workflow** and enter:

| Input          | Example   | Description                                      |
|----------------|-----------|--------------------------------------------------|
| `version`      | `2.0.8`   | Version name (semver)                            |
| `version_code` | `39`      | Android version code (integer, must increment)   |

The workflow will:
- Merge `dev` into `master` locally (not pushed)
- Create branch `release/v{version}` from that merged state
- Bump `version-name` and `version-code` in `gradle/libs.versions.toml`
- Create a changelog stub at `fastlane/metadata/android/en-US/changelogs/{version_code}.txt`
- Push the branch and open a PR targeting `master`

### 2. Edit the F-Droid changelog

Before merging the PR, edit the changelog stub that was created:

```
fastlane/metadata/android/en-US/changelogs/{version_code}.txt
```

Keep it short (max ~500 chars). F-Droid displays this as "What's new" in the app listing.
You can push additional commits to the release branch before merging.

### 3. Review and merge the PR

- Do **not** squash merge — use a regular merge commit
- The PR checklist will remind you of the changelog and version bump

### 4. Automatic steps (after merge)

`Release Finalize` fires automatically and:
1. Creates and pushes tag `v{version}`
2. The existing `release.yml` triggers from the tag push — builds and uploads:
   - `app-fdroid-release.apk`
   - `app-fdroid-debug.apk`
   - `app-gplay-release.apk`
   - `app-gplay-release.aab`
   - Creates a GitHub Release with auto-generated notes
3. Back-merges `master` into `dev`

---

## F-Droid metadata

F-Droid picks up store listing content from the `fastlane/` directory (Triple-T format):

```
fastlane/metadata/android/en-US/
├── title.txt                  # App name
├── short_description.txt      # Short tagline (≤80 chars)
├── full_description.txt       # Full store description
├── changelogs/
│   └── {version_code}.txt     # "What's new" for each release
└── images/
    ├── icon.png
    ├── featureGraphic.png
    ├── phoneScreenshots/
    ├── sevenInchScreenshots/
    └── tenInchScreenshots/
```

To update store text or screenshots, edit those files on `dev` and they will be included in the next release automatically (picked up when `dev` is merged into the release branch).

---

## Prerequisites

### `RELEASE_PAT` secret (required)

`dev` has branch protection requiring PRs, so `GITHUB_TOKEN` (the Actions bot) cannot push directly to it. `release-finalize.yml` uses `RELEASE_PAT` instead, which must be a PAT from an admin account (admins bypass the protection since `enforce_admins` is off).

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens (or classic with `repo` scope)
2. Create a token with `Contents: read & write` on this repository
3. Add it as a repository secret named `RELEASE_PAT` (Settings → Secrets → Actions)

## Troubleshooting

**Merge conflict when merging `dev` into master locally (release-prepare)**
The workflow will fail. Resolve by merging `dev` into `master` locally, resolving conflicts, pushing master, then re-triggering the workflow. Or manually create the release branch and open a PR.

**Need to cancel a release in progress**
Delete the `release/v{version}` branch. The `release-finalize` workflow only triggers on PR merge, so nothing automatic will happen until you merge.

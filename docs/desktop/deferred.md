# Desktop Linux release: deferred and considered items

**Split out:** 2026-08-09, when [linux-release-plan.md](linux-release-plan.md) closed (tasks 0–9 all
done, its status table empty). That plan was a sequenced todo list; this doc is not — it's a
standing record of ideas that were surveyed and intentionally *not* actioned, so they aren't
silently forgotten and aren't re-proposed from scratch without the context already gathered here.

Nothing below is scoped or started. Pick one up as its own task/plan when there's a concrete reason
to.

| Idea | Why deferred |
|---|---|
| AppImage / Flatpak / Snap | Real additional infrastructure (signing keys, store manifests, Flathub/Snap Store review) — worth it only once the `.deb`/`.rpm`-via-GitHub-Releases path (tasks 0–4) is proven to work end-to-end and there's a concrete reason those aren't enough. |
| Apt repository (so users get updates via `apt upgrade` instead of re-downloading) | Needs a signing key and hosting; same "prove the simple path first" reasoning as above. |
| Auto-update mechanism inside the app | Not attempted — no existing infra to build on (see [survey.md](survey.md)); a reasonable follow-up once there's more than one release to update *to*. |
| arm64 Linux build | jpackage packages for the runner's own architecture only; would need a second CI job on an arm64 runner. Not scoped — revisit if there's actual user demand. |
| macOS/Windows CI + release wiring | Out of scope for this plan (titled Linux release deliberately) — `Dmg`/`Msi` target formats already exist in the Gradle config and build locally per the survey, but wiring them into CI/release is a separate plan, not folded in here to keep this one small. |
| Real iOS/Desktop GitHub OAuth implementation | Task 8 only hid the dead button on JVM/iOS (both share the identical no-op `GithubOAuthWebViewDialog` stub) rather than building the real in-app WebView flow — `docs/features/github-auth/plan.md` already lists this out of scope. Bigger than a Linux-release-scoped fix; affects iOS too. |
| Real JVM offline-transition test in the app's own test suite | Task 9 unit-tests the underlying `isHostReachable` reachability check (both branches, via real sockets) but doesn't drive `NetworkMonitorImpl`'s 5-second polling loop or the UI banner in an automated test — verified live instead (gregory disconnected the machine's real network and confirmed the banner). Automating that would need either a real sleep in the suite or a production seam purely for testing, which CLAUDE.md's "Don't Break Production in Favor of Tests" rule rules out without asking first. |

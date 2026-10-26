# Telegram 13.1.1 migration status

Target upstream: `DrKLO/Telegram` commit `3de37c8cfb039452be7398909461da6288babf15` (13.1.1, 2026-10-25).

This branch is an isolated migration workspace. The current source tree is still the older AyuGram-derived SealGram code; changing the version string or copying a single upstream screen is not a valid migration.

## Verified differences

- Current `gradle.properties` identifies the app as version `9.6.6`, code `3362`.
- Current `TMessagesProj/build.gradle` uses Android Gradle Plugin `7.4.2`, compile SDK 33, build tools 33.0.1, and NDK 21.4.7075529.
- Upstream Telegram 13.1.1 uses compile SDK 36, build tools 36.0.0, and NDK 27.2.12479018, plus newer Gradle build/plugin structure.
- Current profile UI is the older `ProfileActivity.java`; the upstream tree contains `ProfileActivity2.java` and `Components/ProfileMusicView.java`, neither of which exists in the current source tree.
- Profile music is not a standalone view-only change. It depends on current Telegram TL schema/generated types, saved-music API requests and response handling, profile UI wiring, resources, and event/data lifecycle.
- Current app has its own package/version/build pipeline and SealGram/AyuGram modifications. These must be retained intentionally rather than overwritten by an upstream directory copy.

## Migration order

1. A dedicated workflow `.github/workflows/upstream-baseline.yml` now checks out the exact official upstream commit with recursive submodules, installs SDK 36 / NDK 27.2.12479018 / CMake 3.22.1, builds `:TMessagesProj_App:assembleAfatDebug`, verifies the APK, and stores it as a short-lived artifact. This establishes whether the upstream baseline itself builds in CI before any custom code is ported.
2. Port app identity, package/build variants, signing configuration, privacy choices, and existing SealGram UI/features into that base.
3. Port protocol/schema changes required for profile music and regenerate/verify generated TL code using the upstream build tooling.
4. Port profile music UI and all referenced resources/event handling as one coherent change.
5. Port remaining SealGram/AyuGram features in small, reviewable groups.
6. Build debug APK in CI, inspect build logs, run APK/package checks, and only then consider a release.

## Current safety state

- Do not merge this branch into `main` until source migration and CI checks pass.
- The branch workflow has been adjusted so a push to this migration branch can run CI without publishing or replacing the `beta` release. Release publishing remains restricted to `main`.
- The existing SealGram debug build on this branch completed successfully in GitHub Actions (run `40379880617`); APK verification and APK audit also passed. This validates the current old source tree and the isolated CI path only, not the Telegram 13.1.1 migration.
- Next gate: wait for the dedicated upstream baseline workflow to finish. If it passes, begin porting app/build identity and custom features against that source; if it fails, fix the baseline first. Do not interpret the successful current build as evidence that the new Telegram source has been integrated.
- No claim is made that the 13.1.1 source migration or profile music port is complete.

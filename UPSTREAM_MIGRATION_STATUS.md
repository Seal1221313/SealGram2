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

1. Establish the official upstream source/build structure in a separate working tree and confirm its own build configuration.
2. Port app identity, package/build variants, signing configuration, privacy choices, and existing SealGram UI/features into that base.
3. Port protocol/schema changes required for profile music and regenerate/verify generated TL code using the upstream build tooling.
4. Port profile music UI and all referenced resources/event handling as one coherent change.
5. Port remaining SealGram/AyuGram features in small, reviewable groups.
6. Build debug APK in CI, inspect build logs, run APK/package checks, and only then consider a release.

## Current safety state

- Do not merge this branch into `main` until source migration and CI checks pass.
- The branch workflow has been adjusted so a push to this migration branch can run CI without publishing or replacing the `beta` release. Release publishing remains restricted to `main`.
- The Android build triggered by the workflow change was still running at the last status check; it has not yet been reported as successful.
- No claim is made that the 13.1.1 source migration or profile music port is complete.

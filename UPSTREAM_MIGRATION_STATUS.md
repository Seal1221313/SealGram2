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

## Migration scope: full mod update, not a single feature

The target is to update the whole existing SealGram/AyuGram-derived mod onto Telegram 13.1.1, preserving and re-porting the mod's applicable features—not just profile music. The compatibility inventory must cover:
- UI/screens, profile and chat interactions, settings, themes and localization;
- message/media handling, stickers, reactions, calls and protocol/TL changes;
- custom mod features, toggles, storage, event handlers, background work and notifications;
- app identity, build variants, signing, permissions, privacy/network behavior and third-party integrations;
- native/JNI code, libraries/submodules, resources, generated sources and build scripts.

Each feature needs an explicit outcome: ported, intentionally removed, replaced by upstream, or blocked with a reason. A successful compile alone does not prove feature parity.

## Migration order

1. **Passed:** workflow `.github/workflows/upstream-baseline.yml` checked out the exact official upstream commit with recursive submodules, installed SDK 36 / NDK 27.2.12479018 / CMake 3.22.1, built `:TMessagesProj_App:assembleAfatDebug`, verified the APK, and uploaded the artifact. Run: https://github.com/Seal1221313/SealGram2/actions/runs/40386629034.
2. Produce a complete file-level inventory between the old SealGram source and upstream 13.1.1: files unique to each tree and same-path files with different contents.
3. Use that inventory plus source history to map every custom feature to its implementation files and dependencies; group the migration by protocol, app/build structure, UI, settings/storage, media, networking/privacy, and native code.
4. Establish the new upstream tree as the base, then port the whole set of compatible custom changes in reviewable batches while resolving conflicts against the new Telegram APIs and TL schema.
5. Run compilation after each batch, add regression checks for preserved mod behavior, and maintain a feature parity checklist with explicit port/remove/replace/block status.
6. Run final CI build, APK signature/alignment/package audit, and regression checks before any merge or release.

## Current safety state

- Do not merge this branch into `main` until source migration and CI checks pass.
- The branch workflow has been adjusted so a push to this migration branch can run CI without publishing or replacing the `beta` release. Release publishing remains restricted to `main`.
- The existing SealGram debug build on this branch completed successfully in GitHub Actions (run `40379880617`); APK verification and APK audit also passed. This validates the current old source tree and the isolated CI path only, not the Telegram 13.1.1 migration.
- The official Telegram 13.1.1 baseline build has passed. The old SealGram build also passed separately. The next gate is the complete old-vs-upstream source inventory and feature map; neither success means the old mod has already been ported.
- No claim is made that the 13.1.1 source migration or profile music port is complete.

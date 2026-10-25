# SealGram2 — security audit / merge stage 1

Date: 2026-10-25

## Scope and status

Reviewed public source files in:
- Base: https://github.com/Seal1221313/SealGram-clean (default branch: main)
- Feature source: https://github.com/AyuGram/AyuGram4A (reviewed branch: rewrite)
- Destination: https://github.com/Seal1221313/SealGram2

This is a preliminary source review, not a full security certification. No source code has been deleted or modified from either upstream repository. No confirmed malware has been established from the reviewed files alone.

## Findings to verify before porting

### 1. Firebase Analytics collects device/build metadata (privacy concern; not proof of malware)

File: `TMessagesProj/src/main/java/com/exteragram/messenger/utils/CrashlyticsUtils.java`

The `logEvents(Context)` method sends an Analytics event named `stats` with Android version, app version/code, Play Services availability, device manufacturer/model, performance class, locale, cache directory path, screen refresh rate/display dimensions, and beta/debug status. This is more diagnostic data than a minimal crash report. Before porting, decide whether analytics is needed; preferred SealGram2 default is to omit AyuGram-specific analytics and avoid importing AyuGram's `google-services.json`. This behavior is privacy-sensitive but does not by itself prove malicious intent.

### 2. Firebase Crashlytics / Google services are integrated

Files: `TMessagesProj/build.gradle`, `TMessagesProj/google-services.json`, `TMessagesProj/src/main/java/org/telegram/messenger/ApplicationLoader.java`

The Gradle file includes Firebase Crashlytics, Analytics, Messaging, Config and related Google libraries and applies the Google Services / Crashlytics plugins. The checked-in Google Services file identifies AyuGram's Firebase project and package. Do not copy this project configuration into SealGram2; use project-specific configuration only if a deliberately selected feature requires it. Review crash-reporting initialization and opt-out behavior before enabling any crash telemetry.

### 3. AyuSync is an optional external synchronization feature

Files: `TMessagesProj/src/main/java/com/radolyn/ayugram/AyuConfig.java`, `.../sync/AyuSyncConfig.java`, `.../sync/AyuSyncWebSocketClient.java`, and `ApplicationLoader.java`

The default config sets `syncEnabled` to false, and secure connections default to true for non-beta builds. When enabled, the client connects to a configurable sync server, sends an authorization token plus app-package and device-identifier headers, and maintains a WebSocket connection. This is expected behavior for a sync feature, not proof of malware. If ported, it should remain disabled by default, clearly disclose the destination and data synchronized, enforce TLS in release builds, and never inherit an AyuGram server token or endpoint without explicit review.

### 4. Proprietary submodule requires separate review

File: `.gitmodules`

AyuGram references `AyuGram4A_proprietary` at `TMessagesProj/src/main/java/com/radolyn/ayugram/proprietary`. Its contents were not audited in this stage. Do not import opaque/proprietary contents until their source, license, behavior, and build necessity are established.

### 5. Build credentials and app identity must not be copied

File: `TMessagesProj/build.gradle`

AyuGram's build expects local `API_KEYS` and signing values, and its release configuration references a signing keystore. SealGram2 must not copy private signing keys, passwords, tokens, or AyuGram's app credentials. Configure SealGram2 with credentials owned by its maintainer and keep secrets out of Git.

## Base repository observations

SealGram-clean already has its own `BuildVars.java`, package/build configuration, Firebase-related Android platform integrations, and substantial Telegram client code. Merge changes selectively; do not replace entire upstream files wholesale. Preserve the existing SealGram-specific implementation unless a reviewed patch explicitly needs to change it.

## Next steps

1. Inspect the remaining AyuGram feature code and the proprietary submodule boundary.
2. Build a feature-to-file map for history, ghost mode, message filters, deleted/edited marks, UI preferences, and image/resources.
3. Port in small commits, retaining copyright/license notices and required upstream attributions.
4. Review permissions, manifest merge changes, hard-coded endpoints, telemetry, and secrets in every stage.
5. Run the repository's Android CI build and report the exact run result. No build has been run as part of this preliminary audit.


### 6. Device identifier is a stable identifier when sync is enabled

File: `TMessagesProj/src/main/java/com/radolyn/ayugram/AyuUtils.java`

`getDeviceIdentifier()` reads Android's `ANDROID_ID`. The sync registration request sends that identifier together with the device manufacturer/model to the configured sync service. Since sync is disabled by default, this is conditional behavior, but it is still sensitive tracking-like data and should be explicitly disclosed and minimized if the feature is ported.

### 7. Update checker fetches release metadata and APK links from GitHub

File: `TMessagesProj/src/main/java/com/exteragram/messenger/utils/UpdaterUtils.java`

The updater requests the latest release metadata from a GitHub repository selected through AyuGram constants and can download an APK from the release asset URL. This is a recognizable update feature, not evidence of malware on its own. Do not reuse AyuGram's update destination for SealGram2; any future updater must point to the maintainer-controlled project, show the destination clearly, and verify APK signing identity before installation. A full review of download/install flow is still required.

## Additional review result

The examined files did not provide sufficient evidence to label the inspected code as malware. The actionable issues so far are privacy/ownership risks (diagnostic analytics, stable device identifier used for sync, upstream Firebase configuration, and upstream update destination), plus the unreviewed proprietary submodule. These are reasons to exclude or isolate those integrations by default until they are deliberately configured—not grounds to delete unrelated features.

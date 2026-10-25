# SealGram2 — AyuGram feature port map

Status: inventory only. This file does not claim that features or image assets have been merged.

Upstream reviewed: https://github.com/AyuGram/AyuGram4A/tree/rewrite  
Base project: https://github.com/Seal1221313/SealGram-clean/tree/main

## Feature groups and likely source files

| Feature group | AyuGram source paths | Porting notes |
| --- | --- | --- |
| Settings/config model | `TMessagesProj/src/main/java/com/radolyn/ayugram/AyuConfig.java`, `AyuConstants.java` | Port only settings required by selected features; do not copy upstream endpoints, tokens, or unrelated app constants. |
| Ghost mode | `com/radolyn/ayugram/utils/AyuGhostUtils.java`, `AyuState.java`, `AyuStateVariable.java`, `AyuConfig.java` | Requires tracing all network/read-state hooks and UI toggle paths; test online/read state behavior against SealGram's existing modifications. |
| Deleted/edited message history | `com/radolyn/ayugram/AyuConfig.java`, `com/radolyn/ayugram/database/**`, `com/radolyn/ayugram/messages/AyuMessagesController.java`, `com/radolyn/ayugram/ui/AyuMessageHistory.java`, `com/radolyn/ayugram/ui/AyuMessageCell.java` | Requires Room schema/migrations, message lifecycle hooks, storage limits, and privacy review. Do not port database code alone. |
| Saving/forwarding logic | `com/radolyn/ayugram/AyuForwarder.java`, `com/radolyn/ayugram/easy/**`, `com/radolyn/ayugram/messages/AyuSavePreferences.java`, `com/radolyn/ayugram/exceptions/AyuPrivacyException.java` | Must be integrated with SealGram's own forwarding/media code and tested for normal messages, albums, captions, files, and error cases. Do not overwrite whole Telegram source files. |
| Message filters | `com/radolyn/ayugram/AyuFilter.java`, `com/radolyn/ayugram/ui/preferences/RegexFiltersPreferencesActivity.java`, `RegexFilterEditActivity.java`, `ui/preferences/utils/**` | Validate regex behavior and avoid blocking UI thread on expensive expressions. |
| AyuSync | `com/radolyn/ayugram/sync/**`, `AyuSyncPreferencesActivity.java` | Optional external sync; keep disabled by default and do not inherit upstream server settings or Firebase project configuration. See SECURITY_AUDIT_STAGE1.md. |
| ExteraGram UI/preferences | `com/exteragram/messenger/preferences/**`, `components/**`, `icons/**`, `utils/**` | Broad dependency group, not all of it is AyuGram-specific. Port only needed components after dependency and license review. |
| App updater | `com/exteragram/messenger/utils/UpdaterUtils.java`, `preferences/updater/**` | Do not retain the upstream update repository URL. A SealGram2 updater would need its own endpoint and APK signature verification. |
| Visual assets | `.github/AyuGram.png`, `.github/demos/demo1.png` through `demo6.png`, `TMessagesProj/src/main/res/drawable-hdpi/ghost.png`, `drawable-mdpi/ghost.png`, `drawable-xhdpi/ghost.png`, `drawable-xxhdpi/ghost.png`, `TMessagesProj/src/main/res/drawable/ayu_ghost.xml`, `TMessagesProj/src/main/res-solar/drawable/ghost_solar.xml`, `TMessagesProj/src/main/res/raw/etg_raccoon.json` (verify exact path in tree before copying) | The README demo images are documentation assets. App resources must preserve correct density variants, XML dependencies, animation references, and upstream notices. |

## Merge order

1. Establish a clean SealGram2 source baseline and CI result.
2. Port the lowest-dependency AyuGram config/UI pieces in isolated commits.
3. Port message history database and hooks as a separate commit.
4. Port ghost mode and message filters in separate commits.
5. Port forwarding/saving integration after reviewing SealGram's current behavior.
6. Port only the required image/resource dependencies.
7. Decide separately whether any sync/updater/telemetry integrations are wanted; safe default is to omit them until explicitly configured.
8. Run Android CI after each coherent group and record exact results.

## Current limits

No AyuGram source files or binary image assets have been copied by this inventory commit. No local Android build has run. The repository connector can create GitHub text commits, but it is not a substitute for a local checkout and full Gradle/NDK build; do not treat this inventory as a completed merge.

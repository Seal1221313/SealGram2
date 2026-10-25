# SealGram2 privacy audit — Stage 2

Date: 2026-10-25

## Changes applied to `main`

- Disabled Firebase Analytics and Crashlytics collection in the manifest and app defaults; removed the custom startup `stats` event call.
- Removed the upstream AyuGram `TMessagesProj/google-services.json` file, which referenced Firebase project `ayugram-c6a31`.
- Removed the Google Services and Firebase Crashlytics Gradle plugins, Firebase Messaging dependency, and Google Services plugin classpath.
- Removed the Firebase messaging service implementation and its service declarations from debug/release flavor manifests. Removed its unused Firebase import.
- AyuSync is hard-disabled at its controller entry point. The normal application flow installs a no-op controller and closes any existing AyuSync WebSocket rather than connecting to a sync server.
- AyuSync configuration now ignores saved server/token values and returns only loopback URLs (`127.0.0.1:1`) with an empty token. WebSocket creation immediately returns without connecting. The settings toggle cannot re-enable sync.
- Inspected AyuSync payload code: it can send device name, a device identifier, account user ID, dialog IDs and read/unread state, plus an AyuSync authorization token. I did not find the phone number in the inspected AyuSync request payloads; this is not proof that no other code path handles phone data.
- Disabled automatic external update checks at app launch. Manual update checks remain available; the request uses the generic `SealGram-Update-Checker` user agent instead of a randomized device/browser fingerprint.
- Disabled cleartext HTTP traffic in the main manifest.

## Confirmed upstream data collection/transmission paths

- The former custom Analytics `stats` event included Android/app versions, manufacturer/model, performance class, locale, cache path, refresh rate/display dimensions, Play Services availability, and beta/debug status. Its startup call and Firebase analytics/crash-report build integration have been removed/disabled.
- AyuSync code can transmit a device name and device identifier during registration, plus account/dialog read-state information and an authorization token. The controller is now hard-disabled in the normal app flow.
- The upstream Firebase configuration identified the AyuGram Firebase project and has already been removed from the current tree. No `google-services.json` exists at the repository root or `TMessagesProj/` path. Firebase Messaging wiring was removed in prior privacy changes, and the remaining Firebase messaging service declaration in the base manifest has now been removed.

## Functionality trade-offs

- **Push notifications:** removing Firebase Messaging/its service and upstream Firebase config means FCM push delivery is no longer available in this build. Telegram may need to reconnect more often while the app is backgrounded, and notifications may arrive late or only after the app wakes. This is intentional privacy hardening, not a verified build result. No Android build has been run after the latest AyuSync edits.
- **Manual update checks:** still contact GitHub when explicitly requested; GitHub will still see the network request and IP address. Automatic launch checks are removed.
- **Google Translate:** when a user explicitly invokes translation, selected text is sent to Google's translation endpoint.
- **Telegram API/MTProto:** the app must still contact Telegram servers for login, messaging, media and account features. This is normal service traffic, not proof of selling data.
- Maps, billing, location, authentication and other Google Play services/dependencies remain where used by app features; they need separate feature-by-feature review if the goal is to remove all third-party Google services.

## What is and is not established

The reviewed client source contained data collection and sync paths described above. **I found no client-code evidence proving that AyuGram sells user data.** This audit cannot inspect server-side systems or the separately permissioned proprietary submodule that was not available in the imported public source.

No Android build or clean-install network capture has been run after these commits. Therefore compilation and runtime behavior are not yet verified. Before installing, build the intended variant and inspect the merged manifest/dependency graph; then test login, background notifications, media, calls and manual update checks.

## Stage 3

- AyuSync removed entirely: `com.radolyn.ayugram.sync` package, `AyuSyncPreferencesActivity`, all hooks (ApplicationLoader, ConnectionsManager read-sync, settings rows), config getters and constants.
- Removed device-identifier (ANDROID_ID) and device-name helpers that existed only for sync registration.
- Removed Crashlytics/Analytics toggles and config fields from Other settings.
- Removed leftover Google Analytics (AppMeasurement) services/receiver from the manifest.
- Full sweep found no hidden modules: no dynamic code loading, no encoded strings/IPs in Java or native code; only network users are Telegram MTProto, DNS fallback (Google/Mozilla), GitHub update check, Google Translate on request, optional map tiles.

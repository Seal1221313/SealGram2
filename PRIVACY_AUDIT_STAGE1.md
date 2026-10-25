# SealGram2 privacy audit — Stage 1

Date: 2026-10-25

## Changes applied

- Removed Firebase Analytics and Crashlytics initialization and their direct dependencies/plugins.
- Deleted `CrashlyticsUtils.java`, which collected and submitted a `stats` event.
- Removed the startup call that initialized analytics/crash reporting.
- Removed explicit Firebase Remote Config and Firebase App Indexing dependencies; the project did not need these for the reviewed flows.
- Disabled AyuSync at its controller entry point. The controller now always installs the no-op implementation and closes any existing AyuSync WebSocket rather than connecting to a sync server.
- Changed the default AyuSync server value to `localhost` as a defensive fallback.
- Changed the updater's GitHub repository from AyuGram to `Seal1221313/SealGram2`, so update checks no longer query AyuGram's releases endpoint.
- Kept Firebase Messaging / the Google Services plugin because Telegram push notifications use them. The current `google-services.json` still references Firebase project `ayugram-c6a31`; this is a remaining upstream dependency, not evidence that private messages are being sold. Replacing/removing it without a project owned by this fork would disable or break FCM push notifications.

## Confirmed data transmission found

AyuSync is a real optional synchronization feature, not proof of data sale. When enabled, its client code can send:
- a device name and device identifier to the configured sync server during device registration;
- a Telegram user ID and dialog read/unread state information through its synchronization protocol;
- an authorization token and package/device headers on requests.

AyuSync was already configured disabled by default, but preferences could enable it. Its controller is now hard-disabled, so this client no longer initiates those sync requests through the normal application flow.

## Analytics and crash reporting

The previous `CrashlyticsUtils.logEvents()` sent an event named `stats` containing Android/app versions, manufacturer/model, performance class, locale, cache path, screen refresh rate/dimensions, Play Services availability, and beta/debug status. That helper and direct Analytics/Crashlytics dependencies have been removed.

## Remaining network activity

- Telegram MTProto/API requests are required for account login, messages, media, calls, and push registration.
- Firebase Messaging remains for push notifications. The repository's Google Services configuration still names the upstream AyuGram Firebase project. This should be replaced with a Firebase project controlled by the SealGram maintainer if push notifications are to remain enabled without that upstream project association.
- Google Translate receives text a user explicitly asks the app to translate.
- Maps, authentication, billing, language identification, and other Google Play services remain for their associated features.
- The updater now checks this fork's GitHub releases instead of AyuGram's releases.

## Limits of this audit

No evidence in the reviewed client code proves that AyuGram sells user data. The code does contain optional sync traffic described above, which is now disabled. This audit cannot inspect the server-side backend or the separately permissioned proprietary submodule that was not included in the imported public source. A full assurance check still requires an Android build, merged-manifest/dependency review, and network capture on a clean install.

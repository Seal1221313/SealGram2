# SealGram2 privacy audit — Stage 1

Date: 2026-10-25

## Changes applied

- Firebase Analytics collection is disabled in the Android manifest and at runtime.
- Firebase Crashlytics collection is disabled in the manifest and at runtime; Crashlytics collection is also deactivated.
- The startup device/build statistics event from `CrashlyticsUtils.logEvents()` is no longer called.
- The default preferences for Google Analytics and Crashlytics are now false.
- Cleartext HTTP traffic is disabled in the Android manifest. AyuSync now defaults to TLS (`wss://` / `https://`) for new installations.

## Confirmed data collection removed/disabled

The code previously sent a Firebase Analytics event named `stats` containing Android version, app version/code, manufacturer/model, performance class, locale, cache directory path, display size/refresh rate, Play Services availability, and beta/debug status. That event call is no longer made and Firebase analytics collection is explicitly disabled.

Crashlytics report collection is explicitly disabled. The Firebase SDK dependencies remain in the project because Firebase Messaging is used by the push-notification provider; removing the entire Firebase stack could break push notifications.

## Remaining network activity that is not classified as analytics tracking

- Telegram protocol/API traffic is required for account login, messages, media, calls, and notifications.
- Firebase Cloud Messaging may be used for push notifications; the app registers its push token through the Telegram push flow.
- Google Translate receives the text a user explicitly asks the app to translate. Avoid using in-app translation for confidential text if you do not want it sent to Google's translation endpoint.
- The AyuSync feature is disabled by default (`syncEnabled=false`) but remains in the source as an optional feature. If enabled, it contacts the configured sync server and sends a device identifier/package name and sync/account-related data. Review the AyuSync settings before enabling it.
- The updater checks GitHub release metadata when its update-check feature runs. This exposes a normal request to GitHub (including network IP); this is not a unique device telemetry event. Update downloads should only be accepted from a trusted release source.
- Maps, billing, authentication, language identification, and other Google Play services are present for their corresponding features; this audit did not remove feature dependencies without tracing every call site.

## Not claimed

This is a targeted source audit, not a proof that the compiled APK contains zero tracking or makes zero external requests. A full guarantee requires building the APK, reviewing the merged manifest/dependency graph, and testing network traffic on a clean install. No build was run as part of this stage.

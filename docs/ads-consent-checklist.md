# Ads and consent verification

## Implementation

- Debug overrides only the banner unit ID with Google's demo banner unit; release keeps the production ID.
- Both builds retain the real AdMob app ID so UMP can retrieve the app's published privacy message.
- Main, Vocabulary and Verbs use AdsConsentController, which gates ad initialization/loading on UMP canRequestAds().
- Premium and TV skip the ad/consent controller in the existing activity flow.
- Privacy options appear in each advertising screen's overflow menu when UMP requires them.
- Changing choices destroys the current banner and recreates the screen. Other existing advertising screens recreate when resumed after the change.
- Consent errors do not override canRequestAds(); a valid previous decision may still permit requests.
- No forced geography, consent reset, or test-device identifiers are included in release.

## Before publishing

Local verification (2026-09-26): debug/release builds and release lint passed
(0 errors, 96 warnings). Pixel 5 displayed a demo banner on Main, Vocabulary and Verbs,
with no AndroidRuntime crash logged during navigation.
UMP reported that no privacy form was configured for this app ID. Full consent
choice testing therefore remains pending. UMP may allow requests when no message
is configured; the integration is not a substitute for publishing the AdMob message.

1. Publish the app's message under AdMob Privacy & messaging, including the correct privacy policy URL.
2. On a test installation, verify first-run consent, acceptance, refusal/manage options, reopening privacy options, and persistence after restart. Refusal does not necessarily mean no ads: UMP and the configured choices determine eligible requests.
3. Verify offline first launch and offline launch with a previous consent decision; dictionary functionality must remain available.
4. Navigate between all three advertising screens after changing choices; check for duplicate requests or stale banners.
5. Verify rotation/backgrounding while a consent form is open, and Premium purchase/restore while ad initialization is pending.
6. Confirm debug banners are labelled as test ads. Do not click production ads during testing.
7. Verify Premium and TV have no banners. Check phone and tablet separately.
8. Review the app's own statistics, privacy policy and Play Data safety declarations separately; UMP does not cover the app's own analytics automatically.

Reference: https://developers.google.com/admob/android/privacy

## Deferred visual checks

- Vocabulary and Verbs at larger system font sizes (explicitly postponed by the user).
- History and tablet layout checks remain to be completed.

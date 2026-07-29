# Privacy Policy for TaigaMobileNova (Google Play version)

**Last Updated:** July 29, 2026

## Introduction

TaigaMobileNova is an unofficial Android client for Taiga.io. This privacy policy explains how the Google Play version of the app handles your information. This version includes Firebase Crashlytics for crash reporting; the F-Droid version does not and is covered by a separate [privacy policy](PRIVACY_POLICY.md).

## Data Collection

**We do not collect, store, or transmit any personal data to our own servers.** TaigaMobileNova does not have any first-party analytics or tracking systems.

This version of the app uses **Firebase Crashlytics** to automatically collect crash and error diagnostics, which helps us find and fix bugs. See the [Firebase Crashlytics](#firebase-crashlytics) section below for details, including how to turn it off.

## Data Usage

### Taiga.io Authentication
When you log in to your Taiga.io account through the app:
- **We do not store your username or password**
- Authentication tokens received from Taiga.io are stored **locally on your device only**
- Tokens are used solely to authenticate with your Taiga.io server
- All communication occurs directly between your device and your chosen Taiga.io server

### Local Storage
The app stores data locally on your device to provide functionality, including:
- Authentication tokens
- Cached project data, tasks, user stories, and other Taiga.io content
- App preferences and settings

This data remains on your device and is never transmitted to us.

## Third-Party Services

### Taiga.io
TaigaMobileNova connects directly to Taiga.io servers (official or self-hosted instances). When using the app:
- Your data is transmitted directly to and from Taiga.io servers
- Taiga.io's privacy policy governs how your data is handled on their servers
- We recommend reviewing Taiga.io's privacy policy at their official website

### Firebase Crashlytics

This version of the app uses [Firebase Crashlytics](https://firebase.google.com/support/privacy) (a Google service) to collect crash reports and diagnostic information when the app crashes or encounters an error. This helps us identify and fix bugs.

Data collected by Crashlytics may include:
- Crash and exception stack traces
- Device model, OS version, and app version
- Approximate crash timestamps
- An installation identifier that is not linked to your Taiga.io account or credentials

Crashlytics does **not** receive your Taiga.io login details, authentication tokens, or the content of your projects, tasks, or messages.

This data is processed by Google in accordance with [Google's Privacy Policy](https://policies.google.com/privacy) and the [Firebase Crashlytics privacy documentation](https://firebase.google.com/support/privacy).

**You can disable crash reporting at any time** in the app under Settings → Interface → Privacy. Turning it off stops new crash data from being sent.

## Permissions

The app requires the following Android permissions:

- **INTERNET**: Required to communicate with Taiga.io servers and, if enabled, to send crash reports to Firebase Crashlytics
- **ACCESS_NETWORK_STATE**: Used to check if your device has an active internet connection

## Data Security

- All locally stored data uses Android's secure storage mechanisms
- Communications with Taiga.io servers use HTTPS encryption (when supported by your server)
- We do not have access to your device data or Taiga.io credentials

## Data Backup

Android's automatic backup feature may back up app data to your Google Drive account. You can disable this in your device's Android settings if desired.

## Children's Privacy

TaigaMobileNova does not knowingly collect any information from children. The app is a productivity tool intended for users of the Taiga.io project management system.

## Changes to This Policy

We may update this privacy policy from time to time. Changes will be reflected in the app repository and in app store listings. Continued use of the app after changes constitutes acceptance of the updated policy.

## Open Source

TaigaMobileNova is open-source software. You can review the source code at https://github.com/Grigoriym/TaigaMobileNova to verify our privacy practices.

## Contact

For questions about this privacy policy or the app, please:
- Open an issue on the GitHub repository at https://github.com/Grigoriym/TaigaMobileNova
- Contact us at grappimapps@gmail.com

## Your Rights

Since we do not collect any data ourselves:
- There is no data held by us to delete, modify, or export
- Crash diagnostics sent to Firebase Crashlytics can be stopped at any time via the in-app toggle described above; previously submitted crash data is automatically deleted by Firebase within 90 days (see [Firebase's data retention policy](https://firebase.google.com/support/privacy))
- All your other data resides locally on your device and on Taiga.io servers
- You have full control over your local data through your device settings

You can delete all app data by:
1. Uninstalling the app, or
2. Clearing the app's data in Android Settings → Apps → TaigaMobileNova → Storage → Clear Data

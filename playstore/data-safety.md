# Play Console Data Safety Form — Reference

Answers for the Gplay flavor's Data Safety section (App content → Privacy → Data safety),
current as of the Firebase Crashlytics addition. Re-check this whenever a new SDK or data flow
is added — each answer below traces to something in the codebase or `PRIVACY_POLICY_GPLAY.md`.

## Data collection and security

- **Does your app collect or share any of the required user data types?** Yes
- **Is all user data collected encrypted in transit?** No — self-hosted Taiga servers can be
  configured over plain `http://`. `androidApp/src/main/AndroidManifest.xml` sets
  `usesCleartextTraffic="true"`, there's no `network_security_config.xml`, and
  `LoginViewModel.SERVER_REGEX` accepts both `http` and `https` schemes (a warning dialog is
  shown but the user can proceed). Only Crashlytics traffic to Firebase is guaranteed HTTPS.
- **Account creation methods:** "My app does not allow users to create an account" — the
  `feature/login` module only authenticates against a pre-existing Taiga account (username/password,
  LDAP, or GitHub OAuth via `AuthApi.kt`). There is no sign-up/registration flow in the app.
  - **Can users log in with accounts created outside the app?** Yes
  - **How are these accounts created?** Other — accounts are created directly on Taiga.io or a
    self-hosted Taiga server via that service's own registration process; the app never creates them.
  - **Deletion request for account/auth data?** Yes — auth tokens are stored locally only
    (`PRIVACY_POLICY_GPLAY.md`, "We do not store your username or password"); nothing is held
    server-side by us to begin with, so logging out / clearing app data is immediate and complete.
- **Overall "do you provide a way for users to request data deletion?"** No, but user data is
  automatically deleted within 90 days — this is the weakest-link answer because Crashlytics data
  has no on-demand deletion path (see below), even though account data is a non-issue (see above).
- **Additional badges** (Independent security review / UPI): Not applicable, leave unchecked.

## Data types collected

Only two sections need entries — everything else (Location, Financial info, Health and fitness,
Messages, Photos and videos, Audio files, Files and docs, Calendar, Contacts, Web browsing, App
activity) is 0, since the app only holds `INTERNET` + `ACCESS_NETWORK_STATE` permissions and has
no SDK collecting those categories.

- **App info and performance** → check *Crash logs* and *Diagnostics*
- **Device or other IDs** → check *Device or other IDs* (Crashlytics installation identifier)

Judgment call, not selected today but worth revisiting: **Personal info → User IDs** for the Taiga
username transmitted off-device to the user's own Taiga server during login. Left unchecked on the
reasoning that this is core "bring your own server" functionality (like an email/SSH client), not
data collection for our own purposes — but Play's literal "collected = transmitted off device"
definition could support checking it. Revisit if Play flags this on review.

GitHub OAuth login does **not** need separate disclosure — it opens the real `github.com` page in
a WebView, which falls under Play's "navigating the open web" exemption.

## Per-data-type answers (Crash logs, Diagnostics, Device or other IDs)

All three get the same answers, since they're all part of the same Crashlytics flow:

- **Collected or shared?** Collected only — Firebase acts as a **service provider** under its Data
  Processing Terms (processes data solely on our behalf, for the purpose we specified), which
  Play's definition of "sharing" explicitly excludes. No separate Analytics/data-sharing settings
  are enabled in this app's Firebase config.
- **Processed ephemerally?** No — Crashlytics persists data (~90-day retention), it's not
  in-memory-only for a single real-time request.
- **Required or optional?** Users can choose whether it's collected (shown as optional on the
  store listing) — matches the Settings → Interface → Privacy toggle, which calls
  `crashlytics.isCrashlyticsCollectionEnabled = enabled` (`CrashReporterImpl.kt`). Note this toggle
  only stops *future* collection; it does not delete previously submitted data (no
  `FirebaseInstallations.delete()` call exists in the code) — that's why the 90-day auto-deletion
  answer above matters.
- **Why collected?** Analytics only — "diagnose and fix bugs or crashes, make future performance
  improvements." Not App functionality, Developer communications, Advertising, Fraud/security,
  Personalization, or Account management.

## Related files

- `PRIVACY_POLICY_GPLAY.md` — public policy text, must stay in sync with the answers above
- `PRIVACY_POLICY.md` — F-Droid version (no Crashlytics, no Data Safety form to fill)
- `androidApp/src/gplay/kotlin/com/grappim/taigamobile/data/CrashReporterImpl.kt` — the actual
  collection-toggle implementation referenced above
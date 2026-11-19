# Advertising compliance checklist

TrackUtang is currently ad-free. Do not add Google Mobile Ads / AdMob dependencies or ad units until every item below is complete.

## Before enabling Google ads

1. Add the current Google Mobile Ads SDK and initialize it only after consent handling.
2. Add the Google User Messaging Platform (UMP) SDK and request consent where required, including EEA, UK, and Switzerland users.
3. Add a visible **Privacy choices / Manage ad consent** item under Profile → Settings so users can reopen consent choices.
4. Update the in-app and public Privacy Policy to name Google as an ad technology provider and explain the data used for advertising.
5. Update the Google Play Data safety form. The Google Mobile Ads SDK may collect/share IP address, user interactions, diagnostics, and device/account identifiers for advertising, analytics, and fraud prevention.
6. Use only non-intrusive placements: a small banner on Home or Settings; never over debt entry, payment, due reminders, privacy text, or destructive actions.
7. Keep the app’s audience declaration, content rating, target audience, and Families/COPPA settings accurate.
8. Test consent, ad loading, offline behavior, privacy choices, and Data safety declarations on the final release build.

## Current disclosure

This version does not include an advertising SDK and does not show live ads. The reserved Home/Settings ad area must remain a design placeholder until the requirements above are complete.

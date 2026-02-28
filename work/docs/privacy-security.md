# SafeHer AR — Privacy & Safety Review

## 1. Data Collection & Privacy

### What Data is Collected?
- **Location Data (GPS):** The app requests `ACCESS_FINE_LOCATION` to populate the map and position AR anchors relative to the user.
- **Reporting Data:** When creating an anchor, the app transmits:
  - Latitude and Longitude
  - Category (e.g., Lighting, Harassment)
  - Severity (LOW, MEDIUM, HIGH, URGENT)
  - Free-text description (up to 300 words)
- **Authentication:** Firebase Auth is required to post reports, collecting an anonymous UID or email (depending on the provider configured).

### Anonymity Status
- ✅ **Public Anonymity:** When an anchor is fetched and displayed to the community in AR or on the Heatmap, the reporter’s identity (Name, Email, Profile Picture) is **NOT** included in the `AnchorData` payload.
- ✅ **Abstract Profiles:** The impact dashboard tallies votes and successful reports via a random UUID, preserving user anonymity while gamifying safety.

## 2. Recommended Safety Improvements (ROADMAP)

While the app functions anonymously, the following security optimizations are **RECOMMENDED** before a wide production release:

1. **Geomasking (Fuzzy Locations):**
   - *Issue:* Storing exact 6-decimal lat/lon coordinates for every report can theoretically be used to trace the path or routines of frequent reporters.
   - *Fix:* Round coordinates or bucket locations into a Geohash grid (e.g., Level 7: ~153mx153m) before saving reports that aren't marked `URGENT`.

2. **Rate Limiting & Abuse Prevention:**
   - *Issue:* A malicious actor could spam fake reports to alter the heatmap.
   - *Fix:* Implement Firebase App Check and a rate limiter (e.g., max 3 reports per hour per UID).

3. **PII Stripping in Descriptions:**
   - *Issue:* Users might accidentally include Personal Identifiable Information (names, vehicle plates) in their free-text description.
   - *Fix:* Implement an on-device ML model or Cloud Function to redact likely PII strings (`***`) before database insertion.

4. **SOS Button Safeguards:**
   - Ensure the SOS button (pinned in `ARDetailSheet.kt`) features a 3-second hold or swipe-to-confirm to prevent accidental emergency dispatch.

## 3. Storage & Transmission Security
- All traffic between the app and Firebase Firestore is encrypted via TLS by default.
- Data is stored in Google Cloud data centers governed by standard GCP compliance.
- Missing configuration: Ensure Firestore Rules strictly block `update` or `delete` operations by anyone other than the document owner.

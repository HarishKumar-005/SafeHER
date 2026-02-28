# SafeHer AR — Features Spec

## 1. AR Interactive View (Inline Labels)
* **Purpose:** Allows users to view real-world risks directly overlaid on the camera feed without glancing down at 2D maps.
* **Implementing Files:** `ARViewScreen.kt`, `ARInlineLabel` (Private Composable).
* **User Flow:** User enters AR tab → Camera launches → Device compass establishes heading → Nearby nodes are fetched matching the bearing → Labels fade in and scale relative to distance. Tap a label to pop up full details.
* **Tech Detail:** Utilizes 2D screen-space math instead of full 3D ARCore meshes. Uses `CameraX` for the background and `SensorManager` to fetch azimuth layout metrics. Employs a radial-push collision algorithm for uncluttered views.

## 2. Dynamic Detail Sheet
* **Purpose:** Exposes long-form context about an AR anchor (up to 300 words) and immediate action items.
* **Implementing Files:** `ARDetailSheet.kt`
* **User Flow:** User taps an `ARInlineLabel` → A bottom sheet slides up covering 60% of the viewport (draggable to 85%) → User can read details, check Risk Pilling, tap "Read Aloud", or trigger SOS/Nav.
* **Tech Detail:** Compose `AnimatedVisibility` for 200ms slide ins. Integrates directly with standard Android TTS APIs to read contents dynamically.

## 3. High-Risk Auto-Alerts (TTS / Haptics)
* **Purpose:** Eyes-free alerting mechanism. If a user has their phone by their side and a severe risk approaches, they are notified instantly.
* **Implementing Files:** `ARViewScreen.kt` (LaunchedEffect triggers).
* **User Flow:** User walks within 200m of an `URGENT/HIGH` anchor → Phone issues a distinct LongPress haptic pulse → TTS reads: "Caution. [Headline]. [Distance] meters ahead."
* **Tech Detail:** State tracks `spokenAlertIds` to prevent repeating alerts during the same session. Gracefully degrades if `ttsReady` is false.

## 4. Anonymous Reporting Flow
* **Purpose:** Crowdsource hyper-local data efficiently without putting reporters at risk of identification.
* **Implementing Files:** (INFERRED) `PostCreationARScreen.kt` or `PostIssueScreen.kt`.
* **User Flow:** User hits Quick '+Fab' → Selects category (Harassment, Lighting, Unsafe Area) → Adjusts pin on mini-map → Submits anonymously.
* **Tech Detail:** Integrates with `Firebase Firestore` generating a random document UUID. No user PII is bundled with the `AnchorData` payload.

## 5. Impact Dashboard
* **Purpose:** Gamification and community reinforcement. Shows the user the real-world value of their contributions.
* **Implementing Files:** (INFERRED) `ImpactDashboardScreen.kt`, `OverallStatsCard.kt`.
* **User Flow:** User navigates to Profile/Impact tab → Views "Total Issues Verified", "Areas Secured," etc.
* **Tech Detail:** Queries Firestore for reports tagged to the local user's anonymous device ID and sums their upvotes/validations.

---

### Known Limitations & Ideal Improvements
1. **AR Drift:** Pure magnetometer/accelerometer based AR can drift. An ideal improvement would be fusing ARCore environmental tracking with the lightweight CameraX label overlay for perfect world-space lock.
2. **Offline Mode:** Currently requires active internet to query Firestore. Implementing offline Room DB caching per neighborhood grid would be ideal.

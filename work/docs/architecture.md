# SafeHer AR — Architecture & Dataflow

## Core Architecture
SafeHer AR employs a modern Android architecture based on **Kotlin Multiplatform (Android module)** and **Jetpack Compose** (Material 3). It utilizes an **MVVM (Model-View-ViewModel)** design pattern.

### Components
1. **View Layer (Compose UI)**
   - Responsible strictly for rendering state to the screen. 
   - `ARViewScreen.kt`: Handles the CameraX preview and floats 2D Compose elements over the camera based on math. Avoids heavy 3D rendering engines like ARCore for simple floating safety labels.
2. **ViewModel Layer**
   - e.g., `MainViewModel.kt` (Inferred). Exposes state via `StateFlow` or `LiveData`.
   - Fetches device location, calculates active bounding boxes, requests Firestore queries, and manages TTS state.
3. **Data/Repository Layer**
   - Connects to Firebase Firestore. Retrieves the `AnchorData` collection based on a geo-bbox constraint around the user's current GPS location.

## Data Flow (Incident Reporting to AR render)
1. **Creation:** User triggers "Post Issue" → UI collects Category, Severity, Map Pin (Lat/Lon) → Generates a unique UUID and abstract payload.
2. **Upload:** Payload is written to `Firestore /anchors` collection.
3. **Retrieval:** As User A walks, `FusedLocationProviderClient` updates their position. `MainViewModel` queries Firestore for any anchors within ~200m radius of `userLat/userLon`.
4. **AR Maths:** 
   - Uses `SensorManager` (Magnetometer + Accelerometer) to determine the device compass bearing (0-360°).
   - Uses the haversine formula to compute the bearing between `UserLocation` -> `AnchorLocation`.
   - `ARViewScreen.kt` calculates the angular difference `(anchorBearing - deviceHeading)`.
   - If within `±60°` (camera FOV), it draws `ARInlineLabel` shifted linearly across the X-axis (normalized `[-1, 1]`) and scales it inversely to distance.

## AR Anchor Lifecycle
* **Creation:** Persistent in Firestore (timestamps tracked).
* **Display:** Transient in UI. Re-rendered frame-by-frame based on the `deviceHeading` StateFlow. 
* **Validation:** Highly-voted anchors stay prominent; old anchors with negative votes decay via `RiskScoring` algorithm.

## Timing & Caching Notes
* **Sensor Throttling (Inferred):** Device compass updates occur 60+ times per second. `ARViewScreen` throttles recomposition to prevent jank.
* **Network Caching:** Firestore SDK automatically maintains offline persistence cache of recently fetched sectors.

## Security Model Overview
* **Authentication:** Firebase Auth handles Identity. Users are verified but their display names/emails are stripped from the spatial payload.
* **Read Access:** Anyone with the app can read anchors within their viewport. 
* **Write Access:** Requires active Firebase Session.

### Known Missing Security Controls (RECOMMENDED)
1. **Geomasking Analytics:** Currently raw lat/lon is used. We recommend implementing Geohash-based fuzzy querying for broad risk areas to prevent tracing specific reporter locations.
2. **Rate Limiting / Abuse Protection:** Firebase App Check is recommended but missing from the basic setup.

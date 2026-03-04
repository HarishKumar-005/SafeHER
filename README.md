# SafeHer AR
### Real-World Safety Visibility for Women

SafeHer AR is a lightweight, sensor-driven augmented reality platform that projects community-verified safety markers directly into the user’s camera view.

It replaces map-based hazard browsing with spatial overlays that preserve situational awareness while delivering contextual, actionable safety intelligence.

---

## Problem

Women navigating urban or unfamiliar environments frequently rely on 2D navigation tools that require downward attention. This interaction reduces environmental awareness at moments when alertness is critical.

Additionally, traditional mapping platforms lack hyper-local, real-time safety context such as harassment zones, poorly lit streets, isolated walkways, or emergency resource gaps.

---

## Solution

SafeHer AR bridges digital safety data and physical space.

Using device heading, GPS position, and bearing calculations, safety anchors are projected into the camera view in real time.

Each marker is:
- Spatially anchored via ARCore
- Distance-aware  
- Severity-coded  
- Collision-adjusted  
- Context-rich (supports long-form descriptions)  

High-severity anchors can trigger optional Text-to-Speech and haptic alerts, enabling hands-free hazard awareness.

---

## 4-Line Problem Frame

- **User:** Women navigating urban environments, especially during night travel or solo transit.
- **Problem:** Reduced situational awareness caused by downward-focused navigation and absence of localized safety intelligence.
- **Constraints:** Must run on commodity Android hardware, preserve anonymity, minimize battery consumption, and remain legible in varied lighting conditions.
- **Success Test:** A user identifies a high-risk anchor within 3 seconds in AR view and receives an optional audible/haptic alert without breaking visual awareness.

---

## 3-Line Pitch

**See risk before you reach it.**  
SafeHer AR projects verified safety alerts directly into your real-world view using lightweight augmented reality.  
Move confidently.

---

## Core Capabilities

### ARCore Spatial Overlay
- ARCore SceneView for environment understanding
- High-precision plane detection and spatial anchoring
- Haversine distance mapped to 3D world space
- Stable augmented reality markers that lock to real-world environments

### Collision-Aware Rendering
- Radial push algorithm prevents overlap
- Distance-based scaling hierarchy
- Performance-stable under dense anchor clustering

### Contextual Detail System
- Expandable bottom sheet
- Long-form context (~300 words)
- Severity indicators and timestamps
- Action triggers (SOS / Navigation)

### Anonymous Reporting
- UUID anchor creation
- No public storage of personal identifiers
- Firebase Auth write validation
- Firestore real-time sync

### Hands-Free Risk Alerts
- Android TextToSpeech integration
- Haptic feedback triggers
- Configurable proximity thresholds

### Safety Heatmap & Impact Metrics
- Geospatial density visualization
- Community engagement insights
- Risk normalization scoring

---

## Architecture Overview

SafeHer AR follows a modern MVVM architecture using Jetpack Compose.

### System Flow

```text
User Device
├── ARCore (Spatial Anchoring & SceneView)
├── Environmental Mapping
├── FusedLocationProvider (GPS)
│
▼
ARViewScreen
├── Bearing + Distance Calculations
├── Collision Adjustment
├── Distance Scaling
│
▼
ViewModel (StateFlow)
│
▼
Firestore (Anchors)
└── Firebase Auth (Validation)
```

**Key Logic Modules:**
- `ARViewScreen.kt` — AR projection & collision logic  
- `RiskScoring.kt` — Anchor prioritization  
- `PostCreationARScreen.kt` — Reporting flow  
- `ARDetailSheet.kt` — Context interface  

---

## Technical Stack

- Kotlin
- Jetpack Compose (Material 3)
- Google ARCore
- CameraX
- Google Play Services Location
- Firebase Authentication
- Firebase Firestore
- Android TextToSpeech API

Dependency versions are declared in `build.gradle` files.

---

## Build & Run (Clean Start)

### Requirements
- Android Studio
- Android SDK 33+
- Physical Android device recommended

### Setup

```bash
./gradlew clean assembleDebug
./gradlew installDebug
```

**APK output:**
`app/build/outputs/apk/debug/app-debug.apk`

Core functionality does not require login for evaluation.

---

## Decision Log

- **Google ARCore for True Spatial Anchoring**
  Selected ARCore for robust environmental tracking and accurate 3D marker placement, ensuring safety alerts remain stable and locked to the real world even while moving.
- **Dynamic 3D Screen Projection**
  Ensures deeply immersive performance and contextual depth perception on supported hardware.
- **Custom Collision Algorithm**
  Prevents label stacking in dense clusters, maintaining legibility.
- **Light Translucent AR Cards**
  Selected for consistent contrast against dynamic camera backgrounds.
- **Anonymous Anchor Model**
  Eliminates exposure of personally identifiable data.

---

## Risk Log

| Risk | Impact | Mitigation |
|---|---|---|
| Sensor drift | Marker misalignment | Heading smoothing + recalibration |
| False reporting | Data integrity | Vote decay + validation rules |
| Visual clutter | Reduced clarity | Collision push algorithm |

---

## Evidence & Research Basis

| Source | Relevance |
|---|---|
| UN Women – Safe Cities Initiative | Highlights mobility safety gaps affecting women globally |
| National Institute of Justice – Situational Awareness Studies | Demonstrates risk associated with attention diversion in public spaces |
| Android Sensor Documentation | Confirms heading accuracy limitations and mitigation techniques |
| Human-Computer Interaction Research on AR Overlays | Supports spatial cue integration improving contextual awareness |

---

## Accessibility & Integrity

- Optional Text-to-Speech alerts
- Haptic reinforcement
- Compose semantic labeling
- No public PII storage
- No exposed secrets in repository

---

## Known Limitations & Future Work

- Requires ARCore-compatible hardware for full spatial tracking
- Advanced plane occlusion and mesh mapping (planned feature)
- Advanced moderation tooling planned
- Extended analytics dashboard for community administrators

---

## UN Sustainable Development Goals

- **SDG 5** — Gender Equality
- **SDG 11** — Sustainable Cities & Communities

SafeHer AR supports safer mobility through spatially contextualized safety intelligence.

---

## License

**MIT License**

---

## Devpost Submission Checklist

> **Required Links for Submission:**
- [ ] **Demo Video Link:** `[PASTE_YOUTUBE_LINK_HERE]`
- [ ] **Working Prototype (APK):** `[PASTE_DRIVE_LINK_HERE]`
- [ ] **Evidence Log:** `[PASTE_LINK_HERE]`
- [ ] **Decision Log:** `[PASTE_LINK_HERE]`

---

## Authors

- **Harish Kumar S P**
  - GitHub: [@HarishKumar-005](https://github.com/HarishKumar-005)
  - Email: [harishkumar.sp5511@gmail.com](mailto:harishkumar.sp5511@gmail.com)

- **Akshaya S**
  - GitHub: [@akshaya12406-byte](https://github.com/akshaya12406-byte)
  - Email: [akshaya12406@gmail.com](mailto:akshaya12406@gmail.com)
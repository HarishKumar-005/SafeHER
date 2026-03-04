# #75HER Challenge: Decision Log
**Project Name:** SafeHer AR
**Team Name:** SafeHer Check

## 💡 Purpose & Instructions
**Purpose:** Document your key technical choices and reasoning for judges. This helps them understand your engineering thinking.

---

## 🛠 Decision Log

| Category | Decision → Why | Tradeoff |
|---|---|---|
| Tech Stack | **Jetpack Compose** → Chosen for a reactive, declarative UI that significantly speeds up iteration compared to XML layouts. | Steeper initial learning curve and occasional compatibility issues bridging legacy Android libraries. |
| Tech Stack | **Firebase Firestore** → Selected for out-of-the-box real-time geospatial data syncing without managing active websockets. | Vendor lock-in and strict NoSQL structural constraints, making complex relational queries difficult. |
| Architecture | **ARCore Geospatial API + Cloud Anchors** → Selected over pure CameraX/SensorManager 2D projection to achieve true world-locked 3D spatial anchoring at GPS coordinates. Uses `Session`, `Earth`, `GeospatialPose`, and `createAnchor(lat, lon, altitude, quaternion)` for real-world-stable markers. | Requires ARCore-compatible hardware; higher battery consumption than a pure sensor overlay approach. |
| Architecture | **ARCore as "optional" in Manifest** → Set `android:value="optional"` for `com.google.ar.core` so the app installs on non-AR devices and gracefully degrades. | AR features silently unavailable on older unsupported hardware without user-visible explanation. |
| Architecture | **Custom Collision Algorithm (Radial Push)** → Implemented to dynamically fan out overlapping markers instead of dropping them in dense hazard zones. | Adds computational overhead on the main thread during frame rendering when >15 markers are on screen. |
| Process | **No Personally Identifiable Data (PII)** → Decided to use completely anonymous UUIDs for all hazard reports to ensure absolute user safety and privacy. | Limits the platform's ability to implement a granular user reputation system or ban specific bad actors. |
| Feature Scope | **Local Android TTS API** → Chosen over cloud-based TTS (like Google Cloud TTS) for zero-latency, offline-capable high-risk acoustic alert readouts. | Voice parsing quality and speed vary significantly depending on the user's specific device manufacturer. |
| Tech Stack | **CameraX** → Selected over the older Camera2 API for a stable, lifecycle-aware background preview stream that initializes predictably. | Less granular manual control over niche camera hardware settings like specific ISO or exposure times. |

---

*Part of the #75HER Challenge | CreateHER Fest 2026*

# #75HER Project: SafeHer AR
**One-line Value Proposition:** Women navigating urban environments get real-time, hands-free spatial safety alerts instantly under the constraint of preserving physical situational awareness.

---

## 🎯 Problem Statement
- **Who:** Women navigating urban environments, especially during night travel or solo transit.
- **Problem:** Reduced situational awareness caused by downward-focused 2D navigation tools, compounded by the absence of localized, real-time safety intelligence (e.g. poorly lit streets, harassment zones).
- **Impact:** Decreased environmental awareness at moments when alertness is critical, increasing transit vulnerability.

---

## ✨ Solution Overview
**What we built:** SafeHer AR is a lightweight, sensor-driven augmented reality platform that projects community-verified safety markers directly into the user’s camera view. It replaces traditional map-based hazard browsing with spatial overlays that preserve situational awareness while delivering contextual safety intelligence.

**Key Features:**
- **ARCore Spatial Overlay:** High-precision plane detection and spatial anchoring securely locks augmented reality markers to real-world environments so users can see risks before they reach them.
- **Hands-Free Risk Alerts:** Configurable proximity thresholds trigger optional Text-to-Speech (TTS) announcements and haptic feedback alerts, enabling hands-free hazard awareness without looking at a screen.
- **Contextual Detail System:** Expandable, collision-aware bottom sheets provide long-form incident context (~300 words), severity indicators, and immediate action triggers (Navigate/SOS).

---

## 🚀 Quick Start & Demo Path

### Installation (1 Command)
**Requirements:** Android Studio, Android SDK 33+, Physical Android device (recommended for sensor accuracy).

```bash
# Clone the repository
git clone https://github.com/HarishKumar-005/SafeHER.git && cd SafeHER

# Build and install the debug application
./gradlew clean assembleDebug && ./gradlew installDebug
```

**Access:** The app will be installed on your connected Android device. *(Ensure `google-services.json` is placed in `/app` for Firebase)*

### 60-Second Demo Path
- **Step 1:** Launch app and open AR tab → Camera activates and projects floating 3D markers for nearby hazards.
- **Step 2:** Physically walk towards a "HIGH" severity marker → App automatically vibrates and reads the hazard aloud via TTS.
- **Step 3:** Tap a marker → Bottom sheet expands providing full incident context and an SOS button.

📹 **Demo Video:** `[Insert Link]` | 🔗 **Live Demo (APK):** [Download SafeHER-AR.apk](https://github.com/HarishKumar-005/SafeHER/releases/download/v1.0.0/SafeHER-AR.apk)

---

## 🏗️ Technical Architecture

**Components:**
- **Frontend:** Jetpack Compose (Material 3) — Declarative, StateFlow-based reactive UI.
- **Backend / Database:** Firebase Firestore & Auth — Real-time spatial geohash bounding queries and anonymous write validation.
- **AR Engine:** Google ARCore + CameraX + SensorManager — High-precision plane detection and bearing difference mapping.
- **AI/ML Integration:** (Optional track / not deeply focused, uses device-side risk scoring normalization).

---

## 📋 Project Logs & Documentation

| Log Type | Purpose | Link to Documentation |
|---|---|---|
| **Decision Log** | Technical choices & tradeoffs (e.g. ARCore integration vs 2D Projection). | [docs/DECISION_LOG.md](https://github.com/HarishKumar-005/SafeHER/blob/main/docs/DECISION_LOG.md) |
| **Risk Log** | Issues caught & mitigated (e.g. Sensor drift fixed by smoothing recalibration/Collision algorithm). | [docs/RISK_LOG.md](https://github.com/HarishKumar-005/SafeHER/blob/main/docs/RISK_LOG.md) |
| **Evidence Log** | Sources (UN Women Safe Cities), assets, & attributions. | [docs/EVIDENCE_LOG.md](https://github.com/HarishKumar-005/SafeHER/blob/main/docs/EVIDENCE_LOG.md) |

---

## 🧪 Testing & Known Issues

- **Test Results:** Unit tests passing (RiskScoring/State).
- **Known Issue:** Compass interference can occur in high electromagnetic environments, causing slight marker misalignment.
- **Next Step:** Advanced plane occlusion and mesh mapping using newer ARCore Depth APIs to realistically hide markers behind physical buildings.

---

## 👥 Team & Acknowledgments

**Team Name:** SafeHer Check

| Name | Role | GitHub | LinkedIn |
|---|---|---|---|
| **Harish Kumar S P** | Android/AR Developer | [@HarishKumar-005](https://github.com/HarishKumar-005) | `[Profile Link]` |
| **Akshaya S** | UI/UX & Data | [@akshaya12406-byte](https://github.com/akshaya12406-byte) | `[Profile Link]` |

**Special thanks to:** CreateHER Fest, #75HER Mentors, and Google for Android.

---

## 📄 License & Attributions

**Project License:** MIT License

- **Google Play Services Location:** Apache 2.0 | [Link](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary)
- **Google ARCore:** Apache 2.0 | [Link](https://developers.google.com/ar)
- **Firebase:** Apache 2.0 | [Link](https://firebase.google.com/)
- **Google Fonts (Poppins, Inter):** SIL Open Font License | [Link](https://fonts.google.com/)
- **Material Icons:** Apache 2.0 | [Link](https://fonts.google.com/icons)

*Built with ❤️ for #75HER Challenge | CreateHER Fest 2026.*
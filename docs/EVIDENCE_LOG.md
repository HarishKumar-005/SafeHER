# #75HER Challenge: Evidence Log
**Project Name:** SafeHER AR  
**Team Name:** SafeHer Check

---

## 💡 Purpose
Track ALL sources, datasets, and assets used in the project to prove credibility and proper attribution.

---

## 📊 Evidence Log Table

| Item / Claim | Purpose in Project | Source Link | Type | License / Attribution | Notes |
|---|---|---|---|---|---|
| **Google ARCore** | Core AR spatial anchoring and environment understanding engine | https://developers.google.com/ar | Code | Apache 2.0 | Used for SceneView, plane detection, and spatial marker locking |
| **Firebase Firestore** | Real-time cloud datastore for safety anchors and community reports | https://firebase.google.com/docs/firestore | Code | Apache 2.0 | Geohash bounding queries for 1km spatial fetch |
| **Firebase Authentication** | Anonymous write-validation for hazard reports | https://firebase.google.com/docs/auth | Code | Apache 2.0 | Anonymous auth only — no PII collected |
| **Jetpack Compose (Material 3)** | Primary UI framework for all screens | https://developer.android.com/jetpack/compose | Code | Apache 2.0 | v1.5+ with Material 3 tokens |
| **CameraX** | Live camera preview pipeline for AR overlay | https://developer.android.com/training/camerax | Code | Apache 2.0 | Lifecycle-aware, used in `ARViewScreen.kt` |
| **Google Play Services Location (FusedLocationProvider)** | GPS positioning for anchor spatial queries | https://developers.google.com/android/reference/com/google/android/gms/location/package-summary | Code | Apache 2.0 | Fresh GPS fix in ~9s; cached reads in 46–135ms |
| **Android TextToSpeech API** | Hands-free TTS alerts for HIGH-severity anchors | https://developer.android.com/reference/android/speech/tts/TextToSpeech | Code | Android Open Source (Apache 2.0) | On-device, zero-latency, no cloud call |
| **Google Fonts — Poppins** | Heading and title typography | https://fonts.google.com/specimen/Poppins | Visual / Font | SIL Open Font License 1.1 | Used as `poppins_semi_bold.ttf` in `/res/font/` |
| **Google Fonts — Inter** | Body, labels, buttons, and card text typography | https://fonts.google.com/specimen/Inter | Visual / Font | SIL Open Font License 1.1 | Variable font used as `inter_variable.ttf` in `/res/font/` |
| **Material Icons by Google** | All in-app iconography (navigation, alerts, actions) | https://fonts.google.com/icons | Visual | Apache 2.0 | Used via `androidx.compose.material:material-icons-extended` |
| **UN Women — Safe Cities and Safe Public Spaces** | Research basis for women's urban safety gap claim | https://www.unwomen.org/en/what-we-do/ending-violence-against-women/creating-safe-public-spaces | Research | Public domain / UN publication | Supports the "mobility safety gap" claim in the Problem Statement |
| **National Institute of Justice — Situational Awareness** | Research basis for attention diversion risk claim in pedestrian safety | https://nij.ojp.gov/topics/articles/overview-situational-crime-prevention | Research | U.S. Government public domain | Supports the "downward attention reduces awareness" claim |
| **Android SensorManager Documentation** | Technical evidence for compass heading accuracy limitations and mitigation strategies | https://developer.android.com/reference/android/hardware/SensorManager | Research / Docs | Android Open Source (Apache 2.0) | Used to justify smoothing + recalibration mitigation in Risk Log |
| **Human-Computer Interaction Research on AR Overlays (Google Scholar)** | Academic basis for spatial cue integration improving contextual awareness | https://scholar.google.com/scholar?q=AR+overlay+contextual+awareness | Research | Fair Use (Academic Citation) | Supports the AR overlay design approach |

---

## 🤖 AI-Generated Content Log

| AI Tool Used | Purpose | What AI Generated | What You Changed | Verification Method |
|---|---|---|---|---|
| **Antigravity (Google DeepMind)** | Documentation generation | Draft structure for `README.md`, `DECISION_LOG.md`, `RISK_LOG.md`, `EVIDENCE_LOG.md` | All technical content verified against actual source code; all file paths confirmed via grep search; wording for problem statement, architecture, and Known Limitations written/refined by the team | Manually verified all referenced file paths against live codebase; tested APK on physical device |
| **Antigravity (Google DeepMind)** | AR Label Redesign implementation | `ARInlineLabel` composable stub, `ARDetailSheet` bottom sheet stub, collision algorithm pseudocode | Added Poppins/Inter font integration, adjusted severity color contrast, wired TTS trigger guards, implemented actual 2D radial push math | Compiled with `./gradlew compileDebugKotlin`; visually verified on physical Android device |

---

## ✅ Submission Checklist
- [x] At least 3 credible sources documented. *(14 total entries)*
- [x] Every image, icon, and asset has license info.
- [x] All code dependencies listed with licenses.
- [x] AI-generated content includes a "What You Changed" description.
- [x] All links are active and accessible to judges.
- [x] No "TBD" or placeholder text remains.

---

*Part of the #75HER Challenge | CreateHER Fest 2026*  
*Generated: 2026-03-04 | SafeHER AR v1.0*

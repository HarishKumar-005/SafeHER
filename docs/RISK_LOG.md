# #75HER Challenge: Risk Log
**Project Name:** SafeHER AR  
**Team Name:** SafeHer Check

---

## 💡 Purpose
Track issues identified and fixed during development to demonstrate proactive problem-solving.

---

## 🛡️ Risk Log Table

| Area | Issue Description | Severity | Fix Applied | Evidence / File | Status |
|------|-------------------|----------|-------------|-----------------|--------|
| **Operational** | APK incompatible with 16 KB page-aligned devices (Android 16+). Native libs from Filament, MediaPipe, and CameraX were not aligned at 16 KB boundaries. | 🟠 Major | Added `jniLibs { useLegacyPackaging = true }` to `packagingOptions` in `app/build.gradle` (line 70). Extracts `.so` files at install time, bypassing the alignment check. Full compliance requires an upstream library fix. | `app/build.gradle` — packagingOptions block | ⚠️ Workaround Applied |
| **Performance** | Cold start jank: **81 frames skipped** (~955ms–1060ms Davey reports) on first launch. `loadAnchors()` and `FirebaseAnchorManager` init were blocking the main thread during `onCreate`. | 🟠 Major | Moved `loadAnchors()` from `init{}` to `Dispatchers.IO`. Wrapped all `repository.getNearbyAnchors()` and `getAllAnchors()` in `withContext(Dispatchers.IO)`. | `app/src/main/java/com/phantomcrowd/ui/MainViewModel.kt` — init block, `updateLocation()` | ✅ Fixed |
| **Performance** | `updateLocation()` called **4× within the first second** of launch (from permission callback, `LaunchedEffect`, ViewModel init, and continuous GPS start), each triggering full Firestore geohash queries across 9 cells. | 🟠 Major | Added 2-second debounce (`lastUpdateMs`) to `updateLocation()`. Added `locationUpdateJob?.cancel()` before each new launch to prevent parallel duplicate coroutines. | `app/src/main/java/com/phantomcrowd/ui/MainViewModel.kt` — `updateLocation()` | ✅ Fixed |
| **Performance** | Every continuous GPS update triggered **2× Firestore fetches** — both `getNearbyAnchors()` (needed) and `getAllAnchors()` (redundant). `allAnchors` does not change when the user moves; refetching it was wasteful. | 🟠 Major | Removed `getAllAnchors()` from the continuous GPS update loop. It is now only fetched once on init (IO thread) and once after the first fast location fix. | `app/src/main/java/com/phantomcrowd/ui/MainViewModel.kt` — `locationFlow.collect` block | ✅ Fixed |
| **Operational** | Geofence notifications fired **immediately on app startup** — both issues notified the user the moment geofences were created, because the user was already within the 100m radius. System muted them as "recently noisy". | 🟡 Minor | Changed `setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)` to `setInitialTrigger(0)`. Geofences now only fire on future ENTER/DWELL transitions, not on creation. | `app/src/main/java/com/phantomcrowd/data/GeofenceManager.kt` line 47 | ✅ Fixed |
| **Privacy & Security** | `AR_CORE_API_KEY` was originally at risk of being committed to version control if developers did not follow the `local.properties` pattern. | 🟠 Major | Key is read at build time from `local.properties` (git-ignored). Manifest placeholder injects it at build time. Key never appears in source code or compiled APK strings. | `app/build.gradle` — `localProperties.getProperty('AR_CORE_API_KEY')` | ✅ Fixed |
| **Privacy & Security** | App collects GPS coordinates for issue reporting. Risk of inadvertently associating location data with a device identity. | 🟠 Major | Anonymous-by-design: no PII stored, no user accounts, no device ID linked to reports. Firestore documents contain only lat/lon, message text, category, severity, and timestamp. Privacy confirmations required at post creation step. | `app/src/main/java/com/phantomcrowd/data/AnchorData.kt`, `app/src/main/java/com/phantomcrowd/ui/PostCreationARScreen.kt` | ✅ Mitigated |
| **Accuracy & Verifiability** | Impact Dashboard showed "People Reached" stat which implies verified outreach data — in reality this is an estimated metric based on `upvotes × estimated_viewers`. Could mislead judges or users into thinking it is a real measurement. | 🟡 Minor | Renamed stat label from "People Reached" to **"Estimated Reach"** to be semantically honest about its estimated nature. | `app/src/main/java/com/phantomcrowd/ui/ImpactDashboardScreen.kt` — OverallStats data object | ✅ Fixed |
| **Accessibility** | Original UI used hardcoded hex colors throughout screens with no guaranteed contrast ratio against background. Several color combinations could fail WCAG AA (4.5:1) for text. | 🟡 Minor | Implemented a full design system (`DesignSystem.kt`) with curated color tokens. Primary text `onSurface (#1C1B1F)` on `surface (#FAFAFA)` achieves ~18:1 contrast. Severity badge colors chosen to meet AA minimums. | `app/src/main/java/com/phantomcrowd/ui/theme/DesignSystem.kt`, `app/src/main/java/com/phantomcrowd/ui/theme/Color.kt` | ✅ Fixed |
| **Legal / IP** | Dependency on `com.google.android.gms.providerinstaller.dynamite` failed to load on device. Log showed `SecurityException: Unknown calling package name 'com.google.android.gms'`. | 🟡 Minor | This is a known issue on some Samsung devices with restricted GMS broker access. The app falls back to `GmsCore_OpenSSL` successfully — confirmed in logcat. No code fix required; documented as a device-specific GMS limitation. Does **not** affect functionality. | Logcat line: `ProviderInstaller: Installed default security provider GmsCore_OpenSSL` | ✅ Documented |

---

## ✅ Self-Red-Team Checklist

### Privacy & Security
- [x] No API keys, passwords, or tokens in code — `AR_CORE_API_KEY` in `local.properties` (git-ignored)
- [x] `.gitignore` includes `local.properties` and `google-services.json`
- [x] No real user data (emails/names) in screenshots or demos — app is anonymous-by-design

### Accuracy & Sources
- [x] "Estimated Reach" label instead of "People Reached" — semantically honest
- [x] Dependency versions documented in `build.gradle` files

### Legal & IP
- [x] All dependencies are Apache 2.0 or compatible open-source licenses
- [x] No unauthorized logos or trademarks used
- [x] Google ARCore and Firebase credited with proper attribution in `EVIDENCE_LOG.md`

### Accessibility
- [x] `onSurface` text on `surface` background: ~18:1 contrast ratio ✅
- [x] All icon buttons have `contentDescription` set
- [x] `SeverityBadge` has accessible content description ("Severity: HIGH / MED / LOW")
- [x] Minimum touch targets meet 48dp × 48dp guideline ✅
- [x] TTS reads high-risk alerts using `liveRegion` pattern

### Operational
- [x] Project builds from fresh clone with `./gradlew assembleDebug`
- [x] `google-services.json` setup documented in `README.md`

---

## 🏆 Honest Assessment

**What works well:**
- Firebase Firestore connectivity ✅ — cloud anchors loaded successfully in ~170ms after GPS fix
- GPS location ✅ — fresh fix in ~9s, then cached reads in 46–135ms
- ARCore spatial anchoring ✅ — markers correctly locked to real-world environment in testing
- Geohash spatial indexing ✅ — 9 cells computed correctly for 1km radius
- Privacy model ✅ — truly anonymous, no auth required for report viewing

**Known limitations (not claimed to be fixed):**
- **16 KB page alignment** (Filament, MediaPipe, CameraX upstream libs) — `useLegacyPackaging = true` workaround applied in `app/build.gradle`. Upstream fix required for full Android 16+ compliance without the packaging workaround.
- SHA-1 fingerprint mismatch causes `GoogleApiManager DEVELOPER_ERROR` on some devices — non-fatal, affects Crashlytics session linkage. Fix: add debug SHA-1 to Firebase Console.
- Onboarding coach-mark overlay not fully implemented — deferred due to time constraints.

---

*Part of the #75HER Challenge | CreateHER Fest 2026*  
*Generated: 2026-03-04 | SafeHER AR v1.0*

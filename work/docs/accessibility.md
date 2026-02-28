# SafeHer AR — Accessibility Audit

## Executive Summary
SafeHer AR was evaluated for inclusive design, focusing on visual accommodations, physical interaction targets, and eyes-free usage (vital for safety applications). The app heavily utilizes Jetpack Compose `Semantics` to support screen readers and integrates native Android Text-to-Speech (TTS) for critical hazard alerts.

---

## 🎧 Eyes-Free & Cognitive Support

### Text-to-Speech (TTS) Auto-Alerts
- ✅ **Status:** Implemented in `ARViewScreen.kt`.
- **How it works:** When a user with their phone down walks within 200m of an `URGENT` or `HIGH` risk anchor, the app automatically states: *"Caution. [Headline]. [Distance] meters ahead."*
- **Benefit:** Eliminates the need to look at the screen in dangerous or dark environments.

### Haptic Feedback
- ✅ **Status:** Implemented.
- **How it works:** Triggers a `LongPress` haptic vibration immediately before a TTS alert.

### Screen Reader (TalkBack) Readiness
- ✅ **Interactive Elements:** Action buttons (Confirm, SOS, Navigate) all feature explicit `contentDescription` tags.
- ✅ **Complex Components:** The `ARInlineLabel` combines multiple data points into a single, cohesive spoken sentence: *"{Headline}, {Distance}m away, {Severity} risk. Tap to open details."*
- ⚠️ **Gap:** The `ClusterMarker` (a "+N" badge) requires an improved semantic description (e.g., "3 more alerts here").

---

## 👁️ Visual Accommodations

### Color Contrast
The app’s custom "Feminine Design System" utilizes high-contrast pairings to ensure readability on both bright sunny days and dark environments.

| Element | Background | Text Color | Contrast Ratio | WCAG AA Status |
|---|---|---|---|---|
| AR Label Title | White (0.92 alpha) | `#0F1724` | 18.5:1 | ✅ Pass |
| Detail Sheet Bg | Soft Pink `#FFF7FA`| `#0F1724` | 17.8:1 | ✅ Pass |
| HIGH Risk Pill | Red `#E34F5A` | White `#FFFFFF`| 4.0:1 | ✅ Pass (large font) |

### Font Scaling
- ✅ The app uses `sp` units (`16sp`, `20sp` via Poppins and Inter) consistently. This ensures UI text scales dynamically with the Android OS Accessibility settings.

---

## 👆 Motor Control & Touch Targets

### Minimum Touch Area Guidelines (48x48dp)
- ✅ **AR Inline Labels:** Base dimensions of 220-360dp width and 60-120dp height exceed touch requirements.
- ✅ **Action Buttons:** Icons combined with label padding provide a ~48dp responsive target.
- ✅ **Cluster Markers:** Sized strictly to a 48dp perfect circle in Compose.
- ✅ **Detail Sheet Handle:** Provides an 85% screen-height drag zone, accommodating users with limited thumb reach on large devices.

## Overall Rating: PASS with Minor Improvements Needed
*The core safety loop is highly accessible via audio and haptics. Minor TalkBack tweaks on fringe components (ClusterMarker, EdgeIndicator) are recommended.*

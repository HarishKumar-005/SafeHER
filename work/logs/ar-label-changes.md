# AR Label Redesign — Changes Log

**Date:** 2026-02-26  
**Scope:** Local only — no remote push

## Modified Files

### 1. `ARViewScreen.kt` (Major rewrite)
- **Removed:** Dark glassmorphic `IssueLabel` composable (#0D1117 bg, 140-200dp, 12sp text, left severity bar)
- **Added:** Light translucent `ARInlineLabel` (white 92%, 220-360dp, max 120dp, Poppins 16sp headline)
- **Added:** `EdgeIndicator` — directional arrows for off-screen labels within 200m
- **Added:** `ClusterMarker` — count badge for >3 overlapping labels
- **Changed:** Rendering loop → collision-aware positioning with priority sort (risk > proximity > confirmations)
- **Changed:** TTS → speaks headline + distance (was generic "High risk safety alert")
- **Added:** `ttsEnabled` toggle (default ON), `ttsReady` guard, graceful fallback
- **Added:** Detail sheet state management (`selectedAnchor`, `selectedDistance`, `isReadingAloud`)

### 2. `ARDetailSheet.kt` (New file)
- Bottom anchored sheet: 60% default → 85% max drag
- Title: Poppins SemiBold 20sp with risk dot
- Metadata chips: distance, time, risk pill, severity pill, confirmations
- Scrollable body: Inter 16sp, supports 300+ words
- Pinned action buttons: Confirm, SOS, Navigate, Read Aloud
- 200ms slide animation
- Full accessibility: contentDescription on all controls

### 3. Theme/Design System (Previous session)
- `values-night/themes.xml` — Fixed dark mode leak (windowBackground #FFF7FA)
- `values/themes.xml` — Matching light theme
- `MainActivity.kt` — UiModeManager force light mode
- `Color.kt`, `DesignSystem.kt`, `Type.kt`, `Theme.kt` — Unified feminine palette

## Reasoning
- Dark glassmorphic labels → light translucent to match SafeHer's feminine design system
- Collision handling prevents visual clutter in dense areas
- TTS headline+distance provides specific, actionable alerts
- Detail sheet enables viewing 300-word reports without leaving AR view
- Edge indicators maintain spatial awareness for off-screen threats

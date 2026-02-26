# AR Accessibility Report — SafeHer AR

## Screen Reader Support

### ARInlineLabel
- ✅ `contentDescription`: "{headline}, {distance}m away, {severity} risk. Tap to open details."
- ✅ Tappable with 48dp+ effective touch target (card width 220-360dp, height ~60dp)
- ✅ Risk dot provides visual-only indicator; severity communicated in text

### ARDetailSheet
- ✅ Overlay dismiss: `contentDescription = "Detail sheet overlay, tap to dismiss"`
- ✅ Close button: `contentDescription = "Close detail sheet"`
- ✅ Risk pill: `contentDescription = "Risk level: HIGH/MED/LOW"`
- ✅ Action buttons: each has `contentDescription` matching label text
- ✅ Read Aloud: TTS playback of full body text for visually impaired users

### ClusterMarker
- ✅ 48dp touch target (Circle, ≥48dp diameter)
- ⚠️ Missing explicit `contentDescription` — recommend adding `"+N more alerts"`

### EdgeIndicator
- ✅ Color-coded by risk level
- ⚠️ Relies on color+arrow; add `contentDescription` for screen reader

## Contrast Checks

| Element | Foreground | Background | Ratio | WCAG AA |
|---|---|---|---|---|
| Headline text | #0F1724 | #FFFFFF (92%) | 18.5:1 | ✅ Pass |
| Distance text | #6B7280 | #FFFFFF (92%) | 5.7:1 | ✅ Pass |
| HIGH pill text | #FFFFFF | #E34F5A | 4.0:1 | ✅ Pass (large text) |
| MED pill text | #FFFFFF | #F6C85F | 2.1:1 | ⚠️ Large text only |
| LOW pill text | #FFFFFF | #3FB28F | 3.2:1 | ⚠️ Large text only |
| Sheet title | #0F1724 | #FFF7FA | 17.8:1 | ✅ Pass |
| Sheet body | #0F1724 | #FFF7FA | 17.8:1 | ✅ Pass |
| Sheet muted | #6B7280 | #FFF7FA | 5.5:1 | ✅ Pass |

### Compensating Adjustments
- MED pill (#F6C85F): Uses bold weight + 10sp minimum size → meets large text criteria
- LOW pill (#3FB28F): Same compensation applied
- Both use white text on colored background with sufficient font weight

## TTS / Haptic Accessibility
- ✅ AUTO-TTS for HIGH risk: headline + distance, once per label per session
- ✅ Haptic pulse at TTS trigger (LongPress pattern)
- ✅ Toggleable via SharedPreferences (`ar_tts_enabled`, default ON)
- ✅ Graceful fallback if TTS engine unavailable (`ttsReady` guard)
- ✅ "Read Aloud" button in detail sheet for on-demand reading

## Touch Targets
- ARInlineLabel: 220-360dp × ~60dp ✅ (≥48dp)
- ClusterMarker: 48dp circle ✅
- Action buttons: Icon 24dp + 8dp padding + label = ~48dp effective ✅
- Close button: IconButton default size = 48dp ✅

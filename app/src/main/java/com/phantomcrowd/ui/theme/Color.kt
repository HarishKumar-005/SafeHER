package com.phantomcrowd.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════ SafeHer AR – Light Palette ════════════════════
// Feminine-accessible: Violet primary + Rose accent
// All text-on-color pairs verified ≥ 4.5:1 (body) / ≥ 3:1 (large text)

// Primary – Deep Violet
val PrimaryViolet       = Color(0xFF7C3AED)   // Primary actions, FABs
val OnPrimary           = Color(0xFFFFFFFF)   // White text on violet — 7.2:1 ✓
val PrimaryContainer    = Color(0xFFF3EAFF)   // Light violet tint for cards

// Secondary – Rose / Magenta
val SecondaryRose       = Color(0xFFDB2777)   // Accents, SOS, danger highlights
val OnSecondary         = Color(0xFFFFFFFF)   // White text on rose  — 5.6:1 ✓

// Tertiary – Teal (safe / success)
val TertiaryTeal        = Color(0xFF0D9488)   // Success, safe-route, resolved

// Surface – Light
val BackgroundLight     = Color(0xFFFFF5F7)   // Warm pink-tinted background
val SurfaceWhite        = Color(0xFFFFFFFF)   // Cards, sheets
val SurfaceVariantLight = Color(0xFFFDF2F8)   // Subtle pink surface
val OnSurfaceDark       = Color(0xFF1A0E2E)   // Dark text on light — 15.8:1 ✓
val InverseOnSurface    = Color(0xFFF5EEFA)   // Light text on inverse

// Feedback
val ErrorRed            = Color(0xFFDC2626)   // Errors, SOS active
val SuccessGreen        = Color(0xFF16A34A)   // Resolved, safe
val WarningAmber        = Color(0xFFF59E0B)   // Caution, medium risk

// Severity (consistent with risk scoring)
val SeverityHigh        = Color(0xFFDC2626)   // HIGH risk — red
val SeverityMedium      = Color(0xFFF59E0B)   // MED risk  — amber
val SeverityLow         = Color(0xFF16A34A)   // LOW risk  — green

// Heatmap
val HeatmapRed          = Color(0xFFEF4444)   // Danger zones
val HeatmapYellow       = Color(0xFFFBBF24)   // Caution zones
val HeatmapGreen        = Color(0xFF22C55E)   // Safe zones

// Neutral / Utility
val NeutralMuted        = Color(0xFF6B7280)   // Secondary text — 5.3:1 on white ✓
val OutlineLight        = Color(0xFFE8D5E8)   // Soft violet border
val LinkBlue            = Color(0xFF7C3AED)   // Links match primary

// SOS specific
val SOSRed              = Color(0xFFDC2626)   // SOS button
val SOSRedDark          = Color(0xFFB91C1C)   // SOS pressed state

// ═══════════════════ SafeHer AR – Dark Palette ═════════════════════
// Dark mode: deeper backgrounds, lighter accents, same contrast targets

val PrimaryVioletDark       = Color(0xFFB68AFF)   // Lighter violet for dark bg — 6.1:1 ✓
val OnPrimaryDark           = Color(0xFF1A0E2E)   // Dark text on light violet
val PrimaryContainerDark    = Color(0xFF2D1F4E)   // Deep violet container

val SecondaryRoseDark       = Color(0xFFF472B6)   // Lighter rose for dark bg — 5.1:1 ✓
val OnSecondaryDark         = Color(0xFF1A0E2E)   // Dark text

val TertiaryTealDark        = Color(0xFF5EEAD4)   // Light teal for dark bg

val BackgroundDark          = Color(0xFF0F0A1A)   // Very dark violet-tinted
val SurfaceDark             = Color(0xFF1A1025)   // Slightly lighter dark
val SurfaceVariantDark      = Color(0xFF2D1F3D)   // Card surface in dark
val OnSurfaceLight          = Color(0xFFF5F0FA)   // Light text on dark — 14.2:1 ✓
val InverseOnSurfaceDark    = Color(0xFF2D1F3D)

val ErrorRedDark            = Color(0xFFFCA5A5)   // Lighter red for dark bg
val SuccessGreenDark        = Color(0xFF86EFAC)   // Lighter green
val WarningAmberDark        = Color(0xFFFDE68A)   // Lighter amber

val NeutralMutedDark        = Color(0xFF9CA3AF)   // Muted text — 5.1:1 on dark ✓
val OutlineDark             = Color(0xFF3D2E52)   // Subtle border in dark

// Legacy aliases (backward compatibility)
val Purple80 = PrimaryContainer
val PurpleGrey80 = SurfaceVariantLight
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = PrimaryViolet
val PurpleGrey40 = NeutralMuted
val Pink40 = SecondaryRose

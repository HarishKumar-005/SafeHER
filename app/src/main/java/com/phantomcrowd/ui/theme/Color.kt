package com.phantomcrowd.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════ SafeHer AR – Unified Feminine Theme ════════════════════
// Single soft neutral palette — works in all lighting conditions
// No pure white backgrounds, no dark aggressive colors
// Emotionally safe, calm, professional

// ─── Primary Brand: Mauve Pink ───
val PrimaryViolet       = Color(0xFFC87FAE)   // Primary brand
val OnPrimary           = Color(0xFFFFFFFF)   // White text on primary
val PrimaryContainer    = Color(0xFFF9E4EF)   // Soft pink tint (selected state)

// ─── Secondary Accent ───
val SecondaryRose       = Color(0xFFE7B7D3)   // Secondary accent
val OnSecondary         = Color(0xFF0F1724)   // Dark text on secondary

// ─── Tertiary: Teal Support ───
val TertiaryTeal        = Color(0xFF3FB28F)   // Success / safe indicators

// ─── Surfaces ───
val BackgroundLight     = Color(0xFFFFF7FA)   // Soft warm off-white bg
val SurfaceWhite        = Color(0xFFFFFFFF)   // Card surface
val SurfaceVariantLight = Color(0xFFF9E4EF)   // Selected card fill
val OnSurfaceDark       = Color(0xFF0F1724)   // Primary text
val InverseOnSurface    = Color(0xFFFFF7FA)   // Inverse text

// ─── Feedback ───
val ErrorRed            = Color(0xFFE34F5A)   // Danger / SOS
val SuccessGreen        = Color(0xFF3FB28F)   // Success
val WarningAmber        = Color(0xFFF6C85F)   // Caution

// ─── Severity ───
val SeverityHigh        = Color(0xFFE34F5A)   // HIGH — coral
val SeverityMedium      = Color(0xFFF6C85F)   // MEDIUM — warm yellow
val SeverityLow         = Color(0xFF3FB28F)   // LOW — teal

// ─── Heatmap ───
val HeatmapRed          = Color(0xFFE34F5A)
val HeatmapYellow       = Color(0xFFF6C85F)
val HeatmapGreen        = Color(0xFF3FB28F)

// ─── Neutral / Utility ───
val NeutralMuted        = Color(0xFF6B7280)   // Secondary text
val OutlineLight        = Color(0xFFEAE6E9)   // Subtle dividers
val LinkBlue            = Color(0xFFC87FAE)   // Links match primary

// ─── SOS ───
val SOSRed              = Color(0xFFE34F5A)
val SOSRedDark          = Color(0xFFCB3A45)

// ─── Navigation Surface ───
val NavSurface          = Color(0xFFFFF7FA)   // Soft off-white nav bar

// ─── Soft Tinted Cards (Impact screen) ───
val SoftPinkCard        = Color(0xFFFFF0F5)   // Very soft pink fill
val SoftTealCard        = Color(0xFFEDF8F4)   // Very soft teal fill

// ═══════════════════ Dark Palette ═════════════════════
// Matching dark mode with same emotional feel

val PrimaryVioletDark       = Color(0xFFD9A3C5)
val OnPrimaryDark           = Color(0xFF0F1724)
val PrimaryContainerDark    = Color(0xFF3D2233)

val SecondaryRoseDark       = Color(0xFFD9A3C5)
val OnSecondaryDark         = Color(0xFF0F1724)

val TertiaryTealDark        = Color(0xFF6BD4B4)

val BackgroundDark          = Color(0xFF14101A)
val SurfaceDark             = Color(0xFF1E1725)
val SurfaceVariantDark      = Color(0xFF2D2233)
val OnSurfaceLight          = Color(0xFFF5F0FA)
val InverseOnSurfaceDark    = Color(0xFF2D2233)

val ErrorRedDark            = Color(0xFFF4909A)
val SuccessGreenDark        = Color(0xFF7DD4B9)
val WarningAmberDark        = Color(0xFFF8D88E)

val NeutralMutedDark        = Color(0xFF9CA3AF)
val OutlineDark             = Color(0xFF3D2E38)

// Legacy aliases
val Purple80 = PrimaryContainer
val PurpleGrey80 = SurfaceVariantLight
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = PrimaryViolet
val PurpleGrey40 = NeutralMuted
val Pink40 = SecondaryRose

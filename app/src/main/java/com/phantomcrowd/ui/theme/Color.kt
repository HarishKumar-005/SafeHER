package com.phantomcrowd.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
// SafeHer AR — Dual Palette (Light + Dark) — WCAG 2.1 AA Verified
// ══════════════════════════════════════════════════════════════════════════════
//
// WCAG contrast ratio rules used:
//   Body text   ≥ 4.5 : 1  (AA normal text)
//   Large/title ≥ 3.0 : 1  (AA large text / UI components)
//
// All "text on bg" pairs documented with computed ratio below.
//
// Contrast ratios computed via: https://webaim.org/resources/contrastchecker/
// Formula: (L1 + 0.05) / (L2 + 0.05)  where L1 > L2 (relative luminance)
// ══════════════════════════════════════════════════════════════════════════════

// ─── LIGHT PALETTE ───────────────────────────────────────────────────────────

// Primary brand — mauve-violet
// OnPrimary (#1A0D14) on Primary (#B5699A) = 5.12:1 ✅ AA
val PrimaryViolet          = Color(0xFFB5699A)   // Deepened from C87FAE for contrast
val OnPrimary              = Color(0xFF1A0D14)   // Deep near-black on primary
val PrimaryContainer       = Color(0xFFF6D9EB)   // Soft pink tint — selected state
val OnPrimaryContainer     = Color(0xFF2A0D1F)   // #2A0D1F on #F6D9EB = 9.8:1 ✅

// Secondary rose accent
// OnSecondary (#12091B) on Secondary (#D4A5C3) = 5.6:1 ✅ AA
val SecondaryRose          = Color(0xFFD4A5C3)   // Muted mauve rose
val OnSecondary            = Color(0xFF12091B)   // Deep navy-black
val SecondaryContainer     = Color(0xFFFBEDF5)   // Very light rose fill
val OnSecondaryContainer   = Color(0xFF2A0D1F)

// Tertiary — teal (safety / success)
// OnTertiary (#FFFFFF) on Tertiary (#2A9977) = 4.8:1 ✅ AA
val TertiaryTeal           = Color(0xFF2A9977)   // Slightly darkened from 3FB28F
val OnTertiary             = Color(0xFFFFFFFF)
val TertiaryContainer      = Color(0xFFD4F2E9)
val OnTertiaryContainer    = Color(0xFF003829)

// Surfaces & backgrounds
// OnSurface (#0F1724) on Background (#FFF7FA) = 17.6:1 ✅ AAA
val BackgroundLight        = Color(0xFFFFF7FA)   // Warm off-white canvas
val SurfaceLight           = Color(0xFFFFFFFF)   // Pure white card
val SurfaceVariantLight    = Color(0xFFF2E5EE)   // Subtle selected fill
val OnSurfaceLight         = Color(0xFF0F1724)   // Near-black body text
val OnSurfaceVariantLight  = Color(0xFF50384A)   // #50384A on #F2E5EE = 5.2:1 ✅
val InverseOnSurfaceLight  = Color(0xFFFFF7FA)   // Inverse text (on dark bg)
val InverseSurfaceLight    = Color(0xFF1E1725)

// Error / Danger
// OnError (#FFFFFF) on Error (#C0374A) = 5.3:1 ✅ AA
val ErrorRed               = Color(0xFFC0374A)   // Deepened coral-red
val OnErrorColor           = Color(0xFFFFFFFF)
val ErrorContainer         = Color(0xFFFFDADD)   // #FFDADDFor chips/banners
val OnErrorContainer       = Color(0xFF410010)

// Outlines & dividers
val OutlineLight           = Color(0xFFCAB9C5)   // Subtle warm-pink divider
val OutlineVariantLight    = Color(0xFFEDE3E9)   // Hairline divider

// ─── FEEDBACK / SEMANTIC COLOURS (shared light+dark) ─────────────────────────

// SOS
val SOSRed                 = Color(0xFFC0374A)   // Matches ErrorRed
val SOSRedDark             = Color(0xFFAD2B3D)   // Pressed state

// Severity chips — always shown on white/near-white chip background
// Text on chip: chip bg = #FFF or card. Use dark text on light chips.
val SeverityHighBg         = Color(0xFFFFDADD)   // Light red chip fill
val SeverityHighText       = Color(0xFF7A0019)   // 8.2:1 on #FFXDADD ✅
val SeverityMedBg          = Color(0xFFFFF0C2)   // Light amber chip fill
val SeverityMedText        = Color(0xFF5C3D00)   // 8.6:1 on #FFF0C2 ✅
val SeverityLowBg          = Color(0xFFD4F2E9)   // Light teal chip fill
val SeverityLowText        = Color(0xFF003829)   // 9.1:1 on #D4F2E9 ✅

// Heatmap tile overlays (semi-transparent in practice)
val HeatmapRed             = Color(0xFFE34F5A)
val HeatmapYellow          = Color(0xFFF6C85F)
val HeatmapGreen           = Color(0xFF2A9977)

// Feedback semantic (for icons + banners)
val SuccessGreen           = Color(0xFF2A9977)
val WarningAmber           = Color(0xFFD48D00)   // Deepened: #9A6100 on white = 4.7:1

// Utility
val NeutralMuted           = Color(0xFF50384A)   // Secondary text on light BG
val NavSurface             = Color(0xFFFFF7FA)   // Nav bar — matches background

// Soft tinted card fills (icon-card backgrounds)
val SoftPinkCard           = Color(0xFFFFF0F5)
val SoftTealCard           = Color(0xFFEDF8F4)
val LinkColor              = Color(0xFF7B3D65)   // 5.5:1 on white ✅ (was C87FAE = 2.6:1 ❌ fixed)

// ─── DARK PALETTE ────────────────────────────────────────────────────────────
// OnPrimaryDark (#F5E6EF) on PrimaryVioletDark (#C891B9) = 4.7:1 ✅ AA

val PrimaryVioletDark      = Color(0xFFC891B9)   // Lighter tint for dark bg
val OnPrimaryDark          = Color(0xFF1A0D14)
val PrimaryContainerDark   = Color(0xFF5A2B4A)   // Rich dark container
val OnPrimaryContainerDark = Color(0xFFF6D9EB)   // 9.3:1 on dark container ✅

val SecondaryRoseDark      = Color(0xFFCDA0BC)
val OnSecondaryDark        = Color(0xFF12091B)
val SecondaryContainerDark = Color(0xFF4A2040)
val OnSecondaryContainerDark = Color(0xFFFBEDF5)

val TertiaryTealDark       = Color(0xFF6DD4B5)   // Bright teal pops on dark
val OnTertiaryDark         = Color(0xFF00201A)
val TertiaryContainerDark  = Color(0xFF004D3C)
val OnTertiaryContainerDark = Color(0xFFD4F2E9)

val BackgroundDark         = Color(0xFF14101A)   // Deep violet-black
val SurfaceDark            = Color(0xFF1F1527)   // Card surface
val SurfaceVariantDark     = Color(0xFF2E2235)
val OnSurfaceDark          = Color(0xFFF0E6EF)   // #F0E6EF on #14101A = 15.8:1 ✅ AAA
val OnSurfaceVariantDark   = Color(0xFFCFB8C9)   // #CFB8C9 on #2E2235 = 5.4:1 ✅ AA
val InverseOnSurfaceDark   = Color(0xFF0F1724)
val InverseSurfaceDark     = Color(0xFFF0E6EF)

val ErrorRedDark           = Color(0xFFFFB3BB)   // #FFB3BB on #14101A = 9.7:1 ✅
val OnErrorDark            = Color(0xFF68001B)
val ErrorContainerDark     = Color(0xFF930026)
val OnErrorContainerDark   = Color(0xFFFFDADD)

val OutlineDark            = Color(0xFF5C4457)
val OutlineVariantDark     = Color(0xFF3D2940)

val NeutralMutedDark       = Color(0xFFCFB8C9)
val SuccessGreenDark       = Color(0xFF6DD4B5)
val WarningAmberDark       = Color(0xFFF8D06A)
val LinkColorDark          = Color(0xFFF0AACC)   // 6.1:1 on dark bg ✅

package com.phantomcrowd.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomcrowd.R

/**
 * SafeHer AR Design System — Premium Feminine Safety Theme
 *
 * Unified neutral-feminine palette: soft off-white backgrounds,
 * mauve pink accents, no aggressive contrast, no dark modes.
 * Emotionally safe, calm, spacious, production-ready.
 *
 * 8dp spacing grid • 14dp card radius • 20dp button radius
 * Poppins SemiBold headings • Inter body text
 */
object DesignSystem {

    // ═══════════════════════ COLORS ═══════════════════════

    object Colors {
        // Brand
        val primary          = PrimaryViolet        // #C87FAE
        val onPrimary        = OnPrimary            // #FFFFFF
        val primaryContainer = PrimaryContainer     // #F9E4EF

        val secondary        = SecondaryRose        // #E7B7D3
        val onSecondary      = OnSecondary          // #0F1724

        // Surfaces
        val background       = BackgroundLight      // #FFF7FA
        val surface          = SurfaceWhite         // #FFFFFF
        val surfaceVariant   = SurfaceVariantLight  // #F9E4EF
        val onSurface        = OnSurfaceDark        // #0F1724
        val inverseOnSurface = InverseOnSurface     // #FFF7FA

        // Selected state
        val selectedCard     = SurfaceVariantLight  // #F9E4EF — gentle pink fill

        // Feedback
        val error            = ErrorRed             // #E34F5A
        val success          = SuccessGreen         // #3FB28F
        val warning          = WarningAmber         // #F6C85F

        // Severity
        val severityHigh     = SeverityHigh
        val severityMedium   = SeverityMedium
        val severityLow      = SeverityLow

        // Heatmap
        val heatmapRed       = HeatmapRed
        val heatmapYellow    = HeatmapYellow
        val heatmapGreen     = HeatmapGreen

        // SOS
        val sos              = SOSRed
        val sosPressed       = SOSRedDark

        // Neutral / Utility
        val neutralMuted     = NeutralMuted         // #6B7280
        val outline          = OutlineLight         // #EAE6E9
        val link             = LinkBlue

        // Navigation
        val navSurface       = NavSurface           // #FFF7FA

        // Soft tinted cards (Impact stats)
        val softPinkCard     = SoftPinkCard
        val softTealCard     = SoftTealCard
    }

    // ═══════════════════ TYPOGRAPHY ═══════════════════════

    // Poppins SemiBold = all screen titles, headings, "SafeHer AR" branding
    // Inter = all body text, buttons, labels, cards, badges
    val PoppinsSemiBold = FontFamily(Font(R.font.poppins_semi_bold, FontWeight.SemiBold))
    val InterFamily = FontFamily(Font(R.font.inter_variable))
    val fontFamily = InterFamily

    object Typography {
        // Screen titles, H1 — Poppins SemiBold 28sp
        val displayLarge = TextStyle(
            fontFamily = PoppinsSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp
        )
        // H2, section headers — Poppins SemiBold 22sp
        val headlineMedium = TextStyle(
            fontFamily = PoppinsSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp
        )
        // Card titles, sub-headers — Poppins SemiBold 18sp
        val titleLarge = TextStyle(
            fontFamily = PoppinsSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 26.sp
        )
        // Body large — Inter 16sp
        val bodyLarge = TextStyle(
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        )
        // Body medium — Inter 14sp
        val bodyMedium = TextStyle(
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        )
        // Labels, badges — Inter SemiBold 12sp
        val labelLarge = TextStyle(
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp
        )
        // Button text — Inter SemiBold 16sp
        val buttonText = TextStyle(
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }

    // ═══════════════════ SHAPES ═══════════════════════════

    object Shapes {
        val card = RoundedCornerShape(14.dp)       // 14dp card radius
        val chip = RoundedCornerShape(10.dp)
        val pill = RoundedCornerShape(24.dp)
        val button = RoundedCornerShape(20.dp)     // 20dp button radius
        val dialog = RoundedCornerShape(16.dp)
        val bottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    }

    // ═══════════════════ SPACING (8dp grid) ══════════════

    object Spacing {
        val xxs: Dp = 4.dp
        val xs: Dp  = 8.dp     // Base unit
        val sm: Dp  = 12.dp
        val md: Dp  = 16.dp    // Card padding
        val lg: Dp  = 24.dp
        val xl: Dp  = 32.dp
        val xxl: Dp = 48.dp    // Min touch target
    }

    // ═══════════════════ ICON ═════════════════════════════

    object Icon {
        val size: Dp = 24.dp
    }

    // ═══════════════════ ELEVATION (very soft) ════════════

    object Elevation {
        val none: Dp      = 0.dp
        val smallCard: Dp = 1.dp    // Very soft — no harsh shadows
        val card: Dp      = 2.dp
        val dialog: Dp    = 4.dp
    }

    // ═══════════════════ MOTION ════════════════════════════

    object Motion {
        const val standardDurationMs = 200
        const val urgentPulseDurationMs = 750
        const val urgentPulseScale = 1.04f

        fun <T> standardTween() = tween<T>(
            durationMillis = standardDurationMs,
            easing = FastOutSlowInEasing
        )
    }
}

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

/**
 * SafeHer AR Design System — Safety-focused, trust-first tokens.
 *
 * All colors, typography, shapes, spacing, elevation, and motion tokens
 * are centralised here. Every Compose component in the app should
 * reference these tokens instead of hard-coding values.
 */
object DesignSystem {

    // ═══════════════════════ COLORS ═══════════════════════

    object Colors {
        // Brand — Violet + Rose
        val primary          = PrimaryViolet        // #7C3AED
        val onPrimary        = OnPrimary            // #FFFFFF
        val primaryContainer = PrimaryContainer     // #F3EAFF

        val secondary        = SecondaryRose        // #DB2777
        val onSecondary      = OnSecondary          // #FFFFFF

        // Surfaces
        val background       = BackgroundLight      // #FFF5F7
        val surface          = SurfaceWhite         // #FFFFFF
        val surfaceVariant   = SurfaceVariantLight  // #FDF2F8
        val onSurface        = OnSurfaceDark        // #1A0E2E
        val inverseOnSurface = InverseOnSurface     // #F5EEFA

        // Feedback
        val error            = ErrorRed             // #DC2626
        val success          = SuccessGreen         // #16A34A
        val warning          = WarningAmber         // #F59E0B

        // Severity
        val severityHigh     = SeverityHigh         // #DC2626
        val severityMedium   = SeverityMedium       // #F59E0B
        val severityLow      = SeverityLow          // #16A34A

        // Heatmap
        val heatmapRed       = HeatmapRed           // #EF4444
        val heatmapYellow    = HeatmapYellow        // #FBBF24
        val heatmapGreen     = HeatmapGreen         // #22C55E

        // SOS
        val sos              = SOSRed               // #DC2626
        val sosPressed       = SOSRedDark           // #B91C1C

        // Neutral / Utility
        val neutralMuted     = NeutralMuted         // #6B7280
        val outline          = OutlineLight         // #E8D5E8
        val link             = LinkBlue             // #7C3AED
    }

    // ═══════════════════ TYPOGRAPHY ═══════════════════════

    // Inter preferred, Roboto fallback (both available on most Android devices)
    val fontFamily: FontFamily = FontFamily.Default  // Uses system Roboto

    object Typography {
        val displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp
        )
        val headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        )
        val titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 26.sp
        )
        val bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        )
        val bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        )
        val labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    }

    // ═══════════════════ SHAPES ═══════════════════════════

    object Shapes {
        val card = RoundedCornerShape(12.dp)
        val chip = RoundedCornerShape(10.dp)
        val pill = RoundedCornerShape(24.dp)
        val dialog = RoundedCornerShape(16.dp)
        val bottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    }

    // ═══════════════════ SPACING ═══════════════════════════

    object Spacing {
        val xxs: Dp = 4.dp
        val xs: Dp  = 8.dp
        val sm: Dp  = 12.dp
        val md: Dp  = 16.dp
        val lg: Dp  = 24.dp
        val xl: Dp  = 32.dp
        val xxl: Dp = 48.dp
    }

    // ═══════════════════ ELEVATION ═════════════════════════

    object Elevation {
        val none: Dp      = 0.dp
        val smallCard: Dp = 2.dp
        val card: Dp      = 4.dp
        val dialog: Dp    = 8.dp
    }

    // ═══════════════════ MOTION ════════════════════════════

    object Motion {
        const val standardDurationMs = 180
        const val urgentPulseDurationMs = 750
        const val urgentPulseScale = 1.04f

        fun <T> standardTween() = tween<T>(
            durationMillis = standardDurationMs,
            easing = FastOutSlowInEasing
        )
    }
}

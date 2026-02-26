package com.phantomcrowd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * SafeHer AR unified feminine color scheme.
 * Single palette — soft off-white background, no dark mode.
 * Communicates safety, trust, calmness, femininity, professionalism.
 */
private val SafeHerColorScheme = lightColorScheme(
    primary = PrimaryViolet,           // #C87FAE
    onPrimary = OnPrimary,             // #FFFFFF
    primaryContainer = PrimaryContainer, // #F9E4EF
    secondary = SecondaryRose,          // #E7B7D3
    onSecondary = OnSecondary,          // #0F1724
    tertiary = TertiaryTeal,            // #3FB28F
    background = BackgroundLight,       // #FFF7FA
    surface = SurfaceWhite,             // #FFFFFF
    surfaceVariant = SurfaceVariantLight, // #F9E4EF
    onSurface = OnSurfaceDark,          // #0F1724
    onSurfaceVariant = NeutralMuted,    // #6B7280
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = InverseOnSurface,
    error = ErrorRed,                   // #E34F5A
    outline = OutlineLight,             // #EAE6E9
    outlineVariant = OutlineLight
)

@Composable
fun SafeHerARTheme(
    darkTheme: Boolean = false, // Always use unified light feminine theme
    content: @Composable () -> Unit
) {
    val colorScheme = SafeHerColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

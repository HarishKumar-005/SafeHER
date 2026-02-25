package com.phantomcrowd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * SafeHer AR Light color scheme — Violet + Rose, warm pink surfaces.
 * WCAG AA verified: body text ≥ 4.5:1, large titles ≥ 3:1.
 */
private val SafeHerLightColorScheme = lightColorScheme(
    primary = PrimaryViolet,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = SecondaryRose,
    onSecondary = OnSecondary,
    tertiary = TertiaryTeal,
    background = BackgroundLight,
    surface = SurfaceWhite,
    surfaceVariant = SurfaceVariantLight,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = NeutralMuted,
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = InverseOnSurface,
    error = ErrorRed,
    outline = OutlineLight,
    outlineVariant = OutlineLight
)

/**
 * SafeHer AR Dark color scheme — deeper backgrounds, lighter accents.
 * WCAG AA verified for dark surfaces.
 */
private val SafeHerDarkColorScheme = darkColorScheme(
    primary = PrimaryVioletDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    secondary = SecondaryRoseDark,
    onSecondary = OnSecondaryDark,
    tertiary = TertiaryTealDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = NeutralMutedDark,
    inverseSurface = OnSurfaceLight,
    inverseOnSurface = InverseOnSurfaceDark,
    error = ErrorRedDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark
)

@Composable
fun SafeHerARTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SafeHerDarkColorScheme else SafeHerLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

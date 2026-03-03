package com.phantomcrowd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Light colour scheme ──────────────────────────────────────────────────────
private val SafeHerLightColorScheme = lightColorScheme(
    primary                = PrimaryViolet,
    onPrimary              = OnPrimary,
    primaryContainer       = PrimaryContainer,
    onPrimaryContainer     = OnPrimaryContainer,

    secondary              = SecondaryRose,
    onSecondary            = OnSecondary,
    secondaryContainer     = SecondaryContainer,
    onSecondaryContainer   = OnSecondaryContainer,

    tertiary               = TertiaryTeal,
    onTertiary             = OnTertiary,
    tertiaryContainer      = TertiaryContainer,
    onTertiaryContainer    = OnTertiaryContainer,

    background             = BackgroundLight,
    onBackground           = OnSurfaceLight,

    surface                = SurfaceLight,
    onSurface              = OnSurfaceLight,
    surfaceVariant         = SurfaceVariantLight,
    onSurfaceVariant       = OnSurfaceVariantLight,
    inverseSurface         = InverseSurfaceLight,
    inverseOnSurface       = InverseOnSurfaceLight,

    error                  = ErrorRed,
    onError                = OnErrorColor,
    errorContainer         = ErrorContainer,
    onErrorContainer       = OnErrorContainer,

    outline                = OutlineLight,
    outlineVariant         = OutlineVariantLight,
)

// ─── Dark colour scheme ───────────────────────────────────────────────────────
private val SafeHerDarkColorScheme = darkColorScheme(
    primary                = PrimaryVioletDark,
    onPrimary              = OnPrimaryDark,
    primaryContainer       = PrimaryContainerDark,
    onPrimaryContainer     = OnPrimaryContainerDark,

    secondary              = SecondaryRoseDark,
    onSecondary            = OnSecondaryDark,
    secondaryContainer     = SecondaryContainerDark,
    onSecondaryContainer   = OnSecondaryContainerDark,

    tertiary               = TertiaryTealDark,
    onTertiary             = OnTertiaryDark,
    tertiaryContainer      = TertiaryContainerDark,
    onTertiaryContainer    = OnTertiaryContainerDark,

    background             = BackgroundDark,
    onBackground           = OnSurfaceDark,

    surface                = SurfaceDark,
    onSurface              = OnSurfaceDark,
    surfaceVariant         = SurfaceVariantDark,
    onSurfaceVariant       = OnSurfaceVariantDark,
    inverseSurface         = InverseSurfaceDark,
    inverseOnSurface       = InverseOnSurfaceDark,

    error                  = ErrorRedDark,
    onError                = OnErrorDark,
    errorContainer         = ErrorContainerDark,
    onErrorContainer       = OnErrorContainerDark,

    outline                = OutlineDark,
    outlineVariant         = OutlineVariantDark,
)

/**
 * SafeHer AR theme.
 *
 * @param darkTheme    Follows system setting by default; override in previews.
 * @param dynamicColor Android 12+ Material You dynamic colours — disabled to preserve
 *                     SafeHer AR brand identity (safety + feminine palette).
 */
@Composable
fun SafeHerARTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,          // keep brand palette; not dynamic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> SafeHerDarkColorScheme
        else      -> SafeHerLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: let content draw behind status/nav bars
            WindowCompat.setDecorFitsSystemWindows(window, false)
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

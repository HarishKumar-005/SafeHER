package com.phantomcrowd.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import com.phantomcrowd.R
import com.phantomcrowd.ui.theme.DesignSystem

/**
 * Reusable SafeHer AR logo + title row used at the top of every screen.
 *
 * Displays:
 *   [🛡 launcher icon]  SafeHer AR   [optional trailing content slot]
 *
 * @param modifier       Applied to the outer Row.
 * @param iconSize       Size of the app icon. Default 32dp.
 * @param trailingContent Optional composable slot rendered at end (e.g. a chip).
 */
@Composable
fun AppLogoHeader(
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: icon + app name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "SafeHer AR logo",
                modifier = Modifier.size(iconSize)
            )
            Text(
                text = "SafeHer AR",
                style = DesignSystem.Typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.primary
            )
        }

        // Right: optional chip / badge slot
        trailingContent?.invoke()
    }
}

package com.phantomcrowd.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.phantomcrowd.ui.theme.DesignSystem

/**
 * Tab descriptor for the bottom navigation.
 */
data class PNavTab(
    val index: Int,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Premium bottom navigation bar with soft neutral surface.
 * Center FAB for Post action. Rounded friendly icons.
 */
@Composable
fun PBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onPostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        PNavTab(0, "Nearby",   Icons.Filled.Home,       Icons.Outlined.Home),
        PNavTab(2, "Map",      Icons.Filled.LocationOn, Icons.Outlined.LocationOn),
        PNavTab(3, "Navigate", Icons.Filled.PlayArrow,  Icons.Outlined.PlayArrow),
        PNavTab(5, "Impact",   Icons.Filled.Info,        Icons.Outlined.Info)
    )

    NavigationBar(
        containerColor = DesignSystem.Colors.navSurface,  // Soft off-white #FFF7FA
        tonalElevation = DesignSystem.Elevation.none,     // No harsh shadow
        modifier = modifier
    ) {
        // Left tabs (Nearby, Map)
        tabs.take(2).forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab.index,
                onClick = { onTabSelected(tab.index) },
                icon = {
                    Icon(
                        if (selectedTab == tab.index) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(DesignSystem.Icon.size) // 24dp
                    )
                },
                label = { Text(tab.label, style = DesignSystem.Typography.labelLarge) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DesignSystem.Colors.primary,
                    selectedTextColor = DesignSystem.Colors.primary,
                    unselectedIconColor = DesignSystem.Colors.neutralMuted,
                    unselectedTextColor = DesignSystem.Colors.neutralMuted,
                    indicatorColor = DesignSystem.Colors.selectedCard  // Gentle pink
                )
            )
        }

        // Center FAB
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onPostClick() },
            icon = {
                FloatingActionButton(
                    onClick = onPostClick,
                    containerColor = DesignSystem.Colors.primary,
                    contentColor = DesignSystem.Colors.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = DesignSystem.Elevation.card  // 2dp soft
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Report an issue" }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Post")
                }
            },
            label = { Text("Post", style = DesignSystem.Typography.labelLarge) },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = DesignSystem.Colors.primary,
                unselectedTextColor = DesignSystem.Colors.neutralMuted,
                indicatorColor = DesignSystem.Colors.navSurface // Hide indicator
            )
        )

        // Right tabs (Navigate, Impact)
        tabs.drop(2).forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab.index,
                onClick = { onTabSelected(tab.index) },
                icon = {
                    Icon(
                        if (selectedTab == tab.index) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(DesignSystem.Icon.size)
                    )
                },
                label = { Text(tab.label, style = DesignSystem.Typography.labelLarge) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DesignSystem.Colors.primary,
                    selectedTextColor = DesignSystem.Colors.primary,
                    unselectedIconColor = DesignSystem.Colors.neutralMuted,
                    unselectedTextColor = DesignSystem.Colors.neutralMuted,
                    indicatorColor = DesignSystem.Colors.selectedCard
                )
            )
        }
    }
}

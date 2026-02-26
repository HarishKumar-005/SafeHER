package com.phantomcrowd.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.phantomcrowd.ui.theme.DesignSystem

/**
 * Premium card with soft elevation, no borders.
 * Uses gentle background fill for selected state instead of borders.
 *
 * @param description  Accessibility label read by TalkBack.
 * @param onClick      Optional click handler; card is non-clickable when null.
 * @param isSelected   Whether this card shows the selected (pink fill) state.
 * @param modifier     Outer modifier forwarded to the underlying [Card].
 * @param content      Composable slot rendered inside the card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PCard(
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = if (isSelected) {
        DesignSystem.Colors.selectedCard  // Gentle pink fill #F9E4EF
    } else {
        DesignSystem.Colors.surface       // White #FFFFFF
    }

    val cardModifier = modifier
        .fillMaxWidth()
        .semantics { contentDescription = description }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = DesignSystem.Shapes.card,    // 14dp radius
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = DesignSystem.Elevation.smallCard  // 1dp — very soft
            )
            // No border — clean premium look
        ) {
            Column(modifier = Modifier.padding(DesignSystem.Spacing.md)) {
                content()
            }
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = DesignSystem.Shapes.card,
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = DesignSystem.Elevation.smallCard
            )
        ) {
            Column(modifier = Modifier.padding(DesignSystem.Spacing.md)) {
                content()
            }
        }
    }
}

package com.phantomcrowd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phantomcrowd.ui.theme.DesignSystem

/**
 * Legend component for the Women Safety Risk Map.
 */
@Composable
fun HeatmapLegend(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(DesignSystem.Shapes.card)
            .background(DesignSystem.Colors.surface.copy(alpha = 0.92f))
            .padding(DesignSystem.Spacing.sm)
            .semantics { contentDescription = "Women Safety Risk Map legend: Red means high risk zone, Yellow means medium risk, Green means low risk" }
    ) {
        Text(
            text = "🛡️ Safety Risk Map",
            style = DesignSystem.Typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = DesignSystem.Colors.onSurface
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxs))
        LegendRow(color = DesignSystem.Colors.heatmapRed,    label = "High Risk Zone")
        LegendRow(color = DesignSystem.Colors.heatmapYellow, label = "Medium Risk")
        LegendRow(color = DesignSystem.Colors.heatmapGreen,  label = "Low Risk")
    }
}

@Composable
private fun LegendRow(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
        Text(
            text = label,
            style = DesignSystem.Typography.labelLarge,
            color = DesignSystem.Colors.neutralMuted
        )
    }
}

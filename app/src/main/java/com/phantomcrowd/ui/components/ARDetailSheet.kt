package com.phantomcrowd.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomcrowd.data.AnchorData
import com.phantomcrowd.data.RiskLevel
import com.phantomcrowd.data.RiskScoring
import com.phantomcrowd.ui.theme.DesignSystem

/**
 * AR Detail Bottom Sheet — anchored panel that opens when tapping an inline label.
 *
 * Height: 60% default, draggable up to 85%.
 * Contains: Title (Poppins 20sp), metadata row, scrollable body (Inter 16sp),
 * and pinned action buttons at bottom.
 */
@Composable
fun ARDetailSheet(
    anchor: AnchorData,
    distance: Float,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit = {},
    onSOS: () -> Unit = {},
    onNavigate: () -> Unit = {},
    onReadAloud: () -> Unit = {},
    isReadingAloud: Boolean = false,
    modifier: Modifier = Modifier
) {
    val config = LocalConfiguration.current
    val screenHeightDp = config.screenHeightDp.dp
    val defaultHeight = screenHeightDp * 0.60f
    val maxHeight = screenHeightDp * 0.85f
    var sheetHeight by remember { mutableStateOf(defaultHeight) }

    val ageDays = (System.currentTimeMillis() - anchor.timestamp).toDouble() / (1000 * 60 * 60 * 24)
    val riskLevel = RiskScoring.computeRiskLevel(anchor.severity, anchor.upvotes, ageDays, distance.toDouble())
    val severityLabel = anchor.severity.ifEmpty { "MEDIUM" }.uppercase()
    val distanceText = if (distance < 1000) "${distance.toInt()}m" else "${String.format("%.1f", distance / 1000)}km"
    val timeAgo = formatTimeAgo(anchor.timestamp)

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = androidx.compose.animation.core.tween(200)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = androidx.compose.animation.core.tween(200)
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss)
                .semantics { contentDescription = "Detail sheet overlay, tap to dismiss" },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFFFFF7FA)) // Soft off-white
                    .clickable(enabled = false) {} // Prevent propagation
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            val newHeight = sheetHeight - dragAmount.y.toDp()
                            sheetHeight = newHeight.coerceIn(defaultHeight, maxHeight)
                        }
                    }
            ) {
                // ── Drag handle ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(DesignSystem.Colors.outline)
                    )
                }

                // ── Title row ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Risk dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(getRiskColor(riskLevel))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = anchor.messageText.ifEmpty { anchor.category },
                            style = DesignSystem.Typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.semantics {
                            contentDescription = "Close detail sheet"
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DesignSystem.Colors.neutralMuted
                        )
                    }
                }

                // ── Metadata row ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Distance
                    MetadataChip(text = "📍 $distanceText")
                    // Time
                    MetadataChip(text = "🕐 $timeAgo")
                    // Risk pill
                    RiskPill(riskLevel = riskLevel)
                    // Severity
                    SeverityPill(severity = severityLabel)
                    // Confirmations
                    if (anchor.upvotes > 0) {
                        MetadataChip(text = "${anchor.upvotes}✓", color = DesignSystem.Colors.success)
                    }
                }

                Divider(
                    color = DesignSystem.Colors.outline,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                // ── Scrollable body (supports 300+ words) ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = anchor.messageText.ifEmpty { "No additional details provided." },
                        style = DesignSystem.Typography.bodyLarge,
                        color = DesignSystem.Colors.onSurface,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location info
                    Text(
                        text = "📍 Location: ${String.format("%.4f", anchor.latitude)}, ${String.format("%.4f", anchor.longitude)}",
                        style = DesignSystem.Typography.bodyMedium,
                        color = DesignSystem.Colors.neutralMuted
                    )

                    // Use case
                    Text(
                        text = "🏷️ Category: ${anchor.useCase} — ${anchor.category}",
                        style = DesignSystem.Typography.bodyMedium,
                        color = DesignSystem.Colors.neutralMuted
                    )
                }

                // ── Pinned action buttons ──
                Divider(color = DesignSystem.Colors.outline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(icon = Icons.Default.Check, label = "Confirm", onClick = onConfirm,
                        color = DesignSystem.Colors.success)
                    ActionButton(icon = Icons.Default.Warning, label = "SOS", onClick = onSOS,
                        color = DesignSystem.Colors.sos)
                    ActionButton(icon = Icons.Default.Place, label = "Navigate", onClick = onNavigate,
                        color = DesignSystem.Colors.primary)
                    ActionButton(
                        icon = if (isReadingAloud) Icons.Default.Clear else Icons.Default.PlayArrow,
                        label = if (isReadingAloud) "Pause" else "Read Aloud",
                        onClick = onReadAloud,
                        color = DesignSystem.Colors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(
    text: String,
    color: Color = DesignSystem.Colors.neutralMuted
) {
    Text(
        text = text,
        style = DesignSystem.Typography.labelLarge,
        color = color,
        modifier = Modifier
            .background(DesignSystem.Colors.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun RiskPill(riskLevel: RiskLevel) {
    val (bg, text) = when (riskLevel) {
        RiskLevel.HIGH -> DesignSystem.Colors.error to "HIGH"
        RiskLevel.MEDIUM -> DesignSystem.Colors.warning to "MED"
        RiskLevel.LOW -> DesignSystem.Colors.success to "LOW"
    }
    Text(
        text = text,
        color = Color.White,
        style = DesignSystem.Typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .semantics { contentDescription = "Risk level: $text" }
    )
}

@Composable
private fun SeverityPill(severity: String) {
    val bg = when (severity) {
        "URGENT" -> DesignSystem.Colors.error
        "HIGH" -> DesignSystem.Colors.severityHigh
        "MEDIUM" -> DesignSystem.Colors.severityMedium
        "LOW" -> DesignSystem.Colors.severityLow
        else -> DesignSystem.Colors.neutralMuted
    }
    Text(
        text = severity,
        color = Color.White,
        style = DesignSystem.Typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    )
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .semantics { contentDescription = label }
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(DesignSystem.Icon.size)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = DesignSystem.Typography.labelLarge,
            color = color,
            fontSize = 11.sp
        )
    }
}

private fun getRiskColor(riskLevel: RiskLevel): Color = when (riskLevel) {
    RiskLevel.HIGH -> Color(0xFFE34F5A)
    RiskLevel.MEDIUM -> Color(0xFFF6C85F)
    RiskLevel.LOW -> Color(0xFF3FB28F)
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}

private fun Float.toDp(): androidx.compose.ui.unit.Dp {
    return (this / 2.625f).dp // approximate for typical density
}

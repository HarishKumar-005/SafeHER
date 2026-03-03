package com.phantomcrowd.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomcrowd.data.*
import com.phantomcrowd.ui.components.AppLogoHeader
import com.phantomcrowd.ui.theme.DesignSystem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Impact Dashboard Screen — shows real-time community impact statistics.
 *
 * All data is streamed live from Firestore via [MainViewModel.impactStats].
 * Collections used: issues, surface_anchors, authority_actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpactDashboardScreen(viewModel: MainViewModel) {
    // Observe real-time stats from ViewModel
    val impactStats by viewModel.impactStats.collectAsState()

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    // Expansion state for category cards
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // Start/stop sync with lifecycle
    LaunchedEffect(Unit) {
        viewModel.startImpactDashboardSync()
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopImpactDashboardSync()
        }
    }

    // Handle pull-to-refresh: briefly restart sync
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.stopImpactDashboardSync()
            kotlinx.coroutines.delay(300)
            viewModel.startImpactDashboardSync()
            kotlinx.coroutines.delay(700)
            isRefreshing = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (impactStats == null) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading impact data...")
                }
            }
        } else {
            val stats = impactStats!!

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── Header ─────────────────────────────────────────
                item {
                    AppLogoHeader(iconSize = 28.dp)
                    Text(
                        "How SafeHer AR is making your area safer for women",
                        style = DesignSystem.Typography.bodyMedium,
                        color = DesignSystem.Colors.neutralMuted,
                        modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp)
                    )
                    // Last synced
                    val sdf = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
                    Text(
                        "Last synced: ${sdf.format(Date(stats.lastSyncedMs))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp)
                    )
                }

                // ─── Overall Stats Card ─────────────────────────────
                item {
                    OverallStatsCard(stats)
                }

                // ─── Section header ─────────────────────────────────
                item {
                    Text(
                        "By Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // ─── Category Breakdown Cards ───────────────────────
                if (stats.categoryBreakdowns.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No reports yet. Be the first to make your community safer!",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(
                        stats.categoryBreakdowns,
                        key = { it.category }
                    ) { breakdown ->
                        CategoryStatCard(
                            breakdown = breakdown,
                            isExpanded = expandedCategory == breakdown.category,
                            onToggleExpand = {
                                expandedCategory =
                                    if (expandedCategory == breakdown.category) null
                                    else breakdown.category
                            }
                        )
                    }
                }

                // ─── Your Contribution ──────────────────────────────
                item {
                    YourContributionCard(stats)
                }

                // ─── Success Stories ────────────────────────────────
                item {
                    SuccessStoriesSection(stats.successStories)
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Composable sub-components
// ═══════════════════════════════════════════════════════════════════

/**
 * Overall statistics card with soft pink tinted background.
 */
@Composable
private fun OverallStatsCard(stats: ImpactStats) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DesignSystem.Colors.softPinkCard  // Soft pink fill, not pure white
        ),
        shape = DesignSystem.Shapes.card,  // 14dp radius
        elevation = CardDefaults.cardElevation(
            defaultElevation = DesignSystem.Elevation.smallCard  // 1dp soft
        )
    ) {
        Column(modifier = Modifier.padding(DesignSystem.Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = stats.totalReports.toString(),
                    label = "Unsafe Areas Reported",
                    icon = "🛡️"
                )
                StatItem(
                    value = stats.issuesFixed.toString(),
                    label = "Areas Secured",
                    icon = "✅"
                )
            }
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = stats.redZones.toString(),
                    label = "High Risk Zones",
                    icon = "🔴"
                )
                StatItem(
                    value = formatNumber(stats.estimatedReach),
                    label = "Women Reached",
                    icon = "👩"
                )
            }
        }
    }
}

/**
 * Single stat item — Inter typography.
 */
@Composable
private fun StatItem(value: String, label: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(140.dp)
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxs))
        Text(
            value,
            style = DesignSystem.Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = DesignSystem.Colors.onSurface
        )
        Text(
            label,
            style = DesignSystem.Typography.bodyMedium,
            color = DesignSystem.Colors.neutralMuted,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Expandable card for a single category breakdown.
 */
@Composable
private fun CategoryStatCard(
    breakdown: CategoryBreakdown,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    // Try to resolve a UseCase color, fall back to primary
    val useCaseEnum = UseCase.fromString(breakdown.category)
    val accentColor = useCaseEnum?.color ?: DesignSystem.Colors.primary

    Card(
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(breakdown.icon, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            breakdown.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            "${breakdown.total} reports",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Filled.KeyboardArrowUp
                    else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            // Quick stats (always visible)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStat("Secured", breakdown.fixed.toString(), DesignSystem.Colors.success)
                QuickStat("Pending", breakdown.pending.toString(), DesignSystem.Colors.warning)
                QuickStat("Active", breakdown.inProgress.toString(), DesignSystem.Colors.primary)
                QuickStat(
                    "Rate",
                    "${(breakdown.resolutionRate * 100).toInt()}%",
                    DesignSystem.Colors.primary
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Authority Actions (real data)
                    if (breakdown.recentActions.isNotEmpty()) {
                        Text(
                            "AUTHORITY ACTIONS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        breakdown.recentActions.forEach { action ->
                            val actionIcon = when (action.actionType.uppercase()) {
                                "RESOLVED" -> "✅"
                                "IN_PROGRESS" -> "🔄"
                                "REJECTED" -> "❌"
                                else -> "📋"
                            }
                            val sdf = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
                            val dateStr = if (action.timestamp > 0)
                                sdf.format(Date(action.timestamp)) else ""
                            Text(
                                "$actionIcon ${action.actionType.replace("_", " ")} — ${action.notes.ifEmpty { action.adminEmail }} $dateStr",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            "⏳ Awaiting authority review",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    // Top hotspots
                    if (breakdown.topHotspots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "TOP HOTSPOTS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        breakdown.topHotspots.forEachIndexed { index, hotspot ->
                            Text(
                                "${index + 1}. $hotspot",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick stat pill.
 */
@Composable
private fun QuickStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Your contribution card — soft teal tinted background.
 */
@Composable
private fun YourContributionCard(stats: ImpactStats) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DesignSystem.Colors.softTealCard  // Soft teal fill
        ),
        shape = DesignSystem.Shapes.card,
        elevation = CardDefaults.cardElevation(
            defaultElevation = DesignSystem.Elevation.smallCard
        )
    ) {
        Column(modifier = Modifier.padding(DesignSystem.Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌟", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                Text(
                    "Your Contribution",
                    style = DesignSystem.Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

            Text(
                if (stats.totalReports > 0)
                    "Your reports are helping women stay safe. Keep going!"
                else
                    "Report unsafe areas to help protect women in your community.",
                style = DesignSystem.Typography.bodyMedium,
                color = DesignSystem.Colors.neutralMuted
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐⭐⭐⭐⭐", fontSize = 16.sp)
                    Text(
                        "Community Trust",
                        style = DesignSystem.Typography.bodyMedium,
                        color = DesignSystem.Colors.neutralMuted
                    )
                }
            }
        }
    }
}

/**
 * Success stories section — real resolved issues from authority_actions.
 */
@Composable
private fun SuccessStoriesSection(stories: List<SuccessStory>) {
    Column {
        Text(
            "Safety Wins",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (stories.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📝 No resolved issues yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Reports are being reviewed by authorities. Check back soon!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            stories.forEach { story ->
                val sdf = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
                val dateStr = if (story.resolvedAt > 0)
                    sdf.format(Date(story.resolvedAt)) else ""

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "✨ ${story.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            story.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (dateStr.isNotEmpty()) {
                            Text(
                                dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format large numbers for display.
 */
private fun formatNumber(num: Int): String {
    return when {
        num >= 1000000 -> "${num / 1000000}M+"
        num >= 1000 -> "${num / 1000}K+"
        else -> num.toString()
    }
}

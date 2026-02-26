package com.phantomcrowd.data

import androidx.compose.ui.graphics.Color

/**
 * Developer toggle — set to true to restore all original categories.
 * For SafeHer AR hackathon: women-only mode.
 */
const val DEVELOPER_SHOW_ALL_CATEGORIES = false

/**
 * Use case categories for issue reporting.
 * SafeHer AR: Women's Safety is the ONLY active use case.
 */
enum class UseCase(
    val label: String,
    val icon: String,
    val color: Color,
    val description: String
) {
    WOMENS_SAFETY(
        label = "Women's Safety",
        icon = "👩",
        color = Color(0xFFC87FAE),
        description = "Report safety concerns affecting women"
    );

    companion object {
        fun fromString(value: String?): UseCase? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * Severity levels for issues.
 * Determines priority and visual representation.
 */
enum class Severity(
    val label: String,
    val color: Color,
    val priority: Int,
    val icon: String
) {
    URGENT(
        label = "Urgent",
        color = Color(0xFFE34F5A),
        priority = 1,
        icon = "🔴"
    ),
    HIGH(
        label = "High",
        color = Color(0xFFFF9900),
        priority = 2,
        icon = "🟠"
    ),
    MEDIUM(
        label = "Medium",
        color = Color(0xFFF6C85F),
        priority = 3,
        icon = "🟡"
    ),
    LOW(
        label = "Low",
        color = Color(0xFF3FB28F),
        priority = 4,
        icon = "🟢"
    );

    companion object {
        fun fromString(value: String?): Severity {
            return entries.find { it.name == value } ?: MEDIUM
        }
    }
}

/**
 * Category within Women's Safety use case.
 */
data class Category(
    val id: String,
    val label: String,
    val icon: String = "",
    val description: String = "",
    val defaultSeverity: Severity = Severity.MEDIUM
)

/**
 * Women's Safety subcategories — the ONLY active categories.
 */
object UseCaseCategories {

    private val womensSafetyCategories = listOf(
        Category("ASSAULT", "Assault", "⚠️", "Physical assault or attack", Severity.URGENT),
        Category("HARASSMENT", "Harassment", "🚫", "Verbal or physical harassment", Severity.HIGH),
        Category("STALKING", "Stalking", "👁️", "Being followed or watched", Severity.URGENT),
        Category("UNSAFE_AREA", "Unsafe Area", "🌙", "Poorly lit or dangerous location", Severity.HIGH),
        Category("NO_EMERGENCY_HELP", "No Emergency Help", "🆘", "Lack of emergency services", Severity.URGENT),
        Category("OTHER", "Other", "📝", "Other safety concern", Severity.MEDIUM)
    )

    fun getCategories(useCase: UseCase): List<Category> {
        return when (useCase) {
            UseCase.WOMENS_SAFETY -> womensSafetyCategories
        }
    }

    fun findCategory(useCase: UseCase, categoryId: String): Category? {
        return getCategories(useCase).find { it.id == categoryId }
    }

    fun getAllCategories(): Map<UseCase, List<Category>> {
        return UseCase.entries.associateWith { getCategories(it) }
    }
}

/**
 * Impact messages — women's safety focused.
 */
object ImpactMessages {

    fun getWhyThisMatters(useCase: UseCase): String {
        return when (useCase) {
            UseCase.WOMENS_SAFETY ->
                "Your report helps protect women. When multiple women report the same unsafe area, authorities deploy lights and patrols."
        }
    }

    fun getSuccessMessage(useCase: UseCase): String {
        return when (useCase) {
            UseCase.WOMENS_SAFETY ->
                "Women in this area feel safer because of reports like yours. Thank you!"
        }
    }

    fun getImpactMetricLabel(useCase: UseCase, count: Int): String {
        return when (useCase) {
            UseCase.WOMENS_SAFETY -> "👥 $count women have reported similar issues in this area"
        }
    }
}

/**
 * Sort options for issue lists.
 */
enum class SortOption(val label: String, val icon: String) {
    RECENT("Recent", "🕐"),
    POPULAR("Popular", "👍"),
    URGENT("Urgent", "🚨"),
    NEAREST("Nearest", "📍")
}

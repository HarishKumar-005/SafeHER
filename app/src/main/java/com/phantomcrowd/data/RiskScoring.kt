package com.phantomcrowd.data

import kotlin.math.ln
import kotlin.math.pow

/**
 * Risk score computation for SafeHer AR.
 *
 * Algorithm:
 *   score = base(severity) × (1 + ln(1 + upvotes)) × timeDecay(ageDays) × proximityFactor(distanceMeters)
 *   timeDecay(ageDays) = 0.5^(ageDays / 30)        — half weight every 30 days
 *   proximityFactor    = 1 / (1 + distanceMeters / 100)
 *
 * The raw score is clamped to [0.0, 1.0].
 *
 * Buckets:
 *   LOW  < 0.3
 *   MED  0.3 – 0.6
 *   HIGH > 0.6
 */
object RiskScoring {

    // Base value per severity tag
    private val severityBase = mapOf(
        "URGENT" to 1.0,
        "HIGH"   to 0.7,
        "MEDIUM" to 0.4,
        "LOW"    to 0.2
    )

    /**
     * Compute a risk score in [0.0, 1.0].
     *
     * @param severity   One of URGENT, HIGH, MEDIUM, LOW (case-insensitive)
     * @param upvotes    Community confirmation count (≥ 0)
     * @param ageDays    Age of the report in days (≥ 0)
     * @param distanceMeters  Distance from the user in meters (≥ 0)
     * @return Normalized risk score ∈ [0.0, 1.0]
     */
    fun computeRiskScore(
        severity: String,
        upvotes: Int,
        ageDays: Double,
        distanceMeters: Double
    ): Double {
        val base = severityBase[severity.uppercase()] ?: 0.4
        val upvoteFactor = 1.0 + ln(1.0 + upvotes.coerceAtLeast(0).toDouble())
        val timeDecay = 0.5.pow(ageDays.coerceAtLeast(0.0) / 30.0)
        val proximityFactor = 1.0 / (1.0 + distanceMeters.coerceAtLeast(0.0) / 100.0)

        val raw = base * upvoteFactor * timeDecay * proximityFactor
        // Normalize: the theoretical max is base=1.0, upvoteFactor≈large, decay=1, prox=1
        // In practice we clamp to [0,1]
        return raw.coerceIn(0.0, 1.0)
    }

    /**
     * Map a numeric risk score to a risk level label.
     */
    fun getRiskLevel(score: Double): RiskLevel {
        return when {
            score > 0.6  -> RiskLevel.HIGH
            score >= 0.3 -> RiskLevel.MEDIUM
            else         -> RiskLevel.LOW
        }
    }

    /**
     * Convenience: compute score and return the level directly.
     */
    fun computeRiskLevel(
        severity: String,
        upvotes: Int,
        ageDays: Double,
        distanceMeters: Double
    ): RiskLevel {
        return getRiskLevel(computeRiskScore(severity, upvotes, ageDays, distanceMeters))
    }
}

/**
 * Risk level buckets used throughout the SafeHer AR UI.
 */
enum class RiskLevel(val label: String, val shortLabel: String) {
    HIGH("High Risk", "HIGH"),
    MEDIUM("Medium Risk", "MED"),
    LOW("Low Risk", "LOW")
}

package com.phantomcrowd

import com.phantomcrowd.data.RiskLevel
import com.phantomcrowd.data.RiskScoring
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the RiskScoring algorithm.
 *
 * Each test verifies a specific combination of severity, upvotes,
 * age, and distance to ensure the score falls within the expected
 * risk bucket (HIGH / MEDIUM / LOW).
 */
class RiskScoringTest {

    // ─── Score range tests ───────────────────────────────────────

    @Test
    fun `URGENT severity, fresh, close by — should be HIGH`() {
        val score = RiskScoring.computeRiskScore(
            severity = "URGENT",
            upvotes = 0,
            ageDays = 0.0,
            distanceMeters = 0.0
        )
        // base=1.0, upvote=1+ln1=1.0, decay=1.0, prox=1.0 → 1.0
        assertEquals(1.0, score, 0.01)
        assertEquals(RiskLevel.HIGH, RiskScoring.getRiskLevel(score))
    }

    @Test
    fun `LOW severity, old, far away — should be LOW`() {
        val score = RiskScoring.computeRiskScore(
            severity = "LOW",
            upvotes = 0,
            ageDays = 60.0,
            distanceMeters = 500.0
        )
        // base=0.2, upvote=1.0, decay=0.25, prox=1/6≈0.167 → 0.0083
        assertTrue(score < 0.3)
        assertEquals(RiskLevel.LOW, RiskScoring.getRiskLevel(score))
    }

    @Test
    fun `MEDIUM severity, moderate age, moderate distance — should be MEDIUM or LOW`() {
        val score = RiskScoring.computeRiskScore(
            severity = "MEDIUM",
            upvotes = 3,
            ageDays = 15.0,
            distanceMeters = 200.0
        )
        // Should be in the medium range or below — not HIGH
        assertTrue("Score $score should be <= 0.6", score <= 0.6)
    }

    @Test
    fun `HIGH severity with upvotes, fresh, nearby — should be HIGH`() {
        val score = RiskScoring.computeRiskScore(
            severity = "HIGH",
            upvotes = 10,
            ageDays = 1.0,
            distanceMeters = 50.0
        )
        // base=0.7, upvote=1+ln(11)≈3.4, decay≈0.977, prox=1/1.5≈0.667 → ~1.56 → clamped to 1.0
        assertEquals(RiskLevel.HIGH, RiskScoring.getRiskLevel(score))
    }

    // ─── Edge case tests ─────────────────────────────────────────

    @Test
    fun `score is always between 0 and 1`() {
        val extremeScore = RiskScoring.computeRiskScore(
            severity = "URGENT",
            upvotes = 1000,
            ageDays = 0.0,
            distanceMeters = 0.0
        )
        assertTrue(extremeScore >= 0.0)
        assertTrue(extremeScore <= 1.0)
    }

    @Test
    fun `negative upvotes are treated as 0`() {
        val score = RiskScoring.computeRiskScore(
            severity = "MEDIUM",
            upvotes = -5,
            ageDays = 0.0,
            distanceMeters = 0.0
        )
        val baseline = RiskScoring.computeRiskScore(
            severity = "MEDIUM",
            upvotes = 0,
            ageDays = 0.0,
            distanceMeters = 0.0
        )
        assertEquals(baseline, score, 0.001)
    }

    @Test
    fun `unknown severity falls back to MEDIUM base`() {
        val score = RiskScoring.computeRiskScore(
            severity = "UNKNOWN_LEVEL",
            upvotes = 0,
            ageDays = 0.0,
            distanceMeters = 0.0
        )
        val baseline = RiskScoring.computeRiskScore(
            severity = "MEDIUM",
            upvotes = 0,
            ageDays = 0.0,
            distanceMeters = 0.0
        )
        assertEquals(baseline, score, 0.001)
    }

    @Test
    fun `time decay halves score at 30 days`() {
        val fresh = RiskScoring.computeRiskScore("HIGH", 0, 0.0, 0.0)
        val aged = RiskScoring.computeRiskScore("HIGH", 0, 30.0, 0.0)
        assertEquals(fresh / 2.0, aged, 0.01)
    }

    // ─── Convenience function test ───────────────────────────────

    @Test
    fun `computeRiskLevel returns matching level`() {
        val level = RiskScoring.computeRiskLevel(
            severity = "URGENT",
            upvotes = 5,
            ageDays = 2.0,
            distanceMeters = 10.0
        )
        assertEquals(RiskLevel.HIGH, level)
    }

    @Test
    fun `RiskLevel enum has correct labels`() {
        assertEquals("HIGH", RiskLevel.HIGH.shortLabel)
        assertEquals("MED", RiskLevel.MEDIUM.shortLabel)
        assertEquals("LOW", RiskLevel.LOW.shortLabel)
        assertEquals("High Risk", RiskLevel.HIGH.label)
    }
}

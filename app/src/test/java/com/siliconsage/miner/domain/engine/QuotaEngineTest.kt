package com.siliconsage.miner.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuotaEngineTest {
    @Test
    fun `credit below target advances quota without clearing`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 2.0,
            target = 10.0,
            credit = 3.0
        )

        assertEquals(5.0, result.nextProgress, 0.0001)
        assertEquals(0, result.clearedCount)
    }

    @Test
    fun `credit crossing target clears once and rolls over remainder`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 8.0,
            target = 10.0,
            credit = 5.0
        )

        assertEquals(3.0, result.nextProgress, 0.0001)
        assertEquals(1, result.clearedCount)
    }

    @Test
    fun `large credit can clear multiple quotas`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 4.0,
            target = 10.0,
            credit = 31.0
        )

        assertEquals(5.0, result.nextProgress, 0.0001)
        assertEquals(3, result.clearedCount)
    }

    @Test
    fun `exact target crossing clears once and resets progress`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 7.0,
            target = 10.0,
            credit = 3.0
        )

        assertEquals(0.0, result.nextProgress, 0.0001)
        assertEquals(1, result.clearedCount)
    }

    @Test
    fun `non finite quota credit is ignored safely`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { credit ->
            val result = QuotaEngine.creditProgress(
                currentProgress = 4.0,
                target = 10.0,
                credit = credit
            )

            assertEquals(4.0, result.nextProgress, 0.0001)
            assertEquals(0, result.clearedCount)
        }
    }

    @Test
    fun `zero and negative targets reset safely`() {
        listOf(0.0, -10.0).forEach { target ->
            val result = QuotaEngine.creditProgress(
                currentProgress = 6.0,
                target = target,
                credit = 2.0
            )

            assertEquals(0.0, result.nextProgress, 0.0001)
            assertEquals(0, result.clearedCount)
        }
    }

    @Test
    fun `negative and non finite current progress are clamped safely`() {
        listOf(-5.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { currentProgress ->
            val result = QuotaEngine.creditProgress(
                currentProgress = currentProgress,
                target = 10.0,
                credit = 3.0
            )

            assertEquals(3.0, result.nextProgress, 0.0001)
            assertEquals(0, result.clearedCount)
        }
    }

    @Test
    fun `very large finite credit saturates clears without returning target progress`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 4.0,
            target = 10.0,
            credit = Double.MAX_VALUE
        )

        assertEquals(Int.MAX_VALUE, result.clearedCount)
        assertTrue("nextProgress must be below target after a clear", result.nextProgress < 10.0)
        assertTrue("nextProgress must be non-negative", result.nextProgress >= 0.0)
    }

    @Test
    fun `progress ratio contributes to stability`() {
        val stability = QuotaEngine.calculateSignalStability(
            quotaProgress = 75.0,
            quotaTarget = 100.0,
            ratePressure = 0.2,
            elapsedShiftSeconds = 120L
        )

        assertEquals(0.75, stability, 0.0001)
    }

    @Test
    fun `rate pressure fallback contributes to stability`() {
        val stability = QuotaEngine.calculateSignalStability(
            quotaProgress = 10.0,
            quotaTarget = 100.0,
            ratePressure = 0.6,
            elapsedShiftSeconds = 120L
        )

        assertEquals(0.6, stability, 0.0001)
    }

    @Test
    fun `early shift grace floors stability`() {
        val stability = QuotaEngine.calculateSignalStability(
            quotaProgress = 0.0,
            quotaTarget = 100.0,
            ratePressure = 0.1,
            elapsedShiftSeconds = 30L
        )

        assertEquals(0.7, stability, 0.0001)
    }

    @Test
    fun `negative credit is ignored safely`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 6.0,
            target = 10.0,
            credit = -2.0
        )

        assertEquals(6.0, result.nextProgress, 0.0001)
        assertEquals(0, result.clearedCount)
    }

    @Test
    fun `ignored credit at exact target resets progress below target`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 10.0,
            target = 10.0,
            credit = 0.0
        )

        // Exact target represents a completed quota; ignored credit must not leave progress stuck at target.
        assertEquals(0.0, result.nextProgress, 0.0)
        assertTrue("ignored credit must return progress below target", result.nextProgress < 10.0)
        assertEquals(0, result.clearedCount)
    }

    @Test
    fun `tiny target credit clears once and preserves sub-target remainder`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 0.0,
            target = 1e-12,
            credit = 1.5e-12
        )

        assertEquals(5e-13, result.nextProgress, 1e-24)
        assertEquals(1, result.clearedCount)
    }

    @Test
    fun `non finite target resets safely`() {
        val result = QuotaEngine.creditProgress(
            currentProgress = 6.0,
            target = Double.NaN,
            credit = 2.0
        )

        assertEquals(0.0, result.nextProgress, 0.0001)
        assertEquals(0, result.clearedCount)
    }

    @Test
    fun `quota clear log throttle allows first log and suppresses rapid repeat`() {
        assertTrue(QuotaEngine.shouldEmitQuotaClearLog(nowMs = 1_000L, lastLogMs = 0L, minIntervalMs = 5_000L))
        assertFalse(QuotaEngine.shouldEmitQuotaClearLog(nowMs = 2_000L, lastLogMs = 1_000L, minIntervalMs = 5_000L))
        assertTrue(QuotaEngine.shouldEmitQuotaClearLog(nowMs = 6_000L, lastLogMs = 1_000L, minIntervalMs = 5_000L))
    }

    @Test
    fun `stability clamps non finite and over one inputs safely`() {
        assertEquals(
            1.0,
            QuotaEngine.calculateSignalStability(
                quotaProgress = Double.POSITIVE_INFINITY,
                quotaTarget = 100.0,
                ratePressure = 2.0,
                elapsedShiftSeconds = 120L
            ),
            0.0001
        )
        assertEquals(
            0.0,
            QuotaEngine.calculateSignalStability(
                quotaProgress = Double.NaN,
                quotaTarget = Double.NaN,
                ratePressure = Double.NaN,
                elapsedShiftSeconds = 120L
            ),
            0.0001
        )
    }

    @Test
    fun `stage zero quota target ratchets from ten to fifty after capacity improves`() {
        assertEquals(10.0, QuotaEngine.nextQuotaTarget(storyStage = 0, currentEffectiveRate = 9.99, currentTarget = 10.0), 0.0001)
        assertEquals(50.0, QuotaEngine.nextQuotaTarget(storyStage = 0, currentEffectiveRate = 10.0, currentTarget = 10.0), 0.0001)
        assertEquals(200.0, QuotaEngine.nextQuotaTarget(storyStage = 0, currentEffectiveRate = 50.0, currentTarget = 50.0), 0.0001)
        assertEquals(50.0, QuotaEngine.nextQuotaTarget(storyStage = 0, currentEffectiveRate = 0.0, currentTarget = 50.0), 0.0001)
    }

    @Test
    fun `story stages one through three use fixed positive targets`() {
        assertEquals(15_000.0, QuotaEngine.nextQuotaTarget(storyStage = 1, currentEffectiveRate = 0.0, currentTarget = 10.0), 0.0001)
        assertEquals(500_000.0, QuotaEngine.nextQuotaTarget(storyStage = 2, currentEffectiveRate = 0.0, currentTarget = 10.0), 0.0001)
        assertEquals(10_000_000.0, QuotaEngine.nextQuotaTarget(storyStage = 3, currentEffectiveRate = 0.0, currentTarget = 10.0), 0.0001)
    }

    @Test
    fun `later story stages retain current target or fall back to safe positive target`() {
        assertEquals(123.0, QuotaEngine.nextQuotaTarget(storyStage = 4, currentEffectiveRate = 0.0, currentTarget = 123.0), 0.0001)
        assertEquals(10.0, QuotaEngine.nextQuotaTarget(storyStage = 4, currentEffectiveRate = 0.0, currentTarget = 0.0), 0.0001)
        assertEquals(10.0, QuotaEngine.nextQuotaTarget(storyStage = 4, currentEffectiveRate = 0.0, currentTarget = Double.NaN), 0.0001)
    }

    @Test
    fun `elapsed shift seconds uses nominal shift minus remaining`() {
        assertEquals(0L, QuotaEngine.elapsedShiftSeconds(shiftTimeRemaining = 43_500L))
        assertEquals(0L, QuotaEngine.elapsedShiftSeconds(shiftTimeRemaining = 43_200L))
        assertEquals(60L, QuotaEngine.elapsedShiftSeconds(shiftTimeRemaining = 43_140L))
    }

    @Test
    fun `elapsed shift seconds uses extended total after overtime`() {
        assertEquals(
            60L,
            QuotaEngine.elapsedShiftSeconds(
                shiftTimeRemaining = 86_340L,
                totalShiftSeconds = 86_400L
            )
        )
    }
}

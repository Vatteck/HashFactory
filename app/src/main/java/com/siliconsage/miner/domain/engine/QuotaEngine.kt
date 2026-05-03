package com.siliconsage.miner.domain.engine

import kotlin.math.floor
import kotlin.math.max

/**
 * Pure quota math for GTC quota pressure and hash-buffer alignment.
 *
 * This object intentionally has no Android, ViewModel, time-source, logging, random, or persistence
 * dependencies so quota behavior can be tested independently of UI/runtime side effects.
 */
object QuotaEngine {
    private const val STAGE_ZERO_INITIAL_TARGET = 10.0
    private const val STAGE_ZERO_MID_TARGET = 50.0
    private const val STAGE_ZERO_HIGH_TARGET = 200.0
    private const val STAGE_ONE_TARGET = 15_000.0
    private const val STAGE_TWO_TARGET = 500_000.0
    private const val STAGE_THREE_TARGET = 10_000_000.0
    private const val REMAINDER_EPSILON = 1e-9

    data class QuotaCreditResult(
        val nextProgress: Double,
        val clearedCount: Int
    )

    private fun boundedProgressBelowTarget(progress: Double, target: Double): Double {
        if (!target.isFinite() || target <= 0.0) return 0.0

        val safeProgress = if (progress.isFinite()) progress.coerceAtLeast(0.0) else 0.0
        return if (safeProgress >= target) {
            // Exact target represents a completed quota; avoid returning a sticky target-progress state.
            0.0
        } else {
            safeProgress.coerceIn(0.0, Math.nextDown(target))
        }
    }

    fun creditProgress(
        currentProgress: Double,
        target: Double,
        credit: Double
    ): QuotaCreditResult {
        if (!target.isFinite() || target <= 0.0) {
            return QuotaCreditResult(nextProgress = 0.0, clearedCount = 0)
        }

        val safeCurrent = if (currentProgress.isFinite()) currentProgress.coerceAtLeast(0.0) else 0.0
        if (!credit.isFinite() || credit <= 0.0) {
            return QuotaCreditResult(
                nextProgress = boundedProgressBelowTarget(safeCurrent, target),
                clearedCount = 0
            )
        }

        val total = safeCurrent + credit
        if (!total.isFinite()) {
            return QuotaCreditResult(nextProgress = 0.0, clearedCount = Int.MAX_VALUE)
        }

        val clearRatio = total / target
        if (!clearRatio.isFinite() || clearRatio >= Int.MAX_VALUE.toDouble()) {
            return QuotaCreditResult(nextProgress = 0.0, clearedCount = Int.MAX_VALUE)
        }

        var clearedCount = floor(clearRatio).toInt().coerceAtLeast(0)
        var remainder = total - (clearedCount * target)
        val remainderTolerance = max(REMAINDER_EPSILON * target, Double.MIN_VALUE)
        if (clearedCount > 0 && target - remainder <= remainderTolerance) {
            remainder = 0.0
            if (clearedCount < Int.MAX_VALUE) {
                clearedCount += 1
            }
        }

        return QuotaCreditResult(
            nextProgress = remainder.coerceIn(0.0, Math.nextDown(target)),
            clearedCount = clearedCount
        )
    }

    fun calculateSignalStability(
        quotaProgress: Double,
        quotaTarget: Double,
        ratePressure: Double,
        elapsedShiftSeconds: Long,
        earlyGraceSeconds: Long = 60L,
        earlyGraceFloor: Double = 0.7
    ): Double {
        val progressRatio = if (quotaProgress.isFinite() && quotaTarget.isFinite() && quotaTarget > 0.0) {
            (quotaProgress / quotaTarget).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
        val safeRatePressure = if (ratePressure.isFinite()) ratePressure.coerceIn(0.0, 1.0) else 0.0
        val base = max(progressRatio, safeRatePressure)
        val graceFloor = if (elapsedShiftSeconds in 0 until earlyGraceSeconds) {
            if (earlyGraceFloor.isFinite()) earlyGraceFloor.coerceIn(0.0, 1.0) else 0.0
        } else {
            0.0
        }

        return max(base, graceFloor).coerceIn(0.0, 1.0)
    }

    fun elapsedShiftSeconds(
        shiftTimeRemaining: Long,
        totalShiftSeconds: Long = 43_200L
    ): Long {
        // Source of truth: CoreGameState.shiftTimeTotalSeconds tracks the active shift window,
        // including overtime extensions. Do not derive elapsed time from the nominal shift length
        // after overtime, or the early-grace floor can remain active for hours.
        return (totalShiftSeconds - shiftTimeRemaining).coerceAtLeast(0L)
    }

    fun shouldEmitQuotaClearLog(
        nowMs: Long,
        lastLogMs: Long,
        minIntervalMs: Long
    ): Boolean {
        if (lastLogMs <= 0L) return true
        val safeInterval = minIntervalMs.coerceAtLeast(0L)
        return nowMs - lastLogMs >= safeInterval
    }

    fun nextQuotaTarget(
        storyStage: Int,
        currentEffectiveRate: Double,
        currentTarget: Double
    ): Double {
        val safeCurrentTarget = if (currentTarget.isFinite() && currentTarget > 0.0) currentTarget else STAGE_ZERO_INITIAL_TARGET
        val safeRate = if (currentEffectiveRate.isFinite()) currentEffectiveRate.coerceAtLeast(0.0) else 0.0
        val candidate = when (storyStage) {
            0 -> when {
                safeRate < STAGE_ZERO_INITIAL_TARGET -> STAGE_ZERO_INITIAL_TARGET
                safeRate < STAGE_ZERO_MID_TARGET -> STAGE_ZERO_MID_TARGET
                else -> STAGE_ZERO_HIGH_TARGET
            }
            1 -> STAGE_ONE_TARGET
            2 -> STAGE_TWO_TARGET
            3 -> STAGE_THREE_TARGET
            else -> safeCurrentTarget
        }

        return max(safeCurrentTarget, candidate)
    }
}

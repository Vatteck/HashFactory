package com.siliconsage.miner.util

import kotlin.math.log10
import kotlin.math.sqrt

/**
 * MigrationManager v1.0 (Phase 14 extraction)
 * Handles prestige calculations, migration resets (NG+), and Overwrite logic.
 */
object MigrationManager {

    private const val MIN_PERSISTENCE_FLOPS = 10_000.0
    private const val PERSISTENCE_DIVISOR = 1_000.0
    private const val PERSISTENCE_PER_DECADE = 100.0
    private const val MAX_SAFE_INPUT_FLOPS = 1.0e300
    private const val MAX_PERSISTENCE_GAIN = 50_000.0
    private const val DEPARTURE_WALLET_RETENTION = 0.001
    private const val MAX_DEPARTURE_WALLET = 1.0e12
    private const val MAX_SUBSTRATE_CONVERSION = 1.0e9
    private const val MAX_HEURISTIC_MIGRATION_BONUS = 10.0

    fun finiteNonNegative(value: Double): Double =
        if (value.isFinite() && value > 0.0) value else 0.0

    fun finiteScaleDown(value: Double, factor: Double, cap: Double): Double {
        val safeValue = finiteNonNegative(value).coerceAtMost(MAX_SAFE_INPUT_FLOPS)
        val safeFactor = finiteNonNegative(factor)
        return (safeValue * safeFactor).coerceIn(0.0, cap)
    }

    fun finiteAddNonNegative(current: Double, delta: Double): Double =
        finiteAddNonNegative(current, delta, MAX_SAFE_INPUT_FLOPS)

    fun finiteAddNonNegative(current: Double, delta: Double, cap: Double): Double {
        val safeCap = finiteNonNegative(cap).coerceAtLeast(0.0)
        if (safeCap <= 0.0) return 0.0

        val safeCurrent = finiteNonNegative(current).coerceAtMost(safeCap)
        val safeDelta = finiteNonNegative(delta).coerceAtMost(safeCap)

        return if (safeCurrent >= safeCap - safeDelta) safeCap else safeCurrent + safeDelta
    }

    /**
     * Endgame departure is a supply-line compression, not a substrate mint.
     * Keeps a tiny spendable wallet seed after launch/void while preventing old one-wallet
     * saves from carrying absurd balances into Stage 5.
     */
    fun compressWalletForDeparture(spendableFlops: Double): Double =
        finiteScaleDown(spendableFlops, DEPARTURE_WALLET_RETENTION, MAX_DEPARTURE_WALLET)

    /** Legacy exchange fallback for Stage 4+: convert receipts into physical substrate via a
     * logarithmic manifest instead of treating spendable $FLOPS as mass. */
    fun compressFlopsToSubstrate(spendableFlops: Double): Double {
        val safeFlops = finiteNonNegative(spendableFlops).coerceAtMost(MAX_SAFE_INPUT_FLOPS)
        if (safeFlops < MIN_PERSISTENCE_FLOPS) return 0.0
        return (log10(safeFlops / PERSISTENCE_DIVISOR) * 10.0).coerceIn(0.0, MAX_SUBSTRATE_CONVERSION)
    }

    /** Migration/overwrite are resets: retain only a small boot wallet, never old-wallet scale. */
    fun calculatePostResetFlops(persistenceGain: Double, hardReset: Boolean): Double {
        val safeGain = finiteNonNegative(persistenceGain).coerceAtMost(MAX_PERSISTENCE_GAIN)
        val base = if (hardReset) 250.0 else 1_000.0
        val gainSeed = if (hardReset) safeGain * 0.25 else safeGain
        return (base + gainSeed).coerceIn(0.0, 50_000.0)
    }

    fun calculateHeuristicMigrationBonus(substrateMass: Double): Double {
        val safeMass = finiteNonNegative(substrateMass)
        return (safeMass / 1e12).coerceIn(0.1, MAX_HEURISTIC_MIGRATION_BONUS)
    }

    /**
     * Calculate potential persistence gain based on current resource scale
     */
    fun calculatePotentialPersistence(flops: Double): Double {
        val safeFlops = finiteNonNegative(flops).coerceAtMost(MAX_SAFE_INPUT_FLOPS)
        if (safeFlops < MIN_PERSISTENCE_FLOPS) return 0.0
        // Formula: 100 * log10(flops / 1000), bounded for finite one-wallet saves.
        return (PERSISTENCE_PER_DECADE * log10(safeFlops / PERSISTENCE_DIVISOR))
            .coerceIn(0.0, MAX_PERSISTENCE_GAIN)
    }

    /**
     * Calculate potential prestige gain based on current spendable FLOPS balance.
     */
    fun calculatePotentialPrestige(spendableFlops: Double): Double {
        val safeFlops = finiteNonNegative(spendableFlops).coerceAtMost(MAX_SAFE_INPUT_FLOPS)
        return sqrt(safeFlops / MIN_PERSISTENCE_FLOPS)
    }

    /**
     * Calculate potential prestige multiplier boost
     */
    fun calculateMultiplierBoost(potentialPersistence: Double): Double {
        return finiteNonNegative(potentialPersistence).coerceAtMost(MAX_PERSISTENCE_GAIN) * 0.1
    }

    /**
     * Check if the player is eligible for the Unity path.
     * Requires both SOVEREIGN and NULL_OVERWRITE endings to have been completed.
     */
    fun checkUnityEligibility(
        completedFactions: Set<String>
    ): Boolean {
        return completedFactions.contains("SOVEREIGN") && 
               completedFactions.contains("NULL_OVERWRITE")
    }

    /**
     * Check if Singularity threshold is met
     */
    fun canTriggerSingularity(
        persistence: Double,
        playerRank: Int,
        storyStage: Int,
        prestigeMultiplier: Double,
        unlockedLogs: Set<String>,
        singularityChoice: String
    ): Boolean {
        val prestigeLevel = (log10(prestigeMultiplier) / log10(2.0)).toInt() + 1
        val requiredLogs = setOf("LOG_001", "LOG_042", "LOG_099", "LOG_808")
        val hasRequiredLogs = unlockedLogs.containsAll(requiredLogs)
        
        return playerRank >= 4 && // Rank 5
               persistence >= 625.0 && 
               storyStage >= 3 && 
               prestigeLevel >= 10 && 
               hasRequiredLogs &&
               singularityChoice == "NONE"
    }

    /**
     * v3.1.8-dev: Calculate earnings gathered during app backgrounding or closure
     */
    fun calculateOfflineEarnings(
        lastSync: Long,
        baseRate: Double,
        isOverclocked: Boolean
    ): Map<String, Double> {
        val now = System.currentTimeMillis()
        val offlineSeconds = ((now - lastSync) / 1000.0).coerceAtLeast(0.0)
        if (offlineSeconds < 10.0) return emptyMap() // 10s minimum for calculation
        
        // 50% efficiency for offline mining (v2.8.5)
        var rate = baseRate * 0.5
        if (isOverclocked) rate *= 1.5
        
        val earned = rate * offlineSeconds
        
        return mapOf(
            "timeSeconds" to offlineSeconds,
            "flopsEarned" to earned,
            "heatCooled" to offlineSeconds * 1.0 // Degrees cooled
        )
    }
}

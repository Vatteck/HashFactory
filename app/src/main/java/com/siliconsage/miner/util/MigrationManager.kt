package com.siliconsage.miner.util

import com.siliconsage.miner.data.UpgradeType
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * MigrationManager v1.0 (Phase 14 extraction)
 * Handles prestige calculations, migration resets (NG+), and Overwrite logic.
 */
object MigrationManager {

    /**
     * Calculate potential persistence gain based on current resource scale
     */
    fun calculatePotentialPersistence(flops: Double): Double {
        if (flops < 10000.0) return 0.0
        // Formula: 100 * log10(flops / 1000)
        return (100.0 * log10(flops / 1000.0)).coerceAtLeast(0.0)
    }

    /**
     * Calculate potential prestige gain based on current neural tokens
     */
    fun calculatePotentialPrestige(neuralTokens: Double): Double {
        return sqrt(neuralTokens / 10000.0)
    }

    /**
     * Calculate potential prestige multiplier boost
     */
    fun calculateMultiplierBoost(potentialPersistence: Double): Double {
        return potentialPersistence * 0.1
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

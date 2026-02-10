package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.math.pow

/**
 * SimulationManager v1.0 (Phase 14 extraction)
 * Handles player-triggered simulation actions like overclocking, purging, and repairs.
 */
object SimulationManager {

    /**
     * Calculate the cost to repair hardware integrity
     */
    fun calculateRepairCost(
        currentIntegrity: Double,
        location: String,
        storyStage: Int,
        playerRank: Int,
        assaultPhase: String
    ): Double {
        val damage = 100.0 - currentIntegrity
        if (damage <= 0) return 0.0
        
        val stageMultiplier = 2.0.pow(storyStage.toDouble().coerceAtLeast(0.0))
        val rankFactor = (playerRank + 1).toDouble()
        
        var cost = damage * 10.0 * rankFactor * stageMultiplier
        
        // v3.0.2: Endgame Resource Scaling
        if (location == "VOID_INTERFACE" || location == "ORBITAL_SATELLITE") {
            cost *= 1e12 
        }
        
        if (assaultPhase != "NOT_STARTED") {
            cost *= 0.1 // 90% discount during assault
        }
        
        return cost
    }

    /**
     * Get the currency bucket for repairs based on location
     */
    fun getRepairCurrency(
        location: String,
        neuralTokens: Double,
        celestialData: Double,
        voidFragments: Double
    ): Double {
        return when (location) {
            "ORBITAL_SATELLITE" -> celestialData
            "VOID_INTERFACE" -> voidFragments
            else -> neuralTokens
        }
    }
}

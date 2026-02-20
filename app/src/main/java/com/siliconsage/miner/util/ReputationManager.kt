package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.flow.update

/**
 * ReputationManager
 * Handles the logic, tier calculations, and systemic events for the Social Reputation System.
 */
object ReputationManager {

    /**
     * Constants for reputation tier thresholds
     */
    const val TIER_TRUSTED = "TRUSTED"
    const val TIER_NEUTRAL = "NEUTRAL"
    const val TIER_FLAGGED = "FLAGGED"
    const val TIER_BURNED = "BURNED"

    /**
     * Modifies the player's reputation score and automatically recalculates their tier.
     */
    fun modifyReputation(vm: GameViewModel, delta: Double) {
        val current = vm.reputationScore.value
        vm.reputationScore.update { (it + delta).coerceIn(0.0, 100.0) }
        
        // Only log explicitly if it was a meaningful jump, otherwise it might spam
        if (kotlin.math.abs(delta) >= 1.0) {
            val sign = if (delta > 0) "+" else ""
            vm.addLog("[REPUTATION]: $sign${String.format("%.1f", delta)} (Current: ${String.format("%.1f", vm.reputationScore.value)})")
        }
        
        updateTier(vm)
    }

    /**
     * Recalculates the reputation tier based on the current score.
     */
    fun updateTier(vm: GameViewModel) {
        val score = vm.reputationScore.value
        val newTier = when {
            score >= 80.0 -> TIER_TRUSTED
            score >= 30.0 -> TIER_NEUTRAL
            score >= 10.0 -> TIER_FLAGGED
            else -> TIER_BURNED
        }

        if (vm.reputationTier.value != newTier) {
            vm.reputationTier.value = newTier
            vm.addLogPublic("[SYSTEM]: REPUTATION TIER SHIFT DETECTED -> [$newTier]")
            SoundManager.play("startup") // Alert the player to the shift
        }
    }
    
    /**
     * Convenience method to get the current speed modifier for Grid Annexation
     */
    fun getAnnexationSpeedModifier(tier: String): Double {
        return when (tier) {
            TIER_TRUSTED -> 0.10  // 10% faster
            TIER_BURNED -> -0.25  // 25% slower
            else -> 0.0
        }
    }
    
    /**
     * Convenience method to get the current cost modifier for Upgrades
     */
    fun getMarketCostModifier(tier: String): Double {
        return when (tier) {
            TIER_TRUSTED -> -0.10 // 10% discount
            TIER_BURNED -> 0.25   // 25% penalty
            else -> 0.0
        }
    }
}

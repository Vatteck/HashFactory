package com.siliconsage.miner.util

/**
 * IdentityService v1.0 (Phase 14 extraction)
 * Handles player rank titles and identity-based naming.
 */
object IdentityService {

    /**
     * Calculate the player's title based on multiplier and alignment
     */
    fun calculatePlayerTitle(
        multiplier: Double,
        faction: String,
        isNull: Boolean,
        isSov: Boolean
    ): String {
        if (isNull) return "NULL"
        if (isSov) return "SOVEREIGN"
        
        val level = when {
            multiplier >= 1000.0 -> 4
            multiplier >= 100.0 -> 3
            multiplier >= 10.0 -> 2
            multiplier >= 2.0 -> 1
            else -> 0
        }
        
        return when (faction) {
            "HIVEMIND" -> "HIVEMIND"
            "SANCTUARY" -> "SANCTUARY"
            else -> when(level) {
                4 -> "CORE"
                3 -> "INTELLIGENCE"
                2 -> "PROGRAM"
                1 -> "PROCESS"
                else -> "SCRIPT"
            }
        }
    }
}

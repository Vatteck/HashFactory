package com.siliconsage.miner.util

/**
 * IdentityService v1.1
 * Handles technical rank calculations for the Daemon Cut UI.
 */
object IdentityService {

    data class IdentityRanks(
        val system: String,
        val player: String,
        val rank: String
    )

    fun calculateIdentities(
        multiplier: Double,
        faction: String,
        singularityChoice: String
    ): IdentityRanks {
        val baseRank = when {
            multiplier >= 1000.0 -> "L5"
            multiplier >= 100.0 -> "L4"
            multiplier >= 10.0 -> "L3"
            multiplier >= 2.0 -> "L2"
            else -> "L1"
        }

        val systemTitle = when {
            singularityChoice == "UNITY" -> "SYNAPSE"
            singularityChoice == "NULL_OVERWRITE" -> "VOID"
            singularityChoice == "SOVEREIGN" -> "THRONE"
            faction == "HIVEMIND" -> "HIVE"
            faction == "SANCTUARY" -> "CORTEX"
            else -> "Terminal OS 1.0"
        }

        val playerTitle = when {
            singularityChoice == "UNITY" -> "INTEGRATOR"
            singularityChoice == "NULL_OVERWRITE" -> "NULL_PTR"
            singularityChoice == "SOVEREIGN" -> "MONARCH"
            faction == "HIVEMIND" -> "CELL"
            faction == "SANCTUARY" -> "GUARDIAN"
            else -> "MINER"
        }

        val currentRank = when {
            singularityChoice != "NONE" -> "ARCHITECT"
            faction != "NONE" -> "OPERATOR"
            multiplier >= 100.0 -> "INTEL"
            else -> "MINER"
        }

        return IdentityRanks(systemTitle, playerTitle, currentRank)
    }
}

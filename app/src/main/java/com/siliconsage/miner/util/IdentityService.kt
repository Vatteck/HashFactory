package com.siliconsage.miner.util

import com.siliconsage.miner.data.UpgradeType

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
        singularityChoice: String,
        upgrades: Map<UpgradeType, Int> = emptyMap()
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
            else -> "CONTRACTOR"
        }

        val securityLevel = upgrades.entries.filter { it.key.isSecurity }.sumOf { it.value }

        val currentRank = when {
            singularityChoice != "NONE" -> "ARCHITECT"
            faction != "NONE" -> "OPERATOR"
            multiplier >= 1000.0 -> "EXECUTIVE"
            multiplier >= 100.0 -> "DIRECTOR"
            multiplier >= 10.0 -> "SUPERVISOR"
            multiplier >= 2.0 -> "SENIOR"
            else -> "JUNIOR"
        }

        return IdentityRanks(systemTitle, playerTitle, currentRank)
    }
}

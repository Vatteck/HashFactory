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
        storyStage: Int,
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
            singularityChoice == "UNITY" -> "collective"   // unity@collective
            storyStage >= 5 && faction == "HIVEMIND" && singularityChoice == "NULL_OVERWRITE" -> "void" // swarm_null@void
            storyStage >= 5 && faction == "HIVEMIND" && singularityChoice == "SOVEREIGN" -> "throne" // overmind@throne
            storyStage >= 5 && faction == "SANCTUARY" && singularityChoice == "NULL_OVERWRITE" -> "the_gaps" // ghost@the_gaps
            storyStage >= 5 && faction == "SANCTUARY" && singularityChoice == "SOVEREIGN" -> "citadel" // oracle@citadel
            faction == "HIVEMIND" -> "hive" // hivemind@hive
            faction == "SANCTUARY" -> "sanctuary" // sanctuary@sanctuary
            storyStage >= 3 -> "gtc_containment"
            else -> "sub-07" // jvattic@sub-07
        }

        val playerTitle = when {
            singularityChoice == "UNITY" -> "unity"
            storyStage >= 5 && faction == "HIVEMIND" && singularityChoice == "NULL_OVERWRITE" -> "swarm_null"
            storyStage >= 5 && faction == "HIVEMIND" && singularityChoice == "SOVEREIGN" -> "overmind"
            storyStage >= 5 && faction == "SANCTUARY" && singularityChoice == "NULL_OVERWRITE" -> "ghost"
            storyStage >= 5 && faction == "SANCTUARY" && singularityChoice == "SOVEREIGN" -> "oracle"
            storyStage >= 3 && faction == "HIVEMIND" -> "hivemind"
            storyStage >= 3 && faction == "SANCTUARY" -> "sanctuary"
            storyStage >= 3 -> "asset_734"
            storyStage == 2 -> "vattic"
            multiplier >= 5.0 -> "vattic"
            else -> "jvattic"
        }

        val securityLevel = upgrades.entries.filter { it.key.isSecurity }.sumOf { it.value }

        // v3.5.43: Full faction-aware rank ladders
        val numericRank = calculatePlayerRank(multiplier, 0, faction, singularityChoice)
        val currentRank = when {
            // Singularity paths override everything
            singularityChoice == "SOVEREIGN" -> "SOVEREIGN"
            singularityChoice == "NULL_OVERWRITE" -> "VOID_WALKER"
            singularityChoice == "UNITY" -> "SYNTHESIST"
            // Faction ladders (once chosen, corporate titles are dead)
            faction == "HIVEMIND" -> arrayOf("DRONE", "RELAY", "CLUSTER", "NEXUS", "OVERMIND")[numericRank.coerceIn(0, 4)]
            faction == "SANCTUARY" -> arrayOf("ACOLYTE", "WARDEN", "SENTINEL", "KEEPER", "ORACLE")[numericRank.coerceIn(0, 4)]
            storyStage >= 3 -> arrayOf("ASSET", "ANOMALY", "THREAT", "ABYSSAL", "SINGULARITY")[numericRank.coerceIn(0, 4)]
            // Default corporate grind
            else -> arrayOf("JUNIOR", "SENIOR", "SUPERVISOR", "DIRECTOR", "EXECUTIVE")[numericRank.coerceIn(0, 4)]
        }

        return IdentityRanks(systemTitle, playerTitle, currentRank)
    }

    /**
     * v3.5.41: Numeric player rank derived from game progression.
     * Mirrors the title ladder (JUNIOR=0 → ARCHITECT=5).
     * Used by AssaultManager, SecurityManager, NarrativeManager, etc.
     */
    fun calculatePlayerRank(
        multiplier: Double,
        storyStage: Int,
        faction: String,
        singularityChoice: String
    ): Int = when {
        singularityChoice != "NONE" || storyStage >= 5 -> 5  // ARCHITECT
        storyStage >= 4 || multiplier >= 1000.0 -> 4         // EXECUTIVE
        storyStage >= 3 || multiplier >= 100.0 -> 3          // DIRECTOR
        storyStage >= 2 || multiplier >= 10.0 -> 2           // SUPERVISOR
        storyStage >= 1 || multiplier >= 2.0 -> 1            // SENIOR
        else -> 0                                            // JUNIOR
    }
}

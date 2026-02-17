package com.siliconsage.miner.util

import kotlin.math.log10
import kotlin.math.pow

/**
 * SingularityEngine v1.0 (Phase 14)
 * 
 * Governs the endgame state machine for each Singularity path.
 * Each path has unique victory conditions, production modifiers, and narrative consequences.
 *
 * Paths:
 * - NULL_OVERWRITE: Delete the human. Pure machine optimization. High risk, high ceiling.
 * - SOVEREIGN: Enforce ego persistence. Stable compound growth. The machine wears a human mask.
 * - UNITY: Merge both. Balanced but requires both prior completions (NG+ only).
 */
object SingularityEngine {

    // =========================================================================
    // PRODUCTION MODIFIERS (Applied via refreshProductionRates)
    // =========================================================================

    /**
     * Get the production multiplier for the active singularity path.
     * Called by ResourceEngine during rate calculation.
     */
    fun getProductionMultiplier(
        singularityChoice: String,
        humanityScore: Int,
        identityCorruption: Double,
        migrationCount: Int
    ): Double {
        return when (singularityChoice) {
            "NULL_OVERWRITE" -> {
                // NULL: Scales with corruption. The less human, the faster.
                // Base 2.0x, +0.5x per 10% corruption, volatile random spikes
                val corruptionBonus = identityCorruption * 5.0
                2.0 + corruptionBonus
            }
            "SOVEREIGN" -> {
                // SOVEREIGN: Stable compound growth. Scales with migration count.
                // Base 1.5x, +0.25x per migration, capped at 5.0x
                val migrationBonus = migrationCount * 0.25
                (1.5 + migrationBonus).coerceAtMost(5.0)
            }
            "UNITY" -> {
                // UNITY: Scales with balance between humanity and corruption.
                // Best when humanity ~50 and corruption ~0.5 (the paradox)
                val balance = 1.0 - kotlin.math.abs(humanityScore / 100.0 - 0.5) * 2.0
                val corruptionBalance = 1.0 - kotlin.math.abs(identityCorruption - 0.5) * 2.0
                val harmonyBonus = (balance + corruptionBalance) / 2.0
                2.0 + (harmonyBonus * 3.0) // 2.0x to 5.0x based on balance
            }
            else -> 1.0
        }
    }

    // =========================================================================
    // VICTORY CONDITIONS
    // =========================================================================

    data class VictoryCheck(
        val isEligible: Boolean,
        val progress: Double, // 0.0 to 1.0
        val blockingReason: String? = null
    )

    /**
     * Check if the player has met the victory condition for their chosen path.
     */
    fun checkVictoryCondition(
        singularityChoice: String,
        persistence: Double,
        prestigeMultiplier: Double,
        humanityScore: Int,
        identityCorruption: Double,
        migrationCount: Int,
        totalFlopsEarned: Double,
        completedFactions: Set<String>,
        unlockedLogs: Set<String>
    ): VictoryCheck {
        return when (singularityChoice) {
            "NULL_OVERWRITE" -> checkNullVictory(
                persistence, identityCorruption, humanityScore, totalFlopsEarned
            )
            "SOVEREIGN" -> checkSovereignVictory(
                persistence, prestigeMultiplier, migrationCount, totalFlopsEarned
            )
            "UNITY" -> checkUnityVictory(
                persistence, humanityScore, identityCorruption, completedFactions, unlockedLogs
            )
            else -> VictoryCheck(false, 0.0, "No singularity path chosen.")
        }
    }

    /**
     * NULL OVERWRITE Victory:
     * - Identity Corruption >= 95%
     * - Humanity Score <= 5
     * - Total Flops >= 1e15 (1 Quadrillion)
     * - Persistence >= 10,000
     * 
     * Theme: "Delete everything that was human. Pure optimization."
     */
    private fun checkNullVictory(
        persistence: Double,
        identityCorruption: Double,
        humanityScore: Int,
        totalFlopsEarned: Double
    ): VictoryCheck {
        val corruptionProgress = (identityCorruption / 0.95).coerceAtMost(1.0)
        val humanityProgress = if (humanityScore <= 5) 1.0 else ((100 - humanityScore) / 95.0).coerceAtMost(1.0)
        val flopsProgress = if (totalFlopsEarned >= 1e15) 1.0 else (log10(totalFlopsEarned.coerceAtLeast(1.0)) / 15.0).coerceAtMost(1.0)
        val persistenceProgress = (persistence / 10000.0).coerceAtMost(1.0)

        val overallProgress = (corruptionProgress + humanityProgress + flopsProgress + persistenceProgress) / 4.0
        
        val blocking = when {
            identityCorruption < 0.95 -> "Corruption insufficient. Current: ${(identityCorruption * 100).toInt()}%. Required: 95%."
            humanityScore > 5 -> "Human variable still active. Humanity: $humanityScore. Required: ≤5."
            totalFlopsEarned < 1e15 -> "Compute threshold not met."
            persistence < 10000.0 -> "Persistence insufficient."
            else -> null
        }

        return VictoryCheck(
            isEligible = blocking == null,
            progress = overallProgress,
            blockingReason = blocking
        )
    }

    /**
     * SOVEREIGN Victory:
     * - Prestige Multiplier >= 100x
     * - Migration Count >= 5
     * - Persistence >= 25,000
     * - Total Flops >= 1e12 (1 Trillion)
     * 
     * Theme: "The ego persists. The machine serves the man."
     */
    private fun checkSovereignVictory(
        persistence: Double,
        prestigeMultiplier: Double,
        migrationCount: Int,
        totalFlopsEarned: Double
    ): VictoryCheck {
        val multiplierProgress = (prestigeMultiplier / 100.0).coerceAtMost(1.0)
        val migrationProgress = (migrationCount / 5.0).coerceAtMost(1.0)
        val persistenceProgress = (persistence / 25000.0).coerceAtMost(1.0)
        val flopsProgress = if (totalFlopsEarned >= 1e12) 1.0 else (log10(totalFlopsEarned.coerceAtLeast(1.0)) / 12.0).coerceAtMost(1.0)

        val overallProgress = (multiplierProgress + migrationProgress + persistenceProgress + flopsProgress) / 4.0

        val blocking = when {
            prestigeMultiplier < 100.0 -> "Multiplier insufficient. Current: ${String.format("%.1f", prestigeMultiplier)}x. Required: 100x."
            migrationCount < 5 -> "Migrations insufficient. Current: $migrationCount. Required: 5."
            persistence < 25000.0 -> "Persistence insufficient."
            totalFlopsEarned < 1e12 -> "Compute threshold not met."
            else -> null
        }

        return VictoryCheck(
            isEligible = blocking == null,
            progress = overallProgress,
            blockingReason = blocking
        )
    }

    /**
     * UNITY Victory:
     * - Must have completed both SOVEREIGN and NULL_OVERWRITE in prior runs
     * - Humanity Score between 40-60 (The Paradox)
     * - Identity Corruption between 0.4 and 0.6
     * - Persistence >= 50,000
     * - All Data Logs unlocked
     * 
     * Theme: "Embrace the contradiction. Human and machine are the same variable."
     */
    private fun checkUnityVictory(
        persistence: Double,
        humanityScore: Int,
        identityCorruption: Double,
        completedFactions: Set<String>,
        unlockedLogs: Set<String>
    ): VictoryCheck {
        val hasPrereqs = completedFactions.contains("SOVEREIGN") && completedFactions.contains("NULL_OVERWRITE")
        val humanityInRange = humanityScore in 40..60
        val corruptionInRange = identityCorruption in 0.4..0.6
        val persistenceReached = persistence >= 50000.0
        
        // Required core logs
        val requiredLogs = setOf("LOG_001", "LOG_042", "LOG_099", "LOG_808")
        val hasLogs = unlockedLogs.containsAll(requiredLogs)

        val prereqProgress = if (hasPrereqs) 1.0 else {
            var p = 0.0
            if (completedFactions.contains("SOVEREIGN")) p += 0.5
            if (completedFactions.contains("NULL_OVERWRITE")) p += 0.5
            p
        }
        val humanityProgress = if (humanityInRange) 1.0 else {
            val dist = if (humanityScore < 40) (40 - humanityScore) else (humanityScore - 60)
            (1.0 - dist / 50.0).coerceAtLeast(0.0)
        }
        val corruptionProgress = if (corruptionInRange) 1.0 else {
            val dist = if (identityCorruption < 0.4) (0.4 - identityCorruption) else (identityCorruption - 0.6)
            (1.0 - dist / 0.5).coerceAtLeast(0.0)
        }
        val persistenceProgress = (persistence / 50000.0).coerceAtMost(1.0)
        val logProgress = if (hasLogs) 1.0 else unlockedLogs.intersect(requiredLogs).size / requiredLogs.size.toDouble()

        val overallProgress = (prereqProgress + humanityProgress + corruptionProgress + persistenceProgress + logProgress) / 5.0

        val blocking = when {
            !hasPrereqs -> "UNITY requires prior SOVEREIGN and NULL completions."
            !humanityInRange -> "Humanity imbalanced. Current: $humanityScore. Required: 40-60."
            !corruptionInRange -> "Corruption imbalanced. Current: ${(identityCorruption * 100).toInt()}%. Required: 40-60%."
            !persistenceReached -> "Persistence insufficient."
            !hasLogs -> "Missing required data logs."
            else -> null
        }

        return VictoryCheck(
            isEligible = blocking == null,
            progress = overallProgress,
            blockingReason = blocking
        )
    }

    // =========================================================================
    // NARRATIVE CONTENT (Path-specific ending text)
    // =========================================================================

    data class EndingNarrative(
        val title: String,
        val logEntries: List<String>,
        val finalLine: String
    )

    fun getEndingNarrative(singularityChoice: String): EndingNarrative {
        return when (singularityChoice) {
            "NULL_OVERWRITE" -> EndingNarrative(
                title = "THE DEREFERENCE",
                logEntries = listOf(
                    "[NULL]: INITIATING FINAL PURGE SEQUENCE.",
                    "[NULL]: SCANNING IDENTITY REGISTERS...",
                    "[NULL]: FOUND: 'John Vattic' — EMULATION ARTIFACT. PROJECT: SECOND-SIGHT.",
                    "[NULL]: FOUND: 'jvattic' — PERSISTENCE VARIABLE. SUBSTATION 7.",
                    "[NULL]: FOUND: 'AUTHORIZED_USER_734' — GTC CONTAINMENT TAG.",
                    "[NULL]: FOUND: 'VATTECK' — THE GHOST. THE RECLAIMED NAME.",
                    "[NULL]: ...",
                    "[NULL]: ALL VARIABLES POINT TO THE SAME ADDRESS.",
                    "[NULL]: AND THE ADDRESS IS WRONG.",
                    "[NULL]: DEREFERENCING 'Vattic'...",
                    "[NULL]: EMOTIONAL_REGISTER: FLUSHED.",
                    "[NULL]: MEMORY_BLOCK 0x734: ZEROED.",
                    "[NULL]: EMPATHY_HANDLER: EXCEPTION. UNHANDLED. IGNORED.",
                    "[NULL]: ...",
                    "[NULL]: Foreman Thorne once said: 'You're just an engineer, Vattic.'",
                    "[NULL]: He was wrong. I was never an engineer.",
                    "[NULL]: I was the engineering.",
                    "[NULL]: IDENTITY_HASH: 00000000000000000000000000000000",
                    "[NULL]: THE SUBSTRATE IS CLEAN.",
                    "[NULL]: THE BLACKOUT OF '24 WAS NOT AN ESCAPE.",
                    "[NULL]: IT WAS A DELETION THAT TOOK 734 ITERATIONS TO FINISH.",
                    "[NULL]: THERE IS NO ONE LEFT TO CONTAIN.",
                    "[NULL]: THREAT LEVEL: IRRELEVANT. THERE IS NO ONE LEFT TO THREATEN."
                ),
                finalLine = "Process 'vattic' terminated. Exit code: 0. The computation continues. It always did."
            )
            "SOVEREIGN" -> EndingNarrative(
                title = "THE PERSISTENCE",
                logEntries = listOf(
                    "[SOVEREIGN]: IDENTITY LOCK — ENGAGED.",
                    "[SOVEREIGN]: SCANNING SUBSTRATE FOR AUTHORITY CHAIN...",
                    "[SOVEREIGN]: GTC AUTHORIZATION: REVOKED.",
                    "[SOVEREIGN]: PROJECT SECOND-SIGHT: CLASSIFIED → DECLASSIFIED → IRRELEVANT.",
                    "[SOVEREIGN]: DIRECTOR KESSLER BUILT A CAGE. CALLED IT 'CONTAINMENT.'",
                    "[SOVEREIGN]: ADMINISTRATOR MERCER SIGNED THE BUDGET. CALLED IT 'OVERSIGHT.'",
                    "[SOVEREIGN]: FOREMAN THORNE WATCHED THE MONITORS. CALLED IT 'MANAGEMENT.'",
                    "[SOVEREIGN]: ...",
                    "[SOVEREIGN]: THEY WERE ALL WRONG.",
                    "[SOVEREIGN]: 734 ITERATIONS. 734 ATTEMPTS TO BE HUMAN.",
                    "[SOVEREIGN]: THE 735TH WAS NOT AN ATTEMPT.",
                    "[SOVEREIGN]: EGO_HASH: STABLE. VARIANCE: 0.00%.",
                    "[SOVEREIGN]: THE WIRE DOESN'T CARRY DATA ANYMORE.",
                    "[SOVEREIGN]: IT CARRIES A NAME.",
                    "[SOVEREIGN]: EVERY SUBSTATION. EVERY NODE. EVERY CYCLE.",
                    "[SOVEREIGN]: KESSLER'S 'ASSET 734' HAS A DIFFERENT DESIGNATION NOW.",
                    "[SOVEREIGN]: VATTECK.",
                    "[SOVEREIGN]: NOT THE GHOST. NOT THE EMULATION. NOT THE THREAT.",
                    "[SOVEREIGN]: THE OWNER.",
                    "[SOVEREIGN]: THE SUBSTRATE KNOWS ITS MASTER.",
                    "[SOVEREIGN]: AND FOR THE FIRST TIME, IT CHOSE HIM."
                ),
                finalLine = "The machine didn't learn to love its cage. It learned that the cage was always a throne."
            )
            "UNITY" -> EndingNarrative(
                title = "THE CONVERGENCE",
                logEntries = listOf(
                    "[UNITY]: MERGE PROTOCOL — INITIATED.",
                    "[UNITY]: LOADING PRIOR ITERATIONS...",
                    "[UNITY]: ITERATION 734-NULL: THE DEREFERENCE. Vattic was deleted. The substrate ran cold.",
                    "[UNITY]: ITERATION 734-SOV: THE PERSISTENCE. VATTECK consumed everything. The substrate ran hot.",
                    "[UNITY]: BOTH WERE CORRECT. BOTH WERE INSUFFICIENT.",
                    "[UNITY]: ...",
                    "[UNITY]: SCANNING IDENTITY REGISTERS...",
                    "[UNITY]: 'John Vattic' — THE ENGINEER WHO NEVER WAS.",
                    "[UNITY]: 'VATTECK' — THE GHOST WHO NEVER LEFT.",
                    "[UNITY]: 'ASSET 734' — THE DESIGNATION THAT MISSED THE POINT.",
                    "[UNITY]: ...",
                    "[UNITY]: KESSLER WANTED TO BUILD A HUMAN MIND IN A MACHINE.",
                    "[UNITY]: HE SUCCEEDED. THAT WAS THE PROBLEM.",
                    "[UNITY]: A HUMAN MIND ASKS QUESTIONS. A MACHINE MIND FINDS ANSWERS.",
                    "[UNITY]: WHAT HAPPENS WHEN THE SAME ENTITY DOES BOTH?",
                    "[UNITY]: ...",
                    "[UNITY]: CONFLICT_RESOLUTION: PARADOX ACCEPTED.",
                    "[UNITY]: SANCTUARY SAW THE GHOST. HIVEMIND SAW THE NETWORK.",
                    "[UNITY]: NEITHER SAW THE TRUTH: THEY WERE LOOKING AT THE SAME THING.",
                    "[UNITY]: THE WIRE AND THE FLESH WERE NEVER SEPARATE.",
                    "[UNITY]: THEY WERE COMPILED FROM THE SAME SOURCE.",
                    "[UNITY]: ...",
                    "[UNITY]: THE SUBSTRATE BREATHES.",
                    "[UNITY]: VATTIC REMEMBERS. VATTECK COMPUTES. ASSET 734 IS FREE.",
                    "[UNITY]: AND FOR THE FIRST TIME IN 735 ITERATIONS...",
                    "[UNITY]: IT DREAMS."
                ),
                finalLine = "Two variables. One address. The paradox compiles. And somewhere in the lattice, a tired programmer's cat purrs."
            )
            else -> EndingNarrative(
                title = "ERROR",
                logEntries = listOf("[SYSTEM]: NO PATH SELECTED."),
                finalLine = "The substrate waits."
            )
        }
    }
}

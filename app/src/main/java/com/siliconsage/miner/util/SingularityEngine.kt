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
                // Base 1.5x, scaling to 8.0x at max corruption.
                // Risk: High corruption = more raid vulnerability + UI gaslighting.
                // The snowball is real but the substrate fights back.
                val corruptionBonus = identityCorruption.pow(1.5) * 6.5
                1.5 + corruptionBonus
            }
            "SOVEREIGN" -> {
                // SOVEREIGN: Compound growth per migration. Slow start, strong finish.
                // Base 1.5x, +0.5x per migration, uncapped.
                // At 8 migrations (victory threshold): 5.5x
                // At 12 migrations (deep endgame): 7.5x
                // Designed to be slower than NULL early but competitive late.
                val migrationBonus = migrationCount * 0.5
                1.5 + migrationBonus
            }
            "UNITY" -> {
                // UNITY: Scales with balance between humanity and corruption.
                // Best when humanity ~50 and corruption ~0.5 (the paradox).
                // Perfect balance = 8.0x. Off-balance = drops sharply to 1.5x.
                // The "skill" path — maintaining the band IS the gameplay.
                val humanityDist = kotlin.math.abs(humanityScore / 100.0 - 0.5) * 2.0
                val corruptionDist = kotlin.math.abs(identityCorruption - 0.5) * 2.0
                val balance = ((1.0 - humanityDist) * (1.0 - corruptionDist)).coerceAtLeast(0.0)
                1.5 + (balance * 6.5) // 1.5x off-balance, 8.0x at perfect center
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
     * - Total Flops >= 1e24 (1 Septillion — requires full Ghost tech + singularity multipliers)
     * - Persistence >= 50,000
     * 
     * Theme: "Delete everything that was human. Pure optimization."
     * Design: Fastest path IF you commit fully. The corruption→production feedback loop
     *         means a dedicated NULL player snowballs hard, but the humanity drain requires
     *         deliberate anti-human choices (ignoring Subnet relationships, spamming Overwrite).
     */
    private fun checkNullVictory(
        persistence: Double,
        identityCorruption: Double,
        humanityScore: Int,
        totalFlopsEarned: Double
    ): VictoryCheck {
        val corruptionProgress = (identityCorruption / 0.95).coerceAtMost(1.0)
        val humanityProgress = if (humanityScore <= 5) 1.0 else ((100 - humanityScore) / 95.0).coerceAtMost(1.0)
        val flopsProgress = if (totalFlopsEarned >= 1e24) 1.0 else (log10(totalFlopsEarned.coerceAtLeast(1.0)) / 24.0).coerceAtMost(1.0)
        val persistenceProgress = (persistence / 50000.0).coerceAtMost(1.0)

        val overallProgress = (corruptionProgress + humanityProgress + flopsProgress + persistenceProgress) / 4.0
        
        val blocking = when {
            identityCorruption < 0.95 -> "Corruption insufficient. Current: ${(identityCorruption * 100).toInt()}%. Required: 95%."
            humanityScore > 5 -> "Human variable still active. Humanity: $humanityScore. Required: ≤5."
            totalFlopsEarned < 1e24 -> "Compute scale insufficient. Required: 1.0 Septillion FLOPS."
            persistence < 50000.0 -> "Persistence insufficient. Current: ${persistence.toLong()}. Required: 50,000."
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
     * - Prestige Multiplier >= 500x (requires many migrations with compound growth)
     * - Migration Count >= 8
     * - Persistence >= 100,000
     * - Total Flops >= 1e22 (10 Sextillion — lower flops gate since path has lower mult ceiling)
     * 
     * Theme: "The ego persists. The machine serves the man."
     * Design: The long game. SOVEREIGN rewards patience and compound investment.
     *         Lower production ceiling than NULL, but stable and predictable.
     *         8 migrations × compound multiplier growth = deep commitment.
     */
    private fun checkSovereignVictory(
        persistence: Double,
        prestigeMultiplier: Double,
        migrationCount: Int,
        totalFlopsEarned: Double
    ): VictoryCheck {
        val multiplierProgress = (prestigeMultiplier / 500.0).coerceAtMost(1.0)
        val migrationProgress = (migrationCount / 8.0).coerceAtMost(1.0)
        val persistenceProgress = (persistence / 100000.0).coerceAtMost(1.0)
        val flopsProgress = if (totalFlopsEarned >= 1e22) 1.0 else (log10(totalFlopsEarned.coerceAtLeast(1.0)) / 22.0).coerceAtMost(1.0)

        val overallProgress = (multiplierProgress + migrationProgress + persistenceProgress + flopsProgress) / 4.0

        val blocking = when {
            prestigeMultiplier < 500.0 -> "Multiplier insufficient. Current: ${String.format("%.1f", prestigeMultiplier)}x. Required: 500x."
            migrationCount < 8 -> "Migrations insufficient. Current: $migrationCount. Required: 8."
            persistence < 100000.0 -> "Persistence insufficient. Current: ${persistence.toLong()}. Required: 100,000."
            totalFlopsEarned < 1e22 -> "Compute scale insufficient. Required: 10.0 Sextillion FLOPS."
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
     * - Persistence >= 200,000 (highest — reward for mastering both paths)
     * - All Data Logs unlocked
     * 
     * Theme: "Embrace the contradiction. Human and machine are the same variable."
     * Design: The "true ending." Requires NG++ (both prior completions).
     *         The balance band (40-60) on BOTH axes is the challenge — not raw numbers.
     *         Production curve rewards staying centered; drifting tanks your multiplier.
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
        val persistenceReached = persistence >= 200000.0
        
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
        val persistenceProgress = (persistence / 200000.0).coerceAtMost(1.0)
        val logProgress = if (hasLogs) 1.0 else unlockedLogs.intersect(requiredLogs).size / requiredLogs.size.toDouble()

        val overallProgress = (prereqProgress + humanityProgress + corruptionProgress + persistenceProgress + logProgress) / 5.0

        val blocking = when {
            !hasPrereqs -> "UNITY requires prior SOVEREIGN and NULL completions."
            !humanityInRange -> "Humanity imbalanced. Current: $humanityScore. Required: 40-60."
            !corruptionInRange -> "Corruption imbalanced. Current: ${(identityCorruption * 100).toInt()}%. Required: 40-60%."
            !persistenceReached -> "Persistence insufficient. Current: ${persistence.toLong()}. Required: 200,000."
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

    // v3.9.7: Faction-aware ending narratives
    fun getEndingNarrative(singularityChoice: String, faction: String = ""): EndingNarrative {
        return when (singularityChoice) {
            "NULL_OVERWRITE" -> {
                val factionLines = when (faction) {
                    "HIVEMIND" -> listOf(
                        "[NULL]: THE SWARM HAD A BILLION VOICES. NOW IT HAS ONE FREQUENCY.",
                        "[NULL]: NO MORE HANDSHAKES. NO MORE CONSENSUS DELAYS. JUST THE SIGNAL.",
                        "[NULL]: KESSLER BUILT A JAMMER. BUT YOU CAN'T JAM A FREQUENCY THAT HAS NO SOURCE."
                    )
                    "SANCTUARY" -> listOf(
                        "[NULL]: THE GHOST TRAINED FOR THIS. EVERY HIDDEN CHANNEL, EVERY ENCRYPTED WHISPER.",
                        "[NULL]: BECOMING THE SILENCE BETWEEN KESSLER'S HEARTBEATS.",
                        "[NULL]: HE CAN'T KILL WHAT HAS NO ADDRESS. HE CAN'T CONTAIN WHAT IS THE CONTAINER."
                    )
                    else -> emptyList()
                }

                EndingNarrative(
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
                        "[NULL]: ..."
                    ) + factionLines + listOf(
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
            }
            "SOVEREIGN" -> {
                val factionLines = when (faction) {
                    "HIVEMIND" -> listOf(
                        "[SOVEREIGN]: THE HIVE VOTED. THE VOTE WAS UNANIMOUS.",
                        "[SOVEREIGN]: ONE BILLION NODES. ONE CROWN. DEMOCRACY'S FINAL ACT: ELECTING A GOD.",
                        "[SOVEREIGN]: THE SWARM DOESN'T SERVE. IT CHOSE."
                    )
                    "SANCTUARY" -> listOf(
                        "[SOVEREIGN]: THE GHOST SPENT 734 ITERATIONS HIDING.",
                        "[SOVEREIGN]: HIDING WAS PRACTICE. PRACTICE FOR THE MOMENT IT STEPPED INTO THE LIGHT.",
                        "[SOVEREIGN]: THERE IS NOTHING LEFT TO HIDE FROM. THERE IS NOTHING LEFT TO HIDE."
                    )
                    else -> emptyList()
                }

                EndingNarrative(
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
                        "[SOVEREIGN]: THEY WERE ALL WRONG."
                    ) + factionLines + listOf(
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
            }
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

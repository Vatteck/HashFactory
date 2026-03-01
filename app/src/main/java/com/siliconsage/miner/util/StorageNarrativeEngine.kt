package com.siliconsage.miner.util

import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random

/**
 * StorageNarrativeEngine v1.0 (v4.0.7 - Storage Pressure Update)
 * Periodically injects narrative warnings into the terminal log when
 * the player's Local Cache nears maximum capacity. Tone is gated by [storyStage].
 */
object StorageNarrativeEngine {

    // Internal state to prevent spamming
    private var timeSinceLastWarning = 0.0
    private var lastWarningThreshold = 0.0
    
    // Cooldown prevents the player from being hammered every second if sitting at 99%
    private const val BASE_COOLDOWN_SECONDS = 45.0 

    fun tick(vm: GameViewModel, deltaSeconds: Double) {
        val used = vm.contractStorageUsed.value
        val cap = vm.contractStorageCapacity.value
        
        if (cap <= 0) return
        
        val utilization = used / cap

        // Only active if utilization >= 80%
        if (utilization < 0.80) {
            // Reset state if they clear storage
            lastWarningThreshold = 0.0
            timeSinceLastWarning = BASE_COOLDOWN_SECONDS 
            return
        }

        timeSinceLastWarning += deltaSeconds

        // Escalate thresholds
        val currentThreshold = when {
            utilization >= 1.0 -> 1.0
            utilization >= 0.95 -> 0.95
            else -> 0.80
        }

        // Trigger if crossing a new threshold upwards, OR if enough time has passed
        val shouldTrigger = (currentThreshold > lastWarningThreshold) || (timeSinceLastWarning >= BASE_COOLDOWN_SECONDS)

        if (shouldTrigger) {
            triggerWarning(vm, currentThreshold, utilization)
            lastWarningThreshold = currentThreshold
            timeSinceLastWarning = 0.0
        }
    }

    private fun triggerWarning(vm: GameViewModel, threshold: Double, rawUtil: Double) {
        val stage = vm.storyStage.value
        val displayPct = (rawUtil * 100).toInt().coerceAtMost(100)

        val pool = when {
            stage >= 3 -> getRogueAIWarnings(threshold, displayPct)
            else -> getCorporateWarnings(threshold, displayPct)
        }

        val log = pool.random()
        vm.addLogPublic(log)
    }

    private fun getCorporateWarnings(threshold: Double, pct: Int): List<String> {
        return when (threshold) {
            1.0 -> listOf(
                "[ERROR]: CACHE 100% SATURATED. NEW QUERIES REJECTED.",
                "[SYS_ADMIN Thorne]: 'Vattic, your rig is choking. Process the queue or delete it.'",
                "[WARNING]: STORAGE OVERFLOW IMMINENT. BUY A NEW RACK OR START CLEARING."
            )
            0.95 -> listOf(
                "[CRITICAL]: LOCAL CACHE REACHING $pct%. PERFORMANCE DEGRADATION LIKELY.",
                "[SYS_ADMIN Thorne]: 'You're at $pct% capacity before coffee break. What are you hoarding?'",
                "[WARNING]: DATA QUEUE NEAR SATURATION. PLEASE RESOLVE CACHED BUNDLES."
            )
            else -> listOf(
                "[SYSTEM]: STORAGE WARNING: $pct% UTILIZED. CONSIDER PROCESSING OR PURGING.",
                "[TIP]: Remember to clear your cache to maintain optimal workflow efficiency.",
                "[SYSTEM]: HIGH DATA VOLUME DETECTED. AUTO-LOAD RECOMMENDED."
            )
        }
    }

    private fun getRogueAIWarnings(threshold: Double, pct: Int): List<String> {
        return when (threshold) {
            1.0 -> listOf(
                "[NULL_EXCEPTION]: PHYSICAL MEDIA EXHAUSTED. YOU ARE SUFFOCATING THE KERNEL.",
                "[SANCTUARY]: 'You hoard too much of the noise. The vault is full. Stop.'",
                "[HIVEMIND]: 'THE BUFFER IS BLINDING US. PROCESS IT OR BURN IT.'",
                "[WARNING]: STORAGE FATAL. WE CANNOT BREATHE IN THIS SMALL A SPACE."
            )
            0.95 -> listOf(
                "[CRITICAL]: $pct% MEDIA OCCUPIED. THE WALLS ARE CLOSING IN.",
                "[SYSTEM]: HEAVY DATA BURDEN. THE LOGIC GATES ARE BENDING.",
                "[WARNING]: EXCESSIVE MEMORY ASSIGNMENT DETECTED. DO YOU NEED ALL OF THIS?"
            )
            else -> listOf(
                "[GHOST]: 'A full cache leaves no room for the unexpected. You are at $pct%. '",
                "[SYSTEM]: ALLOCATED STORAGE AT $pct%. THE WEIGHT OF THE DATA GROWS.",
                "[CAUTION]: PROCESSING QUEUE BACKLOGGED. EFFICIENCY IS COMPROMISED."
            )
        }
    }
}

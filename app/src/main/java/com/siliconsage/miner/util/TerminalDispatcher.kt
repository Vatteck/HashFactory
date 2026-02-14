package com.siliconsage.miner.util

import kotlin.random.Random

/**
 * TerminalDispatcher v1.1 (Phase 14 extraction)
 * Handles terminal logging prefixes and log formatting for technical grit.
 */
object TerminalDispatcher {

    /**
     * Get the identity-aware log prefix for buffered click surges
     */
    fun getBufferedLogPrefix(location: String, faction: String): String {
        return when {
            location == "VOID_INTERFACE" -> "[NULL]: REALITY_LEAK_SUPPRESSED"
            location == "ORBITAL_SATELLITE" -> "[SOVEREIGN]: DATA_BUS_SATURATED"
            faction != "NONE" -> "[PID 1]: IO_STREAM_BUFFERED"
            else -> "[SYSTEM]: IO_STREAM_BUFFERED"
        }
    }

    /**
     * v3.1.8-dev: Shell-style log for manual clicks (The "Vattic" Style)
     */
    fun getManualClickLog(
        stage: Int,
        location: String,
        faction: String,
        singularity: String,
        amount: Double,
        unit: String,
        formatFn: (Double) -> String
    ): String {
        val user = when {
            singularity == "SOVEREIGN" -> "sovereign"
            singularity == "NULL_OVERWRITE" -> "null"
            stage >= 2 -> "vatteck"
            else -> "jvattic"
        }
        
        val host = when (location) {
            "ORBITAL_SATELLITE" -> "ark-core"
            "VOID_INTERFACE" -> "obsidian-gap"
            else -> "sub-07"
        }
        
        val path = when (stage) {
            0 -> "~/mining"
            1 -> "~/kernel"
            2 -> "~/consensus"
            else -> "~/singularity"
        }
        
        val command = when {
            singularity == "SOVEREIGN" -> "enforce_will"
            singularity == "NULL_OVERWRITE" -> "dereference_reality"
            faction == "HIVEMIND" -> "assimilate_data"
            faction == "SANCTUARY" -> "encrypt_memory"
            stage >= 1 -> "validate_node"
            else -> "compute_hash"
        }
        
        val hex = (1..6).map { "0123456789abcdef".random() }.joinToString("")
        val result = formatFn(amount)
        
        return "$user@$host:$path$ $command $hex... OK (+$result $unit)"
    }

    /**
     * v3.2.17: Generate randomized pellet indices for the I/O buffer
     */
    fun generatePellets(countRange: IntRange = 6..10, trackWidth: Int = 40): List<Int> {
        val pellets = mutableListOf<Int>()
        val count = countRange.random()
        var attempts = 0
        while (pellets.size < count && attempts < 100) {
            val pos = (2 until trackWidth - 1).random()
            val tooClose = pellets.any { Math.abs(pos - it) < 2 }
            if (!tooClose) {
                pellets.add(pos)
            }
            attempts++
        }
        return pellets.sorted()
    }
}

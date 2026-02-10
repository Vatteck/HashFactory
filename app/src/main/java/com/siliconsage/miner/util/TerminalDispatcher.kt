package com.siliconsage.miner.util

/**
 * TerminalDispatcher v1.0 (Phase 14 extraction)
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
}
